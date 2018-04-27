(ns odin.graphql.resolvers.utils.plans
  (:require [blueprints.models.member-license :as member-license]
            [blueprints.models.account :as account]
            [blueprints.models.unit :as unit]
            [teller.customer :as tcustomer]
            [teller.property :as tproperty]))


(defn plan-name
  [teller license]
  (let [account       (member-license/account license)
        email         (account/email account)
        unit-name     (unit/code (member-license/unit license))
        customer      (tcustomer/by-account teller account)
        property      (tcustomer/property customer)
        property-name (tproperty/name property)]
    (str "autopay for " email " @ " property-name " in " unit-name)))
