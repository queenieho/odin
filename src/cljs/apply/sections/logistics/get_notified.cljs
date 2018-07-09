(ns apply.sections.logistics.get-notified
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.button :as button]
            [apply.routes :as routes]))


(def step :logistics.move-in-date/get-notified)


;; db ===========================================================================


(defmethod db/has-back-button? step
  [_]
  false)


;; views ========================================================================


;; TODO get date for 45 days out
(defmethod content/view step
  [_]
  [:div
   [:div.w-60-l.w-100
    [:h1 "We'll send you an email notification before [date] to remind  you to apply."]
    [:p "Thanks for your interest in Starcity! Hope to see you soon."]]
   [:div.w-80-l.w-100
    [:div.page-content
     [:a {:href "/logout"}
      [button/primary
       {:on-click #()}
       "Close"]]]]])
