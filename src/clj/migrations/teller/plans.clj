(ns migrations.teller.plans
  (:require [blueprints.models.member-license :as ml]
            [blueprints.models.service :as service]
            [datomic.api :as d]
            [teller.plan :as plan]
            [toolbelt.datomic :as td]))

(defn- query-services [db]
  (->> (service/query db {:billed [:service.billed/monthly]
                          :active true})
       ;; only services without a plan and with a price
       (filter #(and (nil? (service/plan %)) (some? (service/price %))))))


(defn- create-plan! [teller service]
  (plan/create! teller (service/name service) :payment.type/order (service/price service)))


(defn- attach-plans [teller services]
  (reduce
   (fn [acc service]
     (let [plan (create-plan! teller service)]
       (conj acc [:db/add (td/id service) :service/plan (td/id plan)])))
   nil
   services))


(defn ^{:added "1.10.0"} attach-plans-to-subscription-services
  "Creates a plan for all active subscription services that do not currently have
  one."
  [teller conn]
  (let [services (query-services (d/db conn))]
    (when-let [tx-data (attach-plans teller services)]
      @(d/transact conn tx-data))))


(comment

  (do
    (def conn odin.datomic/conn)
    (def teller odin.teller/teller)

    (defn t [id]
      (d/touch (d/entity (d/db conn) id))))

  (def sample-mlicense
    (ml/active (d/db conn) [:account/email "member@test.com"]))

  (attach-plans-to-subscription-services teller conn)

  )
