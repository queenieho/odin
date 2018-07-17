(ns odin.graphql.resolvers.background
  (:require [blueprints.models.safety :as safety]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [datomic.api :as d]
            [toolbelt.datomic :as td]
            [toolbelt.core :as tb]
            [taoensso.timbre :as timbre]))


;; ==============================================================================
;; mutations ====================================================================
;; ==============================================================================


(defn create!
  "Create a new community-safety entity for an account."
  [{:keys [conn requester]} {:keys [account]} _]
  (let [account          (d/entity (d/db conn) account)
        background-check (safety/create account)]
    @(d/transact conn [background-check])
    (safety/by-account (d/db conn) account)))


;; ==============================================================================
;; queries ======================================================================
;; ==============================================================================


(defn query-by-account
  [{conn :conn} {id :id} _]
  (let [account (d/entity (d/db conn) id)]
    (safety/by-account (d/db conn) account)))


;; ==============================================================================
;; resolvers ====================================================================
;; ==============================================================================


(def resolvers
  {;; mutations
   :background/create!    create!
   ;; queries
   :background/by-account query-by-account})
