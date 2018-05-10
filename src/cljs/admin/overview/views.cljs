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


(defmulti notification (fn [type] type))


(defmethod notification :orders [_ {:keys [account status created id] :as order}]
  [:div.columns
   [:div.column.is-1
    [ant/avatar (formatters/initials (:name account))]]
   [:div.column.is-11
    [:p [:a
         {:href (routes/path-for :accounts/entry :account-id (:id account))}
         (:name account)]
     " ordered "
     [:a
      {:href (routes/path-for :services.orders/entry :order-id id)}
      (:name order)]]
    [:p.fs1 (format/date-short created) " - " (string/capitalize (name status))]]])


(defmethod notification :payments [_ {:keys [amount description account status] :as payment}]
  [:div.columns
   [:div.column.is-1
    [ant/avatar (formatters/initials (:name account))]]
   [:div.column.is-11
    [:p
     [:a
      {:href (routes/path-for :accounts/entry :account-id (:id account))}
      (:name account)]
     "'s payment for "
     [:b description] " is " [:b status]]
    [:p.fs1 (format/currency amount)]]])


(defmethod notification :end-of-term [_ {:keys [id property name active_license] :as member}]
  [:div.columns
   [:div.column.is-1
    [ant/avatar (formatters/initials name)]]
   [:div.column.is-11
    [:p
     [:a {:href (routes/path-for :accounts/entry :account-id id)} name]
     " is " [:b (time/days-between (:ends active_license)) " days"] " from end of term"]
    [:p.fs1
     [:a
      {:href (routes/path-for :properties/entry :property-id (:id property))}
      (:name property)]
     " - End of term: " (format/date-short (:ends active_license))]]])


(defmethod notification :move-out [_ {:keys [property name active_license id] :as member}]
  (let [{[date pre_walk] :move_out} active_license]
    [:div.columns
     [:div.column.is-1
      [ant/avatar (formatters/initials name)]]
     [:div.column.is-11
      [:p
       [:a {:href (routes/path-for :accounts/entry :account-id id)} name]
       " is moving out on " [:b (format/date-short date)]]
      [:p.fs1
       [:a
        {:href (routes/path-for :properties/entry :property-id (:id property))}
        (:name property)]
       " - Pre-walkthrough " (format/date-short pre_walk)]]]))


(defn- empty-view-message [type]
  (case type
    :orders      "No active orders at the moment"
    :payments    "Hurray! Everyone's paid!"
    :end-of-term "No members near their end of term"
    :move-out    "No members moving out"
    "Nothing to see here"))


(defn- empty-view [type]
  [:div
   [:p.fs3 (empty-view-message type)]])


(defn- notifications-table [type items title]
  (let [state (r/atom {:current 1})]
    (fn [type items]
      (let [{:keys [current]} @state
            items'            (->> (drop (* (dec current) 5) items)
                                   (take 5))]
        [:div.column.is-half
         [ant/card
          {:no-hovering true
           :title       title
           ;; :extra       (r/as-element [:a [ant/icon {:type "arrows-alt"}]])
           :loading     @(subscribe [:overview/loading])}
          (if (not-empty items)
            (map #(with-meta [notification type %] {:key (:id %)}) items')
            [empty-view type])
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
