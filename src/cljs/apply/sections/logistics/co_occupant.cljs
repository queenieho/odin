(ns apply.sections.logistics.co-occupant
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]))


(def step :logistics.occupancy/co-occupant)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :logistics/pets)


(defmethod db/previous-step step
  [db]
  :logistics/occupancy)


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
    [:h1 "Who will be living with you?"]
    [:p "Please give us some information so we can contact them to complete the
    background check and income verification steps."]]
   [:div.page-content.w-60-l.w-100
    [form/form-item
     {:label "Full Name"}
     [form/text]]
    [form/form-item
     {:label "Email Address"}
     [form/text]]
    [form/form-item
     {:label "Relationship to you"}
     [form/text]]]])
