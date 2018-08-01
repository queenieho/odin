(ns odin.graphql.resolvers.deposit
  (:require [blueprints.models.event :as event]
            [blueprints.models.security-deposit :as deposit]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [datomic.api :as d]
            [odin.graphql.resolvers.utils :refer [error-message]]
            [odin.graphql.resolvers.utils.deposit :as utils-deposit]
            [taoensso.timbre :as timbre]
            [teller.customer :as tcustomer]
            [teller.payment :as tpayment]
            [toolbelt.datomic :as td]))

;; =============================================================================
;; Fields
;; =============================================================================


(defn amount-remaining
  [_ _ deposit]
  (deposit/amount-remaining deposit))


(defn amount-paid
  [_ _ deposit]
  (deposit/amount-paid deposit))


(defn amount-pending
  [_ _ deposit]
  (deposit/amount-pending deposit))


(defn payments
  [{teller :teller} _ deposit]
  (let [customer (tcustomer/by-account teller (deposit/account deposit))]
    (tpayment/query teller {:payment-types [:payment.type/deposit]
                            :customers     [customer]})))


(defn deposit-status
  [_ _ dep]
  (let [is-overdue (t/after? (t/now) (c/to-date-time (deposit/due dep)))]
    (cond
      (> (deposit/amount-pending dep) 0)                    :pending
      (= (deposit/amount dep) (deposit/amount-paid dep))    :paid
      (and is-overdue (> (deposit/amount-remaining dep) 0)) :overdue
      (= (deposit/amount-paid dep) 0)                       :unpaid
      :otherwise                                            :partial)))


(defn refund-status
  [_ _ deposit]
  (when-let [s (deposit/refund-status deposit)]
    (keyword (name s))))


(defn line-items
  [_ _ deposit]
  (deposit/line-items deposit))


(defn- sum-amount [items]
  (reduce
   (fn [sum item]
     (cond
       (number? (:price item))
       (+ sum (:price item))

       :else
       sum))
   0
   items))


(defn- refund-amount
  [deposit-amount charges credits]
  (let [charge-amount (sum-amount charges)
        credit-amount (sum-amount credits)
        refund-amount (-> deposit-amount
                          (- charge-amount)
                          (+ credit-amount))]
    (if (number? refund-amount)
      refund-amount
      0)))


(defn- create-associated-payments!
  [customer charges credits]
  (let [property        (tcustomer/property customer)
        charge-payments (doall
                         (map
                          (fn [{price :price}]
                            (tpayment/create! customer price :payment.type/deposit {:subtypes [:deposit-refund-charge]
                                                                                    :status   :payment.status/paid
                                                                                    :paid-on  (java.util.Date.)
                                                                                    :property property}))
                          charges))
        credit-payments (doall
                         (map
                          (fn [{price :price}]
                            (tpayment/create! customer price :payment.type/deposit {:subtypes [:deposit-refund-credit]
                                                                                    :status   :payment.status/paid
                                                                                    :paid-on  (java.util.Date.)
                                                                                    :property property}))
                          credits))]
    (concat charge-payments credit-payments)))


(defn- line-item-data
  [line-items subtype]
  (map
   (fn [{desc :desc price :price types :types}]
     {:line-item/desc  desc
      :line-item/price price
      :line-item/types types
      :line-item/subtypes subtype})
   line-items))


(defn refund!
  [{:keys [teller conn] :as ctx} {:keys [deposit_id charges credits] :as params} _]
  (let [deposit (d/entity (d/db conn) deposit_id)]
    (if (nil? deposit)
      (resolve/resolve-as nil {:message "Member does not have a security deposit"})
      (let [account  (deposit/account deposit)
            customer (tcustomer/by-account teller account)
            amount   (refund-amount (deposit/amount deposit) charges credits)]
        (cond
          (utils-deposit/is-refunded? deposit)
          (resolve/resolve-as nil {:message "Member has already been refunded their security deposit"})

          (not (tcustomer/can-pay? customer))
          (resolve/resolve-as nil {:message "Member does not have a payout account."})

          (> amount (deposit/amount deposit))
          (resolve/resolve-as nil {:message "Refund amount is above security deposit."})

          (> 0 amount)
          (resolve/resolve-as nil {:message "Refund amount is below security deposit."})

          :else
          (try
            (when-let [payment (tcustomer/pay! customer amount :payment.type/deposit {:subtypes   [:deposit-refund]
                                                                                      :absorb-fee true})]
              (let [assoc-payments (create-associated-payments! customer charges credits)]
                (->> [{:db/id                 deposit_id
                       :deposit/refund-status :deposit.refund-status/successful
                       :deposit/lines         (concat
                                               (line-item-data charges :refund-charge)
                                               (line-item-data credits :refund-credit))}
                      {:db/id              (td/id payment)
                       :payment/associated (map
                                            #(td/id %)
                                            assoc-payments)}
                      (event/job :deposit/refund {:params {:deposit-id deposit_id
                                                           :account-id (td/id account)}})]
                     (d/transact conn)
                     (deref)))
              (d/entity (d/db conn) deposit_id))
            (catch Throwable t
              (timbre/error t ::refund! {:email (tcustomer/email customer)})
              (resolve/resolve-as nil {:message (error-message t)}))))))))

;; =============================================================================
;; Resolvers
;; =============================================================================


(def resolvers
  {;;fields
   :deposit/amount-remaining amount-remaining
   :deposit/amount-paid      amount-paid
   :deposit/amount-pending   amount-pending
   :deposit/payments         payments
   :deposit/refund-status    refund-status
   :deposit/status           deposit-status
   :deposit/refund!          refund!
   :deposit/line-items       line-items})
