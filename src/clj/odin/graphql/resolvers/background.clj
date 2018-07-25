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


(defn update!
  [{conn :conn} {:keys [background_check_id params]} _]
  (let [background-check    (d/entity (d/db conn) background_check_id)
        background-check-tx (tb/assoc-some
                             {:db/id (td/id background-check)}
                             :community-safety/consent-given? (:consent params)
                             :community-safety/wants-report? (:wants_report params)
                             :community-safety/report-url (:report_url params))]
    @(d/transact conn [background-check-tx])
    (d/entity (d/db conn) (td/id background-check))))


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
   :background/update!    update!
   ;; queries
   :background/by-account query-by-account})
