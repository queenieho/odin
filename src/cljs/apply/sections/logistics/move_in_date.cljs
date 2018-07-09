(ns apply.sections.logistics.move-in-date
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.utils.log :as log]))


(def step :logistics/move-in-date)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  (if (= :date (s db))
    :logistics.move-in-date/choose-date
    :logistics/occupancy))


(defmethod db/next-step step
  [db]
  (if (= :date (step db))
    :logistics.move-in-date/choose-date
    :logistics/occupancy))


(defmethod db/has-back-button? :logistics/move-in-date
  [_]
  false)


(defmethod db/step-complete? step
  [db step]
  (some? (step db)))

;; events =======================================================================


(defmethod events/save-step-fx step
  [db range]
  {:db       (assoc db step range)
   :dispatch [:application/update {:move_in_range range}]})


(defmethod events/gql->rfdb :move_in_range [_] step)


;; views ========================================================================


(defmethod content/view step
  [_]
  [:div
   [:div.w-60-l.w-100
    [:h1 "Let's get started." [:br] "When do you want to move-in?"]
    [:p "We'll do our best to accommodate your move-in date, but we cannot
    guarantee that the date you choose will be the date that you move in."]]
   [:div.page-content.w-90-l.w-100
    [ant/button
     {:on-click #(dispatch [:step.current/next :date])}
     "Choose a date"]
    [ant/button
     {:on-click #(dispatch [:step.current/next :asap])}
     "ASAP"]
    [ant/button
     {:on-click #(dispatch [:step.current/next :flexible])}
     "I'm flexible"]]])
