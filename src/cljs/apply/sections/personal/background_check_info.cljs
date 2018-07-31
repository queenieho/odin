(ns apply.sections.personal.background-check-info
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [clojure.string :as s]
            [re-frame.core :refer [dispatch subscribe reg-event-fx]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.locations :as locations]
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
  [{:keys [dob first-name last-name current_location]}]
  (and (some? dob)
       (not (s/blank? first-name))
       (not (s/blank? last-name))
       (not (s/blank? (:country current_location)))
       (not (s/blank? (:locality current_location)))
       (not (s/blank? (:region current_location)))))


(defmethod db/step-complete? step
  [db step]
  (not (form-complete (step db))))


;; events =======================================================================


(reg-event-fx
 ::update-background-info
 (fn [{db :db} [_ k v]]
   {:db (assoc-in db (into [step] k) v)}))


(defmethod events/save-step-fx step
  [db params]
  {:dispatch [:application/update (step db)]})


(defmethod events/gql->rfdb :current_location [k v] step)


(defmethod events/gql->value :current_location
  [k v]
  (let [data (subscribe [:db/step step])]
    (merge @data {:current_location v})))


;; views ========================================================================


(defmethod content/view step
  [_]
  (let [data (subscribe [:db/step step])]
    [:div
     [:div.w-60-l.w-100
      [:h1 "Tell us a little bit about yourself."]]
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
            :on-change #(dispatch [::update-background-info [:dob] %])}]]]
        [:div.cf.mb3-ns.mb0
         [form/item
          {:label "Full Legal Name"}
          [:div.w-30-l.w-100.fl.pr3-l.pr0
           [form/text
            {:placeholder "First"
             :value       (:first-name @data)
             :on-change   #(dispatch [::update-background-info [:first-name] (.. % -target -value)])}]]
          [:div.w-30-l.w-100.fl.pr3-l.pr0
           [form/text
            {:placeholder "Middle"
             :value       (:middle-name @data)
             :on-change   #(dispatch [::update-background-info [:middle-name] (.. % -target -value)])}]]
          [:div.w-30-l.w-100.fl.pr3-l.pr0
           [form/text
            {:placeholder "Last"
             :value       (:last-name @data)
             :on-change   #(dispatch [::update-background-info [:last-name] (.. % -target -value)])}]]]]
        [:div.cf.mb3-ns.mb0
         [form/item
          [form/item
           {:label "Location of residence"}
           [:div.w-30-l.w-100.fl.pr3-l.pr0
            [form/select
             {:placeholder "Select country"
              :value       (get-in @data [:current_location :country])
              :on-change   #(dispatch [::update-background-info [:current_location :country] (.. % -target -value)])}
             (map
              (fn [{:keys [name code]}]
                ^{:key code}
                [form/select-option {:value name} name])
              locations/countries)]]
           [:div.w-30-l.w-100.fl.pr3-l.pr0
            [form/text
             {:placeholder "City/Town"
              :value       (get-in @data [:current_location :locality])
              :on-change   #(dispatch [::update-background-info [:current_location :locality] (.. % -target -value)])}]]
           [:div.w-30-l.w-100.fl.pr3-l.pr0
            (if (= (get-in @data [:current_location :country]) "United States")
              [form/select
               {:placeholder "Select state"
                :value       (get-in @data [:current_location :region])
                :on-change   #(dispatch [::update-background-info [:current_location :region] (.. % -target -value)])}
               (map
                (fn [state]
                  ^{:key state}
                  [form/select-option {:value state} state])
                locations/states)]
              [form/text
               {:placeholder "Province/Region"
                :value       (get-in @data [:current_location :region])
                :on-change   #(dispatch [::update-background-info [:current_location :region] (.. % -target -value)])}])]
           [:div.w-10-l.w-100.fl.pr0-l.pr0
            [form/text
             {:placeholder "Zip"
              :value       (get-in @data [:current_location :postal_code])
              :on-change   #(dispatch [::update-background-info [:current_location :postal_code] (.. % -target -value)])}]]]]]]]]]))
