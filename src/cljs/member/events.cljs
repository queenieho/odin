(ns member.events
  (:require [iface.components.notifications :as notifs]
            [iface.modules.graphql :as graphql]
            [iface.utils.time :as t]
            [member.db :as db]
            [member.routes :as routes]
            [member.profile.events]
            [member.services.events]
            [re-frame.core :refer [reg-event-db reg-event-fx path]]
            [iface.utils.formatters :as format]
            [antizer.reagent :as ant]))


(graphql/configure
 "/api/graphql"
 {:on-unauthenticated (fn [_]
                        {:route "/logout"})
  :on-error-fx        (fn [[k _]]
                        {:dispatch [:ui/loading k false]})})


(reg-event-fx
 :app/init
 (fn [_ [_ account]]
   {:db       (db/bootstrap account)
    :dispatch [::fetch-membership-status (:id account)]}))


(reg-event-fx
 ::fetch-membership-status
 (fn [{:keys [db]} [_ account-id]]
   {:graphql {:query      [[:account {:id account-id}
                            [:refundable :payout_account
                             [:deposit [:id :due :amount :amount_remaining :amount_paid :amount_pending]]
                             [:active_license [[:payments [:amount :status :due]]]]]]]
              :on-success [::fetch-success]
              :on-failure [:graphql/failure]}}))


(defn- rent-due-message [{due :due}]
  (let [is-overdue (.isAfter (js/moment.) (js/moment. due))]
    (notifs/create :rent-due
                   [:span
                    [:b (format/format "Your rent for this month is %s"
                                       (if is-overdue "overdue!" "due."))]
                    " Go to your " [:u "Membership"] " page to pay your rent."]
                   (routes/path-for :profile/membership)
                   (if is-overdue :danger :warning))))


(defn- deposit-overdue-message [{due :due}]
  (let [is-overdue (.isAfter (js/moment.) (js/moment. due))]
    (notifs/create :deposit-overdue
                   [:span
                    [:b "Your security deposit is overdue!"]
                    " Go to your " [:u "Membership"] " page to pay your deposit."]
                   (routes/path-for :profile/membership)
                   :danger)))


(defn- payout-account-missing-message []
  (notifs/create :payout-account-missing
                 [ant/tooltip {:title "We can send your deposit directly to your
                  bank when we know how much to return, but we’ll need some more
                  info first."}
                  [:span
                   [:b "We need a little more info."]
                   " Can you go to " [:u "Payment Methods"] " and click “Add
                   Deposit Info”? Thanks!"]]
                 (routes/path-for :profile.payment/sources)
                 :warning))


(defn- messages [{:keys [account] :as data}]
  (let [due-payments (->> (get-in account [:active_license :payments])
                          (filter #(= (:status %) :due)))
        deposit      (:deposit account)]
    (cond-> []
      (not (empty? due-payments))
      (conj (rent-due-message (first due-payments)))

      (and (> (:amount_remaining deposit) 0) (t/is-before-now (:due deposit)))
      (conj (deposit-overdue-message deposit))

      (false? (:payout_account account))
      (conj (payout-account-missing-message)))))


(reg-event-fx
 ::fetch-success
 (fn [_ [_ response]]
   #_(update db :messages concat (messages (:data response)))
   {:dispatch [:iface.components.notifications/create (messages (:data response))]}))



(reg-event-db
 :layout.mobile-menu/toggle
 (fn [db _]
   (update-in db [:menu :showing] not)))


(reg-event-db
 :user/update
 (fn [db [_ data]]
   (update db :account merge data)))
