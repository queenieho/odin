(ns apply.sections.personal.background-check
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx]]
            [apply.events :as events]
            [apply.db :as db]))


(def step :personal/background-check)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  (if (= :no (s db))
    :personal.background-check/declined
    :personal.background-check/info))


(defmethod db/next-step step
  [db]
  (if (= :no (step db))
    :personal.background-check/declined
    :personal.background-check/info))


(defmethod db/previous-step step
  [db]
  :personal/phone-number)


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
    [:h1 "Do we have your consent to perform a background check?"]
    [:p "We perform background checks to ensure the safety of our community
    members. Your background check is completely confidential, and we'll share
    the results (if any) with you."]]
   [:div.page-content.w-90-l.w-100
    [ant/button {:on-click #(dispatch [:step.current/next :yes])} "Yes"]
    [ant/button {:on-click #(dispatch [:step.current/next :no])} "No"]]])
