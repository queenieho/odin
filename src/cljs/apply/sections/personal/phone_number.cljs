(ns apply.sections.personal.phone-number
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.log :as log]))


(def step :personal/phone-number)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :personal/background-check)


(defmethod db/previous-step step
  [db]
  :community/term)


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
    [:h1 "What's your phone number?"]
    [:p "We promise we'll keep your phone number private and only contact you by
    phone with prior permission."]]
   [:div.page-content.w-60-l.w-100
    [form/item
     {:label "Phone Number"}
     [form/text
      {:on-change #(log/log "new phone number" (.. % -target -value))}]]]])
