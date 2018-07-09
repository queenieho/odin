(ns apply.sections.personal.income-cosigner
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]))


(def step :personal.income/cosigner)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  :personal/about)


(defmethod db/next-step step
  [db]
  :personal/about)


(defmethod db/previous-step step
  [db]
  :personal/income)


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
    [:h1 "Who will be cosigning with you?"]
    [:p "Please give us some information so we can contact them to complete the
    income verification steps."]]
   [:div.page-content.w-90-l.w-100
    [form/item
     {:label "Full Name"}
     [form/text]]
    [form/item
     {:label "Email Address"}
     [form/text]]
    [form/item
     {:label "Relationship to you"}
     [form/text]]]])
