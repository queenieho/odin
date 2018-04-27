(ns odin.graphql.resolvers.utils
  (:require [clojure.spec.alpha :as s]
            [ribbon.core :as ribbon]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]
            [teller.core :as teller]
            [blueprints.models.member-license :as member-license]
            [blueprints.models.account :as account]
            [blueprints.models.unit :as unit]
            [teller.customer :as tcustomer]
            [teller.property :as tproperty]))


(s/def ::conn td/conn?)
(s/def ::requester td/entityd?)
(s/def ::stripe ribbon/conn?)
(s/def ::config map?)
(s/def ::teller teller/connection?)


(s/def ::ctx
  (s/keys :req-un [::stripe ::requester ::conn ::config ::teller]))


(defn context? [x]
  (s/valid? ::ctx x))


(defn context
  "Construct a new context map."
  [conn requester stripe config teller]
  {:conn      conn
   :requester requester
   :stripe    stripe
   :config    config
   :teller    teller})

(s/fdef context
        :args (s/cat :conn ::conn
                     :requester ::requester
                     :stripe ::stripe
                     :config ::config
                     :teller ::teller)
        :ret ::ctx)


;; misc =================================


(defn plan-name
  [teller license]
  (let [account       (member-license/account license)
        email         (account/email account)
        unit-name     (unit/code (member-license/unit license))
        customer      (tcustomer/by-account teller account)
        property      (tcustomer/property customer)
        property-name (tproperty/name property)]
    (str "autopay for " email " @ " property-name " in " unit-name)))


;; errors ===============================


(defn error-message [t]
  (or (:message (ex-data t)) (.getMessage t) "Unknown error!"))

(s/fdef error-message
        :args (s/cat :throwable tb/throwable?)
        :ret string?)
