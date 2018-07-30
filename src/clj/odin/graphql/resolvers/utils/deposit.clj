(ns odin.graphql.resolvers.utils.deposit
  (:require [blueprints.models.account :as account]
            [blueprints.models.security-deposit :as deposit]
            [clojure.spec.alpha :as s]
            [teller.core :as teller]
            [teller.customer :as tcustomer]
            [teller.payment :as tpayment]
            [toolbelt.datomic :as td]))

(defn can-pay?
  [teller account]
  (some? (when-let [c (tcustomer/by-account teller account)]
           (tcustomer/can-pay? c))))

(s/fdef can-pay?
        :args (s/cat :teller teller/connection?
                     :deposit td/entityd?)
        :ret boolean?)


(defn is-refunded?
  "Has this deposit refund proccess been initiated or successful?"
  [deposit]
  (some?
   (#{:deposit.refund-status/successful
      :deposit.refund-status/initiated} (deposit/refund-status deposit))))

(s/fdef is-refunded?
        :args (s/cat :deposit td/entityd?)
        :ret boolean?)


(defn is-refundable?
  "Can this security deposit be refunded?"
  [teller deposit]
  (let [customer (tcustomer/by-account teller (deposit/account deposit))]
    (and (nil? (deposit/refund-status deposit))
         (not (empty? (deposit/payments deposit)))
         (let [charge-total (->> (deposit/payments deposit)
                                 (map (partial tpayment/by-entity teller))
                                 (filter #(tpayment/paid? %1))
                                 (reduce #(+ %1 (tpayment/amount %2)) 0))]
           (= (deposit/amount deposit) charge-total))
         (can-pay? teller (deposit/account deposit))
         (false? (is-refunded? deposit)))))

(s/fdef is-refundable?
        :args (s/cat :teller teller/connection?
                     :deposit td/entityd?)
        :ret boolean?)
