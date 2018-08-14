(ns onboarding.sections.member-agreement.thanks
  (:require [onboarding.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [onboarding.events :as events]
            [onboarding.db :as db]
            [iface.components.ptm.layout :as layout]))


(def step :member-agreement/thanks)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :helping-hands/byomf)


(defmethod db/previous-step step
  [db]
  :member-agreement/sign)


(defmethod db/has-back-button? step
  [_]
  true)


(defmethod db/step-complete? step
  [db step]
  true)


;; events =======================================================================


(defmethod events/save-step-fx step
  [db params]
  {:db       (assoc db step params)
   :dispatch [:step/advance]})


;; views ========================================================================


(defmethod content/view step
  [_]
  [:div
   [layout/header
    {:title "Thanks for signing your member agreement."}]
   [:div.w-60-l.w-100
    [:div.page-content]]])
