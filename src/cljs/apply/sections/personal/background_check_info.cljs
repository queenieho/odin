(ns apply.sections.personal.background-check-info
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [clojure.string :as s]
            [re-frame.core :refer [dispatch subscribe reg-event-fx]]
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


(defn- form-complete
  [{:keys [dob first-name middle-name last-name country city state zip]}]
  (and (some? dob)
       (not (s/blank? first-name))
       (not (s/blank? last-name))
       (not (s/blank? country))
       (not (s/blank? city))
       (not (s/blank? state))))


(defmethod db/step-complete? step
  [db step]
  false
  ;; NOTE commenting this out until this works
  #_(not (form-complete (step db))))


;; events =======================================================================


(reg-event-fx
 ::update-background-info
 (fn [{db :db} [_ k v]]
   {:db (assoc-in db [step k] v)}))


(defmethod events/save-step-fx step
  [db params]
  {:dispatch [:application/update (step db)]}
  #_{:db     (assoc db step params)
     :dispatch [:step/advance]})


(defmethod events/gql->rfdb :current_location [k v] step)


(defmethod events/gql->value :current_location
  [k v]
  (let [data (subscribe [:db/step step])]
    (merge @data v)))


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
          [ant/date-picker
           {:value     (:dob @data)
            :on-change #(dispatch [::update-background-info :dob %])}]]]
        [:div.cf.mb3-ns.mb0
         [form/item
          {:label "Full Legal Name"}
          [:div.w-30-l.w-100.fl.pr3-l.pr0
           [form/text
            {:placeholder "First"
             :value       (:first-name @data)
             :on-change   #(dispatch [::update-background-info :first-name (.. % -target -value)])}]]
          [:div.w-30-l.w-100.fl.pr3-l.pr0
           [form/text
            {:placeholder "Middle"
             :value       (:middle-name @data)
             :on-change   #(dispatch [::update-background-info :middle-name (.. % -target -value)])}]]
          [:div.w-30-l.w-100.fl.pr3-l.pr0
           [form/text
            {:placeholder "Last"
             :value       (:last-name @data)
             :on-change   #(dispatch [::update-background-info :last-name (.. % -target -value)])}]]]]
        [:div.cf.mb3-ns.mb0
         [form/item
          [form/item
           {:label "Location of residence"}
           [:div.w-30-l.w-100.fl.pr3-l.pr0
            [form/text
             {:placeholder "Country"
              :value       (:country @data)
              :on-change   #(dispatch [::update-background-info :country (.. % -target -value)])}]]
           [:div.w-30-l.w-100.fl.pr3-l.pr0
            [form/text
             {:placeholder "City/Town"
              :value       (:locality @data)
              :on-change   #(dispatch [::update-background-info :locality (.. % -target -value)])}]]
           [:div.w-30-l.w-100.fl.pr3-l.pr0
            [form/text
             {:placeholder "State/Province/Region"
              :value       (:region @data)
              :on-change   #(dispatch [::update-background-info :region (.. % -target -value)])}]]
           [:div.w-10-l.w-100.fl.pr0-l.pr0
            [form/text
             {:placeholder "Zip"
              :value       (:zip @data)
              :on-change   #(dispatch [::update-background-info :zip (.. % -target -value)])}]]]]]]]]]))
