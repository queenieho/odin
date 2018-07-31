(ns apply.sections.payment.complete
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.utils.log :as log]))


(def step :payment/complete)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  nil)


(defmethod db/next-step step
  [db]
  nil)


(defmethod db/previous-step step
  [db]
  :payment/review)


(defmethod db/has-back-button? step
  [_]
  false)


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
    [:h1 "Thanks for applying live in Starcity! What’s next?"]
    [:p "We’ll review your application soon and let you know if you’re qualified."]]
   [:div.page-content.w-90-l.w-100
    [:p "In the meantime, you can check out your application status here:"]
    [ant/button {:on-click #(log/log "application status!")}
     "application status"]]])
