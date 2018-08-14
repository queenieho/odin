(ns onboarding.sections.helping-hands.byomf
  (:require [onboarding.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [onboarding.events :as events]
            [onboarding.db :as db]
            [iface.components.ptm.layout :as layout]
            [iface.components.ptm.ui.card :as card]))


(def step :helping-hands/byomf)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :helping-hands/bundles)


(defmethod db/previous-step step
  [db]
  :member-agreement/thanks)


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
   [layout/header
    {:title   "Get ready for moving day!"
     :subtext "Please review the scenarios below and select which one best
     represents your situation."}]
   [:div.w-100
    [:div.page-content
     [card/single
      {:title       "Just bringing myself"
       :subtitle    "No extra cost"
       :description "Select this option if you're just moving in basic things
       like your clothes and other small belongings."
       :img         "http://placekitten.com/300/200"
       :tag         "Most Common"
       :align       :left
       :on-click    #(.log js/console "click")}]
     [card/single
      {:title       "Just bringing myself"
       :subtitle    "No extra cost"
       :description "Select this option if you're just moving in basic things
       like your clothes and other small belongings."
       :img         "http://placekitten.com/300/200"
       :align       :left}]
     [card/single
      {:title       "Just bringing myself"
       :subtitle    "No extra cost"
       :description "Select this option if you're just moving in basic things
       like your clothes and other small belongings."
       :img         "http://placekitten.com/300/200"
       :align       :left}]]]])
