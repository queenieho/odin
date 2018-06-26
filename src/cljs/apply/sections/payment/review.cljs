(ns apply.sections.payment.review
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]))


(def step :payment/review)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :payment/complete)


(defmethod db/previous-step step
  [db]
  :personal/about)


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
    [:h1 "Review our terms to finish and pay your $25 application fee."]
    [:p "Please take a moment to review our Terms of Service and Privacy Policy."]]
   [:div.page-content.w-90-l.w-100
    [form/checkbox {} "I have read and agrere to the Terms of Service and Privacy Policy."]]])
