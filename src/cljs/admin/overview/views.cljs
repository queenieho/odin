(ns admin.overview.views
  (:require [admin.overview.db :as db]
            [admin.content :as content]
            [admin.routes :as routes]
            [antizer.reagent :as ant]
            [clojure.string :as string]
            [iface.loading :as loading]
            [iface.components.table :as table]
            [iface.components.typography :as typography]
            [iface.modules.payments]
            [iface.utils.formatters :as format]
            [iface.utils.time :as time]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [toolbelt.core :as tb]))


(def render-name
  (table/wrap-cljs
   (fn [_ {{name :name} :account}]
     [:p.fs3 (subs name 0 (if (< 18 (count name)) 18 (count name)))])))


(def render-order-name
  (table/wrap-cljs
   (fn [_ {name :name}]
     [:p.fs3 (subs name 0 (if (< 25 (count name)) 25 (count name)))])))


(def render-date
  (table/wrap-cljs
   (fn [_ {created :created}]
     [:p.fs3 (format/date-short-num created)])))


(def render-status
  (table/wrap-cljs
   (fn [_ {status :status}]
     [:div.align-right
      [:span.tag.is-hollow status]])))


(def render-payment-description
  (table/wrap-cljs
   (fn [_ {description :description}]
     [:p.fs3 description])))


(def render-amount
  (table/wrap-cljs
   (fn [_ {amount :amount}]
     [:p.fs3 (format/currency amount)])))


(defmulti columns (fn [role] role))


(defmethod columns :orders [_]
  [{:title "Member"
    :dataIndex :member
    :render render-name}
   {:title "Order"
    :dataIndex :order
    :render render-order-name}
   {:title "Date"
    :dataIndex :date
    :render render-date}
   {:title "Status"
    :dataIndex :status
    :render render-status}])


(defmethod columns :payments [_]
  [{:title "Member"
    :dataIndex :member
    :render render-name}
   {:title "Payment description"
    :dataIndex :description
    :render render-payment-description}
   {:title "Amount"
    :dataIndex :amount
    :render render-amount}
   {:title "Status"
    :dataIndex :status
    :render render-status}])


(defn- notifications-table [type items title]
  [:div.column.is-half
   [:h3 title]
   [ant/table {:pagination {:page-size 5
                            :size      :small}
               :columns    (columns type)
               :dataSource (map #(assoc % :key (:id %)) items)}]])


(defn notifications []
  (let [payments (subscribe [:payments/by-status [:due :pending :failed]])
        orders (subscribe [:orders])]
    [:div
     [:div.columns
      [notifications-table :orders (sort-by :created > @orders) "Helping Hands"]
      [notifications-table :payments (sort-by :created > @payments) "Payments"]]
     #_[:div.columns
      [orders-table @orders]
      [orders-table @orders]]]))


(defn overview-content []
  [:div
   (typography/view-header "Overview" "Important updates on our communities")
   [notifications]])


;; ==============================================================================
;; entry ========================================================================
;; ==============================================================================

(defmethod content/view :overview [route]
  [overview-content])
