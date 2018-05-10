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
            [toolbelt.core :as tb]
            [iface.utils.formatters :as formatters]))


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


(defmulti notification (fn [type] type))


(defmethod notification :orders [_ {:keys [account name status created] :as order}]
  [:div.columns
   [:div.column.is-1
    [ant/avatar (formatters/initials (:name account))]]
   [:div.column.is-11
    [:p [:b (:name account)] " ordered "
     [:b name]]
    [:p.fs1 (format/date-short created) " - " status]]])


(defmethod notification :payments [_ {:keys [amount description account status] :as payment}]
  [:div.columns
   [:div.column.is-1
    [ant/avatar (formatters/initials (:name account))]]
   [:div.column.is-11
    [:p "Payment of " [:b amount] " for "
     [:b description] " is " [:b status]]
    [:p.fs1 (:name account)]]])


(defmethod notification :end-of-term [_ {:keys [property name active_license] :as member}]
  [:div.columns
   [:div.column.is-1
    [ant/avatar (formatters/initials name)]]
   [:div.column.is-11
    [:p [:b name] " is " [:b (time/days-between (:ends active_license)) " days"] " from end of term"]
    [:p.fs1 (:name property) " - End of term: " (format/date-short (:ends active_license))]]])


(defmethod notification :move-out [_ {:keys [property name active_license] :as member}]
  (let [{[date pre_walk] :move_out} active_license]
    [:div.columns
     [:div.column.is-1
      [ant/avatar (formatters/initials name)]]
     [:div.column.is-11
      [:p [:b name] " is moving out on " [:b (format/date-short (get-in active_license [:move_out :date]))]]
      [:p.fs1 (:name property) " - Pre-walkthrough " (format/date-short (get-in active_license [:move_out :pre_walk]))]]]))


(defn- notifications-table [type items title]
  (let [state (r/atom {:current 1})]
    (fn [type items]
      (let [{:keys [current]} @state
            items'            (->> (drop (* (dec current) 5) items)
                                   (take 5))]
        [:div.column.is-half
         [ant/card
          {:title title}
          (map #(with-meta [notification type %] {:key (:id %)}) items')
          [ant/pagination {:page-size 5
                           :size      :small
                           :current   current
                           :total     (count items)
                           :on-change #(swap! state assoc :current %)}]]]))))


(defn notifications []
  (let [payments (subscribe [:payments/by-status [:due :pending :failed]])
        orders   (subscribe [:orders])
        members  (subscribe [:overview.accounts/end-of-term])
        move-out (subscribe [:overview.accounts/move-out])]
    [:div
     (.log js/console @move-out)
     [:div.columns
      [notifications-table :orders (sort-by :created > @orders) "Helping Hands"]
      [notifications-table :payments (sort-by :created > @payments) "Payments"]]
     [:div.columns
      [notifications-table :end-of-term @members "End of term"]
      [notifications-table :move-out @move-out "Moving out"]]]))


(defn overview-content []
  [:div
   (typography/view-header "Overview" "Important updates on our communities")
   [notifications]])


;; ==============================================================================
;; entry ========================================================================
;; ==============================================================================

(defmethod content/view :overview [route]
  [overview-content])
