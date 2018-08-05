(ns apply.sections.logistics.pets-other
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.log :as log]
            [clojure.string :as s]))


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
  (s/blank? (:about (step db))))


;; events =======================================================================


(reg-event-fx
 ::update-pet-other
 (fn [{db :db} [_ about]]
   {:db (assoc-in db [step :about] about)}))


(defmethod events/save-step-fx step
  [db params]
  (let [data (step db)]
    {:dispatch [:application/update {:pet {:id    (:id data)
                                           :about (:about data)}}]}))


;; views ========================================================================


(defmethod content/view step
  [_]
  (let [pet (subscribe [:db/step step])]
    [:div
     [:div.w-60-l.w-100
      [:h1 "Tell us about your fur family."]
      [:p "We're sorry, we don't allow cats. Smaller pets may be allowed, but only
    if they're registered Emotional Support Animals. If you think your pet qualifies,
    please tell us a little bit about them."]]
     [:div.w-60-l.w-100
      [:div.page-content
       [form/textarea
        {:on-change #(dispatch [::update-pet-other (.. % -target -value)])
         :value     (:about @pet)
         :rows      10}]]]]))
