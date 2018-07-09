(ns apply.sections.logistics.get-notified
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-sub]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.button :as button]
            [apply.routes :as routes]
            [iface.utils.formatters :as format]))


(def step :logistics.move-in-date/get-notified)


;; db ===========================================================================


(defmethod db/has-back-button? step
  [_]
  false)


;; subs =========================================================================


(reg-sub
 :db
 (fn [db _]
   db))

;; views ========================================================================


;; NOTE I'm assuming that the date used here is for the user's
;; selected date. If not, we have to revise this header
(defmethod content/view step
  [_]
  (let [date (subscribe [:step/data])]
    [:div
     [:div.w-60-l.w-100
      [:h1 "We'll send you an email notification before " (format/date-month-day @date) " to remind  you to apply."]
      [:p "Thanks for your interest in Starcity! Hope to see you soon."]]
     [:div.w-80-l.w-100
      [:div.page-content
       [:a {:href "/logout"}
        [button/primary
         {:on-click #()}
         "Close"]]]]]))
