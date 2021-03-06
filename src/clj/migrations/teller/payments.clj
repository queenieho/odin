(ns migrations.teller.payments
  (:require [clojure.core.async :as a :refer [go <! >! chan]]
            [teller.payment :as tpayments]
            [odin.config :as config :refer [config]]
            [datomic.api :as d]
            [teller.core :as teller]
            [teller.customer :as tcustomer]
            [toolbelt.datomic :as td]
            [stripe.charge :as charge]
            [toolbelt.core :as tb]
            [toolbelt.async :refer [<!?]]
            [taoensso.timbre :as timbre]))


(defn add-ids-to-payments
  [conn]
  (let [eids (d/q '[:find [?e ...]
                    :in $
                    :where
                    [?e :payment/amount _]
                    [(missing? $ ?e :payment/id)]]
                  (d/db conn))]
    (->> (map #(vector :db/add % :payment/id (d/squuid)) eids)
         (d/transact conn)
         (deref))))


(defn add-payment-method-other-to-missing-method-payments
  [conn]
  (let [eids (d/q '[:find [?e ...]
                    :in $
                    :where
                    [?e :payment/amount _]
                    [(missing? $ ?e :payment/method)]
                    [(missing? $ ?e :payment/check)]]
                  (d/db conn))]
    (->> (map #(vector :db/add % :payment/method :payment.method/other) eids)
         (d/transact conn)
         (deref))))


(defn all-payments-not-checks [db]
  (->> (d/q '[:find [?e ...]
              :in $
              :where
              [?e :payment/amount _]
              (not [?e :payment/method :payment.method/other])
              [(missing? $ ?e :payment/check)]]
            db)
       (map (partial d/entity db))))


(defn source-for-payment
  [teller payment out-ch]
  (go
    (let [account  (:payment/account payment)
          customer (:entity (tcustomer/by-account teller account))
          type     (:payment/for payment)]
      (try
        (if (#{:payment.type/deposit :payment.type/rent} type)
          (>! out-ch
              (td/id
               (tb/find-by
                (comp (partial = :payment-source.type/bank) :payment-source/type)
                (:customer/payment-sources customer))))
          (let [charge    (<!? (charge/fetch (:stripe/charge-id payment)
                                             {:token  (config/stripe-secret-key config)
                                              :out-ch (chan 1)}))
                source-id (get-in charge [:source :id])]
            (>! out-ch [:payment-source/id source-id])))
        (catch Throwable t
          (timbre/error t "Yikes!")
          :nada)))
    (a/close! out-ch)))


(defn- <fetch-sources
  [teller payments]
  (let [concurrency 10
        in          (chan)
        out         (chan)]
    (a/pipeline-async concurrency out (partial source-for-payment teller) in)
    (a/onto-chan in payments)
    (a/into [] out)))


(defn enrich-payments-with-teller-stuff
  [teller conn]
  (let [payments (all-payments-not-checks (d/db conn))]
    (->> (mapv
          (fn [payment source-id]
            (let [account  (:payment/account payment)
                  customer (:entity (tcustomer/by-account teller account))]
              (assert (some? account)
                      (format "Payment does not have an account %s" (td/id payment)))
              (assert (some? customer)
                      (format "Payment ID: %s\nAccount does not have a customer %s"
                              (td/id payment) (td/id account)))
              (tb/assoc-when
               {:db/id            (td/id payment)
                :payment/customer (td/id customer)
                :payment/source   source-id}
               :payment/charge-id  (:stripe/charge-id payment)
               :payment/invoice-id (:stripe/invoice-id payment))))
          payments
          (a/<!! (<fetch-sources teller payments)))
         (d/transact conn)
         (deref))))


(comment

  (defn t [id]
    (d/touch (d/entity (d/db conn) id)))

  (do
    (def conn odin.datomic/conn)
    (def teller odin.teller/teller))

  (do
    (add-payment-method-other-to-missing-method-payments conn)

    (add-ids-to-payments conn)

    )

  (enrich-payments-with-teller-stuff teller conn)


  ;; any without ids?
  (d/q '[:find ?p
         :in $
         :where
         [?p :payment/amount _]
         [(missing? $ ?p :payment/id)]]
       (d/db conn))

  ;; without types?
  (d/q '[:find ?p
         :in $
         :where
         [?p :payment/amount _]
         [(missing? $ ?p :payment/type)]]
       (d/db conn))


  )
