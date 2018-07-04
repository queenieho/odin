(ns apply.sections.logistics.dog
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-sub]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.log :as log]))


(def step :logistics.pets/dog)


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


(reg-event-fx
 ::update-dog
 (fn [{db :db} [_ k v]]
   (log/log "what's updatedog?" k v)
   {:db (assoc-in db [step k] v)}))


(defmethod events/save-step-fx step
  [db params]
  {:db       (assoc db step params)
   :dispatch [:step/advance]})


;; subs =========================================================================


(reg-sub
 ::dog-form
 (fn [db _]
   (step db)))


;; views ========================================================================


(defmethod content/view step
  [_]
  (let [pupper (subscribe [::dog-form])]
   [:div
    [:div.w-60-l.w-100
     [:h1 "Tell us about your fur family."]
     [:p "Most of our communities are dog-friendly, but we unfortunately do not
    allow cats. If you have a dog, please let us know what breed and weight."]]

    ;;NOTE - we need to style the form input components in such a way that we can have them inline.
    [:div.page-content.w-60-l.w-100
     [:div.cf.mb3-ns.mb0
      [:div.w-40-l.w-100.fl-pr4-l.pr0
       [form/form-item
        {:label "Name"}
        [form/text
         {:on-change #(dispatch [::update-dog :name (.. % -target -value)])}]]]
      [:div.w-40-l.w-100.fl-pr4-l.pr0
       [form/form-item
        {:label     "Breed"}
        [form/text
         {:on-change #(dispatch [::update-dog :breed (.. % -target -value)])}]]]
      [:div.w-20-l.w-100.fl-pr4-l.pr0
       [form/form-item
        {:label "Weight"}
        [form/number
         {:placeholder "lbs"
          :on-change #(dispatch [::update-dog :weight (.. % -target -value)])}]]]]
     [form/form
      [form/form-item
       {:label "How will your dog be taken care of during the day?"}
       [form/textarea
        {:on-change #(dispatch [::update-dog :daytime_care (.. % -target -value)])}]]

      [form/form-item
       {:label "Please describe your dog's demeanor."}
       [form/textarea
        {:on-change #(dispatch [::update-dog :demeanor (.. % -target -value)])}]]


      ;; NOTE - we'll also need an inline-friendly radio-group variant
      [form/form-item
       {:label "Spayed/neutered?"}
       [form/radio-group
        {:name "spayed-neutered"}
        [form/radio-option {} "Yes"]
        [form/radio-option {} "No"]]]

      [form/form-item
       {:label "Up-to-date vaccines?"}
       [form/radio-group
        {:name "vaccines"}
        [form/radio-option {} "Yes"]
        [form/radio-option {} "No"]]

       [form/form-item
        {:label "Has your dog ever bitten a human?"}
        [form/radio-group
         {:name "bitey"}
         [form/radio-option {} "Yes"]
         [form/radio-option {} "No"]]]]
      ]]]))
