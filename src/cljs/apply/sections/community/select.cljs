(ns apply.sections.community.select
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx]]
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


(defmethod events/gql->rfdb :communities [_] step)


(defmethod events/gql->value :communities [_ v]
  (map
   (fn [community]
     (:code community))
   v))


;; views ========================================================================


(defn community-content
  "Produces a community card content element"
  [price availability]
  [:p.mb0.mt3
   "From " (format/currency price)
   [:br]
   availability " units open"])


(def communities [{:title       "Soma"
                   :description [community-content 1500 2]
                   :images      ["http://placekitten.com/600/600"
                                 "http://placekitten.com/600/400"
                                 "http://placekitten.com/600/500"
                                 "http://placekitten.com/600/700"]
                   :value       "52gilbert"}
                  {:title       "Mission"
                   :description [community-content 1200 5]
                   :images      ["http://placekitten.com/600/600"
                                 "http://placekitten.com/600/400"
                                 "http://placekitten.com/600/500"
                                 "http://placekitten.com/600/700"]
                   :value       "2072mission"}
                  {:title       "South Park"
                   :description [community-content 2000 2]
                   :images      ["http://placekitten.com/600/600"
                                 "http://placekitten.com/600/400"
                                 "http://placekitten.com/600/500"
                                 "http://placekitten.com/600/700"]
                   :value       "414bryant"}])


(defn- update-group-value [coll v]
  (if (some #(= v %) coll)
    (remove #(= v %) coll)
    (conj coll v)))


(defmethod content/view step
  [_]
  (let [data (subscribe [:db/step step])]
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
