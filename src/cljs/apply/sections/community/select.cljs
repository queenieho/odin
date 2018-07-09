(ns apply.sections.community.select
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]))


(def step :community/select)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  :community/term)


(defmethod db/next-step step
  [db]
  :community/term)


(defmethod db/previous-step step
  [db]
  (case (:logistics/pets db)
    :dog   :logistics.pets/dog
    :other :logistics.pets/other
    :logistics/pets))


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
    [:h1 "Which Starcity communities do you want to join?"]
    [:p "Browse our communities and learn about what makes each special."]]
   [:div.page-content.w-90-l.w-100
    [:ul
     [:li "west soma"]
     [:li "soma south park"]
     [:li "north beach"]
     [:li "the misison"]
     [:li "nopa"]
     [:li "venice beach"]]]])
