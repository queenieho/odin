(ns apply.sections.personal.background-check-declined
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]))


(def step :personal.background-check/declined)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  nil)


(defmethod db/next-step step
  [db]
  nil)


(defmethod db/has-back-button? step
  [_]
  false)


;; views ========================================================================


(defmethod content/view step
  [_]
  [:div
   [:div.w-60-l.w-100
    [:h1 "Thank you for your interest in Starcity."]]
   [:div.page-content.w-90-l.w-100
    [:a {:href "/logout"} [ant/button "Close"]]]])
