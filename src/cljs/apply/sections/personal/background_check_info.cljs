(ns apply.sections.personal.background-check-info
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]))


(def step :personal.background-check/info)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :personal/income)


(defmethod db/previous-step step
  [db]
  :personal/background-check)


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
    [:h1 "Please fill out your personal information."]]
   [:div.page-content.w-90-l.w-100
    [form/form
     [form/form-item
      {:label "Date of Birth"}
      [ant/date-picker]]
     [form/form-item
      {:label "Full Legal Name"}
      [form/text {:placeholder "First"}]
      [form/text {:placeholder "Middle"}]
      [form/text {:placeholder "Last"}]]
     [form/form-item
      [form/form-item
       {:label "Location of residence"}
       [form/select
        {}
        [form/select-option "Canada"]
        [form/select-option "USA"]
        [form/select-option "Mexico"]]
       [form/text {:placeholder "City"}]
       [form/text {:placeholder "State"}]
       [form/text {:placeholder "Zip"}]]]]]])
