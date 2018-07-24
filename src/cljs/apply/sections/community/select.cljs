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
            [iface.utils.log :as log]
            [iface.components.ptm.ui.modal :as modal]
            [toolbelt.core :as tb]))


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


(defmethod events/save-step-fx step
  [db params]
  (let [data (step db)]
    {:dispatch [:application/update {:communities data}]}))


(defmethod events/gql->rfdb :communities [_] step)


(defmethod events/gql->value :communities [_ v]
  (map
   (fn [community]
     (:id community))
   v))


(reg-event-fx
 ::update-selection
 (fn [{db :db} [_ v]]
   {:db (assoc db step v)}))


(reg-event-fx
 ::open-community-modal
 (fn [{db :db} [_ v]]
   {:db       (assoc db :community.select/modal v)
    :dispatch [:modal/show :community.select/modal]}))


(reg-event-fx
 ::close-community-modal
 (fn [{db :db} _]
   {:db       (dissoc db :community.select/modal)
    :dispatch [:modal/hide :community.select/modal]}))


;; subscriptions ================================================================


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


(reg-sub
 ::communities
 (fn [db _]
   (->> (:communities-options db)
        (map-indexed
         (fn [idx {:keys [id code name rates units cover_image_url application_copy]}]
           (let [rate   (get-lowest-rate rates)
                 ucount (count-available-units units)
                 mcopy  (tb/assoc-some application_copy
                                       :index idx
                                       :price rate
                                       :units-available ucount
                                       :value id)]
             {:index       idx
              :title       name
              :value       id
              :description [community-content rate ucount]
              :ucount      ucount
              :images      (:images application_copy)
              :modal-copy  mcopy})))
        (sort-by :ucount)
        (map #(dissoc % :ucount)))))


(reg-sub
 ::community-modal
 (fn [db _]
   (:community.select/modal db)))


(reg-sub
 ::next-community
 :<- [:db]
 :<- [::communities]
 (fn [[db communities] _]
   (let [modal       (:community.select/modal db)
         index       (:index modal)]
     (if (= (inc index) (count communities))
       (first communities)
       (first (filter #(= (inc index) (:index %)) communities))))))


;; views ========================================================================


(defn- update-group-value [coll v]
  (if (some #(= v %) coll)
    (remove #(= v %) coll)
    (conj coll v)))


(defmethod content/view step
  [_]
  (let [data          (subscribe [:db/step step])
        communities   (subscribe [::communities])
        is-showing    (subscribe [:modal/visible? :community.select/modal])
        modal-content (subscribe [::community-modal])
        on-select     #(dispatch [::update-selection (update-group-value @data %)])
        next          (subscribe [::next-community])]
    [:div
     [modal/community (merge
                       {:visible   @is-showing
                        :on-close  #(dispatch [::close-community-modal])
                        :on-select #(on-select %)
                        :selected  (some #(= (:value @modal-content) %) @data)
                        :on-next   #(dispatch [::open-community-modal (:modal-copy @next)])
                        :next      {:name (:title @next)}}
                       @modal-content)]
     [:div.w-60-l.w-100
      [:h1 "Which Starcity communities do you want to join?"]
      [:p "Browse our communities and learn about what makes each special."]]
     [:div.w-60-l.w-100
      [:div.page-content
       [card/group
        {:on-change  #(on-select %)
         :value      @data
         :card-width :half
         :show-count true}
        (map
         (fn [item]
           ^{:key (:value item)}
           [card/carousel-card
            (assoc item :on-card-click #(dispatch [::open-community-modal (:modal-copy item)]))])
         @communities)]]]]))
