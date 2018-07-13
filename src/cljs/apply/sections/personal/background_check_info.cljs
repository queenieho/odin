(ns apply.sections.personal.background-check-info
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.log :as log]))


(def step :personal.background-check/info)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  :personal/income)


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
  (let [data (subscribe [:db/step step])]
    [:div
     (log/log "form" @data)
     [:div.w-60-l.w-100
      [:h1 "Please fill out your personal information."]]
     [:div.w-90-l.w-100
      [:div.page-content
       [form/form
        [:div.cf.mb4-ns.mb0
         [form/item
          {:label "Date of Birth"}
          ;; NOTE date picker needs to be styled to match our style...
          ;; or we need to make a new one
          [ant/date-picker {:on-change #(.log js/console (.. % -target))}]]]
        [:div.cf.mb3-ns.mb0
         [form/item
          {:label "Full Legal Name"}
          [:div.w-30-l.w-100.fl.pr3-l.pr0
           [form/text {:placeholder "First"}]]
          [:div.w-30-l.w-100.fl.pr3-l.pr0
           [form/text {:placeholder "Middle"}]]
          [:div.w-30-l.w-100.fl.pr3-l.pr0
           [form/text {:placeholder "Last"}]]]]
        [:div.cf.mb3-ns.mb0
         [form/item
          [form/item
           {:label "Location of residence"}
           [:div.w-30-l.w-100.fl.pr3-l.pr0
            [form/text {:placeholder "Country"}]]
           [:div.w-30-l.w-100.fl.pr3-l.pr0
            [form/text {:placeholder "City"}]]
           [:div.w-30-l.w-100.fl.pr3-l.pr0
            [form/text {:placeholder "State"}]]
           [:div.w-10-l.w-100.fl.pr0-l.pr0
            [form/text {:placeholder "Zip"}]]]]]]]]]))
