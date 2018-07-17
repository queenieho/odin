(ns odin.graphql.resolvers.background
  (:require [blueprints.models.safety :as safety]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [datomic.api :as d]
            [toolbelt.datomic :as td]
            [toolbelt.core :as tb]
            [taoensso.timbre :as timbre]))


;; ==============================================================================
;; queries ======================================================================
;; ==============================================================================


(defn query-by-account
  [{conn :conn} {id :id} _]
  (timbre/info "\n\n\n Account here: " id)
  (->> (d/q '[:find [?e ...]
              :in $ ?a
              :where
              [?e :community-safety/account ?a]]
            (d/db conn) id)
       (map #(d/entity (d/db conn) %))))


;; ==============================================================================
;; resolvers ====================================================================
;; ==============================================================================


(def resolvers
  {;; queries
   :background/by-account query-by-account})
