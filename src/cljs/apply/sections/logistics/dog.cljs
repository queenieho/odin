(ns apply.sections.logistics.dog
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-sub]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.log :as log]
            [toolbelt.core :as tb]
            [iface.utils.formatters :as format]))


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


(defn- get-boolean
  [val]
  (case val
    "true"  true
    "false" false
    nil))


(defn- parse-dog-data
  [{:keys [sterile vaccines bitten weight]
    :as   data}]
  (-> data
      (assoc :vaccines (or (get-boolean vaccines) (nil? vaccines))
             :sterile (or (get-boolean sterile) (nil? sterile))
             :bitten (or (get-boolean bitten) (not (nil? bitten)))
             :weight (tb/str->int weight))))


(defmethod events/save-step-fx step
  [db params]
  (let [data (step db)]
    {:dispatch [:application/update {:pet (parse-dog-data data)}]}))


;; views ========================================================================


(defmethod content/view step
  [_]
  (let [pupper (subscribe [:db/step step])]
    [:div
     [:div.w-60-l.w-100
      [:h1 "Tell us about your fur family."]
      [:p "Most of our communities are dog-friendly, but we unfortunately don't
    allow cats. If you have a dog, please tell us a little bit about them."]]
     [:div.w-60-l.w-100
      [:div.page-content
       [:div.cf.mb3-ns.mb0
        [:div.w-40-l.w-100.fl.pr4-l.pr0
         [form/item
          {:label "Name"}
          [form/text
           {:value     (:name @pupper)
            :on-change #(dispatch [::update-dog :name (.. % -target -value)])}]]]
        [:div.w-40-l.w-100.fl.pr4-l.pr0
         [form/item
          {:label "Breed"}
          [form/text
           {:value     (:breed @pupper)
            :on-change #(dispatch [::update-dog :breed (.. % -target -value)])}]]]
        [:div.w-20-l.w-100.fl.pr0
         [form/item
          {:label "Weight"}
          [form/number
           {:placeholder "lbs"
            :value       (:weight @pupper)
            :on-change   #(dispatch [::update-dog :weight (.. % -target -value)])}]]]]
       [:div.cf.mb3-ns.mb0
        [form/item
         {:label "How will your dog be taken care of during the day?"}
         [form/textarea
          {:on-change #(dispatch [::update-dog :daytime_care (.. % -target -value)])
           :value     (:daytime_care @pupper)
           :rows      3}]]]
       [:div.cf.mb3-ns.mb0
        [form/item
         {:label "Please describe your dog's demeanor."}
         [form/textarea
          {:on-change #(dispatch [::update-dog :demeanor (.. % -target -value)])
           :value     (:demeanor @pupper)
           :rows      3}]]]
       [:div.cf
        [:div.w-third-l.w-100.fl.pr4-l.pr0.mb3
         [form/item
          {:label "Spayed / neutered?"}
          [form/radio-group
           {:value     (or (:sterile @pupper) "true")
            :on-change #(dispatch [::update-dog :sterile (.. % -target -value)])}
           [form/radio-option
            {:value "true"}
            "Yes"]
           [form/radio-option
            {:value "false"}
            "No"]]]]
        [:div.w-third-l.w-100.fl.pr4-l.pr0.mb3
         [form/item
          {:label "Up-to-date vaccines?"}
          [form/radio-group
           {:value     (or (:vaccines @pupper) "true")
            :on-change #(dispatch [::update-dog :vaccines (.. % -target -value)])}
           [form/radio-option
            {:value "true"}
            "Yes"]
           [form/radio-option
            {:value "false"}
            "No"]]]]
        [:div.w-third-l.w-100.fl.pr4-l.pr0.mb3
         [form/item
          {:label "Ever bitten a human?"}
          [form/radio-group
           {:value (or (:bitten @pupper) "false")
            :on-change #(dispatch [::update-dog :bitten (.. % -target -value)])}
           [form/radio-option
            {:value "true"}
            "Yes"]
           [form/radio-option
            {:value "false"}
            "No"]]]]]]]]))
