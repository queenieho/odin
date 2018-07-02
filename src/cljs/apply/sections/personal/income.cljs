(ns apply.sections.personal.income
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]))


(def step :personal/income)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  (if (= :cosigner (db step))
    :personal.income/cosigner
    :personal/about))


(defmethod db/previous-step step
  [db]
  :personal.background-check/info)


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
    [:h1 "Please verify your income."]
    [:p "To qualify to live in Starcity, your gross income must be at least 2.5x the cost of rent. Please submit acceptable forms of verification."]]
   [:div.page-content.w-90-l.w-100
    [:p
     [:ul
      [:li "YES: Most recent pay stub"]
      [:li "YES: Last three months bank statements"]
      [:li "YES: Offer letter"]
      [:li "NO: Stock portfolio"]
      [:li "NO: Photo of your crypto wallet"]
      [:li "NO: Photo of your actual wallet"]]]

    [ant/button {:class "mt3"} "Upload files"]

    [:p.mt3.mb3 "Are you taking a picture with your phone? Get an SMS link to finish this part of the application on your phone."]

    [:span {:on-click #(dispatch [:step.current/next :cosigner])}
     [form/checkbox {} "I am applying with a cosigner (i)"]]]])