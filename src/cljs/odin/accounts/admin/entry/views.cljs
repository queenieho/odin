(ns odin.accounts.admin.entry.views
  (:require [odin.utils.formatters :as format]
            [re-frame.core :refer [subscribe dispatch]]
            [toolbelt.core :as tb]
            [iface.loading :as loading]
            [iface.typography :as typography]
            [antizer.reagent :as ant]
            [odin.components.payments :as payments-ui]))



(defmulti subheader :role)


(defmethod subheader :default [{:keys [role]}]
  [:span "Is a " [:b role]])


(defmethod subheader :member
  [{{unit :unit} :active_license, property :property}]
  [:span "Lives in " [:a {:href ""} (:name property)] " in room #" [:b (:number unit)]])


(defn contact-info [{:keys [email phone dob]}]
  [:div
   [:p.has-text-right.fs1
    [:a {:href (str "mailto:" email)} email]
    [ant/icon {:class "ml1" :type "mail"}]]
   (when-some [p phone]
     [:p.has-text-right.fs1
      (format/phone-number p)
      [ant/icon {:class "ml1" :type "phone"}]])
   (when-some [d dob]
     [:p.has-text-right.fs1
      (format/date-month-day d)
      [ant/icon {:class "ml1" :type "gift"}]])])


(defn view [{{account-id :account-id} :params}]
  (let [{:keys [email phone] :as account} @(subscribe [:account (tb/str->int account-id)])
        payments                          (subscribe [:payments/by-account-id (tb/str->int account-id)])
        is-loading                        (subscribe [:loading? :account/fetch])
        payments-loading                  (subscribe [:loading? :payments/fetch])]
    (if (or @is-loading (nil? account))
      (loading/fullpage :text "Fetching account...")
      [:div
       [:div.columns
        [:div.column.is-three-quarters
         (typography/view-header (:name account) (subheader account))]
        [:div.column [contact-info account]]]

       [:div.columns
        [:div.column
         ]
        [:div.column
         [:p.title.is-5 "Payments"]
         [ant/card {:class "is-flush"}
          [payments-ui/payments-table @payments @payments-loading]]]]])))
