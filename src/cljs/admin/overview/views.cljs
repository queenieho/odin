(ns admin.overview.views
  (:require [admin.overview.db :as db]
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


(defn overview-content []
  [:div
   (typography/view-header "Overview" "Important updates on our communities")
   [notifications-modules]])


;; ==============================================================================
;; entry ========================================================================
;; ==============================================================================

(defmethod content/view :overview [route]
  [overview-content])
