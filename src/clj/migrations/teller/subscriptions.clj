(ns migrations.teller.subscriptions
  (:require [blueprints.models.member-license :as ml]
            [blueprints.models.order :as order]
            [blueprints.models.service :as service]
            [clj-time.coerce :as c]
            [datomic.api :as d]
            [odin.graphql.resolvers.utils.plans :as plans-utils]
            [stripe.plan :as splan]
            [stripe.subscription :as ssub]
            [teller.core :as teller]
            [teller.customer :as tcustomer]
            [teller.plan :as tplan]
            [teller.subscription :as tsubscription]
            [toolbelt.datomic :as td]
            [odin.graphql.resolvers.utils.autopay :as autopay-utils]))

(defn- license-query
  [db]
  (d/q '[:find [?l ...]
         :in $
         :where
         [?l :member-license/subscription-id _]
         [?l :member-license/status :member-license.status/active]]
       db))


(defn migrate-autopay-license
  [teller license]
  (let [plan-name    (plans-utils/plan-name teller license)
        license-rate (ml/rate license)
        customer     (tcustomer/by-account teller (ml/account license))
        connect-id   (-> license (ml/property) :property/rent-connect-id)
        plan         (tplan/create! teller plan-name :payment.type/rent license-rate)
        source       (first (tcustomer/sources customer :payment-source.type/bank))
        subs         (tsubscription/subscribe! customer plan
                                               {:source   source
                                                :start-at (autopay-utils/autopay-start customer)})]
    (ssub/cancel! (:member-license/subscription-id license) {}
                  {:token   (teller/py teller)
                   :account connect-id})
    (splan/delete! (:member-license/plan-id license)
                   {:token   (teller/py teller)
                    :account connect-id})
    (->> [{:db/id                        (td/id subs)
           :teller-subscription/payments (->> (ml/payments license)
                                              (filter :stripe/invoice-id)
                                              (map td/id))}
          [:db/retract (td/id license) :member-license/subscription-id (:member-license/subscription-id license)]
          [:db/retract (td/id license) :member-license/plan-id (:member-license/plan-id license)]]
         (d/transact (teller/db teller))
         (deref))))



(defn migrate-order-subscription
  [teller order]
  (let [customer   (tcustomer/by-account teller (order/account order))
        source     (first (tcustomer/sources customer :payment-source.type/card))
        service    (order/service order)
        plan       (tplan/by-entity teller (service/plan service))
        sub        (ssub/fetch (:stripe/subs-id order)
                               {:token (teller/py teller)})
        period-end (c/to-date (c/from-epoch (:current_period_end sub)))
        subs       (tsubscription/subscribe! customer plan
                                             {:start-at period-end
                                              :source   source})]
    (ssub/cancel! (:stripe/subs-id order) {} {:token (teller/py teller)})
    (splan/delete! (get-in sub [:items :data 0 :plan :id]) {:token (teller/py teller)})
    (->> [{:db/id              (td/id order)
           :order/subscription (td/id subs)}
          {:db/id                        (td/id subs)
           :teller-subscription/payments (map td/id (order/payments order))}]
         (d/transact (teller/db teller))
         (deref))))


(defn migrate-all-licenses-on-autopay
  [teller conn]
  (let [licenses (license-query (d/db conn))]
    (doseq [license licenses]
      (migrate-autopay-license teller (d/entity (d/db conn) license)))))


;; (defn migrate-historical-order-subscriptions
;;   [teller]
;;   ;; FUTURE
;;   (ssub/fetch-all))


(comment

  (do
    (def conn odin.datomic/conn)
    (def teller odin.teller/teller)

    (defn t [id]
      (d/touch (d/entity (d/db conn) id))))


  (migrate-all-licenses-on-autopay teller conn)


  (migrate-order-subscription teller sample-order)


  )
