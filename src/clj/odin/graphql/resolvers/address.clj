(ns odin.graphql.resolvers.address
  (:require [blueprints.models.account :as account]
            [blueprints.models.address :as address]
            [blueprints.models.source :as source]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [odin.graphql.authorization :as authorization]))


;; ==============================================================================
;; mutations ====================================================================
;; ==============================================================================


(defn- parse-address-params [params]
  params)


(defn create!
  "Create a new address"
  [{:keys [conn requester]} {{:keys [lines locality region country postal-code]} :params} _]
  (let [address-tx (address/create lines locality region country postal-code)]
    @(d/transact conn [address-tx
                       (source/create requester)])
    ;; TODO return this entity
    ;; Need to figure out how do we want to return this entity?
    ))


;; ==============================================================================
;; resolvers ====================================================================
;; ==============================================================================


#_(defmethod authorization/authorized :address/create! [_ account _]
  (account/admin? account))


(def resolvers
  {;; mutations
   :address/create! create!})


(comment

  (create! {} {:params {:lines       "414 Bryant St"
                        :locality    "San Francisco"
                        :region      "CA"
                        :country     "USA"
                        :postal-code "94107"}} "")

  )
