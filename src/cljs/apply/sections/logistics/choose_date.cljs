(ns apply.sections.logistics.choose-date
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.utils.log :as log]))


(def step :logistics.move-in-date/choose-date)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  ;; conditional - if chosen date is > 45 days from today, there's a different flow.
  ;; here's a placeholder flow for now.
  (if (= :after-45 (step db))
    :logistics.move-in-date/outside-application-window
    :logistics/occupancy))


(defmethod db/previous-step step
  [db]
  :logistics/move-in-date)


(defmethod db/step-complete? step
  [db step]
  (some? (step db)))


;; events =======================================================================


(defmethod events/save-step-fx step
  [db params]
  {:db       (assoc db step params)
   :dispatch [:step/advance]})


;; views ========================================================================


(defmethod content/view step
  [_]
  [:div
   [:div.w-60-l.w-100
    [:h1 "Let's get started." [:br] "When do you want to move-in?"]
    [:p "We'll do our best to accommodate your move-in date, but we cannot
    guarantee that the date you choose will be the date that you move in."]]
   [:div.page-content.w-90-l.w-100
    [ant/date-picker
     {:on-change #(dispatch [:step.current/next %])}]
    ;;NOTE - this button is a placeholder until the correct time logic has been
    ;;implemented, probably via momentjs
    [ant/button
     {:on-click #(dispatch [:step.current/next :after-45])}
     ">45 days flow"]]])
