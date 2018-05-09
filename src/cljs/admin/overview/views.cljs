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


(def render-name-from-account
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


(def render-name
  (table/wrap-cljs
   (fn [_ {name :name}]
     [:p.fs3 (subs name 0 (if (< 18 (count name)) 18 (count name)))])))


(def render-property
  (table/wrap-cljs
   (fn [_ {{name :name} :property}]
     [:p.fs3 name])))


(def render-days-left
  (table/wrap-cljs
   (fn [_ {{ends :ends} :active_license}]
     [:p.fs3.align-center (time/days-between ends)])))


(def render-end-of-term
  (table/wrap-cljs
   (fn [_ {{ends :ends} :active_license}]
     [:p.fs3 (format/date-short-num ends)])))


(defmulti columns (fn [role] role))


(defmethod columns :orders [_]
  [{:title "Member"
    :dataIndex :member
    :render render-name-from-account}
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
  [{:title "member"
    :dataindex :member
    :render render-name-from-account}
   {:title "payment description"
    :dataindex :description
    :render render-payment-description}
   {:title "amount"
    :dataindex :amount
    :render render-amount}
   {:title "status"
    :dataindex :status
    :render render-status}])


(defmethod columns :end-of-term [_]
  [{:title "member"
    :dataindex :member
    :render render-name}
   {:title "Property"
    :dataindex :property
    :render render-property}
   {:title "End of Term"
    :dataIndex :date
    :render render-end-of-term}
   {:title "Days Left"
    :dataindex :days-left
    :render render-days-left}])


(defn- notifications-table [type items title]
  [:div.column.is-half
   [:h3 title]
   [ant/table {:pagination {:page-size 5
                            :size      :small}
               :columns    (columns type)
               :dataSource (map #(assoc % :key (:id %)) items)}]])


(defn notifications []
  (let [payments (subscribe [:payments/by-status [:due :pending :failed]])
        orders   (subscribe [:orders])
        members  (subscribe [:overview.accounts/end-of-term])]
    [:div
     (.log js/console @members)
     [:div.columns
      [notifications-table :orders (sort-by :created > @orders) "Helping Hands"]
      [notifications-table :payments (sort-by :created > @payments) "Payments"]]
     [:div.columns
      [notifications-table :end-of-term @members "End of term"]]]))


(defn overview-content []
  [:div
   (typography/view-header "Overview" "Important updates on our communities")
   [notifications]])


;; ==============================================================================
;; entry ========================================================================
;; ==============================================================================

(defmethod content/view :overview [route]
  [overview-content])
