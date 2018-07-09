(ns apply.sections.logistics.pets-other
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.log :as log]))


(def step :logistics.pets/other)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  :community/select)


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
  [:div
   [:div.w-60-l.w-100
    [:h1 "Tell us about your fur family."]
    [:p "We do not allow cats. Smaller pets may be allowed only if they are
    registered Emotional Support Animals. If your pet meets these requirements,
    tell us about them below."]]
   [:div.page-content.w-60-l.w-100
    [form/textarea
     {:on-change #(dispatch [::update-pet-other (.. % -target -value)])}]]])
