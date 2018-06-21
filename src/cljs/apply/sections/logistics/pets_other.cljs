(ns apply.sections.logistics.pets-other
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]))


(def step :logistics.pets/other)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :community/select)


(defmethod db/previous-step step
  [db]
  :logistics/pets)


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
    [:h1 "Tell us about your fur family."]
    [:p "We do not allow cats. Smaller pets may be allowed only if they are
    registered Emotional Support Animals. If your pet meets these requirements,
    tell us about them below."]]
   [:div.page-content.w-60-l.w-100
    [form/textarea]]])
