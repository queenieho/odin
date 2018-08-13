(ns onboarding.sections.helping-hands.request-furniture
  (:require [onboarding.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [onboarding.events :as events]
            [onboarding.db :as db]))


(def step :helping-hands/request-furniture)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :next/step)


(defmethod db/previous-step step
  [db]
  :previous/step)


(defmethod db/has-back-button? step
  [_]
  false)


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
    [:h1 "Step Header"]
    [:p "Step description"]]
   [:div.page-content.w-90-l.w-100
    [:p "Step content"]]])
