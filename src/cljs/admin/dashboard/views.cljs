(ns admin.dashboard.views
  (:require [admin.dashboard.db :as db]
            [admin.content :as content]
            [admin.routes :as routes]
            [antizer.reagent :as ant]
            [clojure.string :as string]
            [iface.loading :as loading]
            [iface.components.typography :as typography]
            [iface.modules.payments]
            [iface.utils.formatters :as format]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [toolbelt.core :as tb]))


(defn notifications-modules []
  (let [payments (subscribe [:payments/by-statuses [:due :pending :failed]])
        orders (subscribe [:orders])]
    #_[:div
     [:h1 "test"]]
    [:div
     (.log js/console  "payments" @payments)
     (.log js/console  "orders" @orders)
     [:div.columns
      [:div.column.is-half
       [:h1 "Helping Hands Orders"]]
      [:div.column.is-half
       [:h1 "Payments"]]]
     [:div.columns
      [:div.column.is-half
       [:h1 "45 days"]]
      [:div.column.is-half
       [:h1 "Moving out"]]]]))


(defn dashboard-content []
  [:div
   (typography/view-header "Dashboard" "Important updates on our communities")
   [notifications-modules]])


;; ==============================================================================
;; entry ========================================================================
;; ==============================================================================

(defmethod content/view :dashboard [route]
  [dashboard-content])
