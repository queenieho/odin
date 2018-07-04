(ns apply.sections.logistics.occupancy
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.utils.log :as log]))


(def step :logistics/occupancy)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  (if (= :double (step db))
    :logistics.occupancy/co-occupant
    :logistics/pets))


(defmethod db/previous-step step
  [db]
  (if (some? (:logistics.move-in-date/choose-date db))
    :logistics.move-in-date/choose-date
    :logistics/move-in-date))


(defmethod db/has-back-button? step
  [_]
  true)


(defmethod db/step-complete? step
  [db step]
  false)


;; events =======================================================================


(defmethod events/save-step-fx step
  [db occupancy]
  {:dispatch [:application/update {:occupancy occupancy}]})


(defmethod events/gql->rfdb :occupancy [_] step)


;; views ========================================================================


(defmethod content/view step
  [_]
  [:div
   [:div.w-60-l.w-100
    [:h1 "How many adults will be living in the unit?"]
    [:p "Most of our units have one bed and are best suited for one adult, but some are available for two adults."]]
   [:div.page-content.w-90-l.w-100
    [ant/button
     {:on-click #(dispatch [:step.current/next :single])}
     "one"]
    [ant/button
     {:on-click #(dispatch [:step.current/next :double])}
     "two"]]])
