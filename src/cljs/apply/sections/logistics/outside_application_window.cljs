(ns apply.sections.logistics.outside-application-window
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]))


(def step :logistics.move-in-date/outside-application-window)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  (if (= :choose-new-date (step db))
    :logistics.move-in-date/choose-date
    :logistics.move-in-date/get-notified))


(defmethod db/previous-step step
  [db]
  :logistics.move-in-date/choose-date)


(defmethod db/has-back-button? step
  [_]
  true)


(defmethod db/step-complete? step
  [db step]
  false)


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
    [:h1 "We currently only accept applicants who want to move in by [date] or sooner."]
    [:p "You cna change your move-in date, or get notified when it's time to apply."]]
   [:div.page-content.w-90-l.w-100
    [ant/button
     {:on-click #(dispatch [:step.current/next :choose-new-date])}
     "change date"]
    [ant/button
     {:on-click #(dispatch [:step.current/next :get-notified])}
     "get notified"]]])
