(ns apply.sections.community.select
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch
                                   subscribe
                                   reg-event-fx
                                   reg-sub]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.utils.formatters :as format]
            [iface.components.ptm.ui.card :as card]
            [iface.utils.log :as log]))


(def step :community/select)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  :community/term)


(defmethod db/next-step step
  [db]
  :community/term)


(defmethod db/previous-step step
  [db]
  (case (:logistics/pets db)
    :dog   :logistics.pets/dog
    :other :logistics.pets/other
    :logistics/pets))


(defmethod db/has-back-button? step
  [_]
  true)


(defmethod db/step-complete? step
  [db step]
  (if (empty? (step db))
    true
    false))


;; events =======================================================================


(reg-event-fx
 ::update-selection
 (fn [{db :db} [_ v]]
   {:db (assoc db step v)}))


(defmethod events/save-step-fx step
  [db params]
  (let [data (step db)]
    {:dispatch [:application/update {:communities data}]}))


(defmethod events/init-step step
  [s]
  {:dispatch-n [[:ui/loading :step.current/save false]
                [::query-communities]]})


(reg-event-fx
 ::query-communities
 (fn [_ [k]]
   (log/log "getting communities")
   {:graphql {:query [[:properties [:id :name :code :cover_image_url
                                    [:rates [:rate]]
                                    [:units [[:occupant [:id]]]]]]]
              :on-success [::query-communities-success]
              :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::query-communities-success
 (fn [{db :db} [_ response]]
   (let [communities (get-in response [:data :properties])]
     (log/log "queried" communities)
     {:db (assoc db :communities-options communities)})))


(defmethod events/gql->rfdb :communities [_] step)


(defmethod events/gql->value :communities [_ v]
  (map
   (fn [community]
     (:code community))
   v))


;; subscriptions ================================================================


(reg-sub
 ::communities
 (fn [db _]
   (:communities-options db)))


;; views ========================================================================


(defn community-content
  "Produces a community card content element"
  [price availability]
  [:p.mb0.mt3
   "From " (format/currency price)
   [:br]
   availability " units open"])


(defn- get-lowest-rate
  [rates]
  (->> (sort-by :rate rates)
       first
       :rate))


(defn- count-available-units [units]
  (->> (filter #(nil? (:occupant %)) units)
       count))


(defn- parse-communities
  [communities]
  (->> communities
       (map
        (fn [{:keys [code name rates units cover_image_url]}]
          (let [rate   (get-lowest-rate rates)
                ucount (count-available-units units)]
            {:title       name
             :value       code
             :description [community-content rate ucount]
             :ucount       ucount
             :images      [cover_image_url]})))
       (sort-by :ucount)
       (map #(dissoc :ucount %))))


(defn- update-group-value [coll v]
  (if (some #(= v %) coll)
    (remove #(= v %) coll)
    (conj coll v)))


(defmethod content/view step
  [_]
  (let [data        (subscribe [:db/step step])
        communities (parse-communities @(subscribe [::communities]))]
    [:div
     [:div.w-60-l.w-100
      [:h1 "Which Starcity communities do you want to join?"]
      [:p "Browse our communities and learn about what makes each special."]]
     [:div.w-60-l.w-100
      [:div.page-content
       [card/group
        {:on-change  #(dispatch [::update-selection (update-group-value @data %)])
         :value      @data
         :card-width :half
         :show-count true}
        (map
         (fn [item]
           ^{:key (:value item)}
           [card/carousel-card item])
         communities)]]]]))
