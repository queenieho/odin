(ns apply.sections.logistics.outside-application-window
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.card :as card]))


(def step :logistics.move-in-date/outside-application-window)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  (if (= :choose-new-date (s db))
    :logistics.move-in-date/choose-date
    :logistics.move-in-date/get-notified))


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
   [:div.w-80-l.w-100
    [:div.page-content
     [card/single
      {:title    "Change date"
       :img      "/assets/images/ptm/icons/sketch-calendar.svg"
       :on-click #(dispatch [:step.current/next :choose-new-date])}]
     [card/single
      {:title    "Get notified"
       :img      "/assets/images/ptm/icons/sketch-notification.svg"
       :on-click #(dispatch [:step.current/next :get-notified])}]]]])
