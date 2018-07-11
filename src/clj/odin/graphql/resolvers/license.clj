(ns odin.graphql.resolvers.license
  (:require [blueprints.models.license :as license]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [datomic.api :as d]
            [odin.graphql.authorization :as authorization]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]
            [taoensso.timbre :as timbre]))


;; ==============================================================================
;; fields =======================================================================
;; ==============================================================================


(defn term
  [_ _ license]
  (license/term license))


;; ==============================================================================
;; queries ======================================================================
;; ==============================================================================


(defn- query-available-licenses
  [db]
  (license/available db))


(defn query-available
  [{conn :conn} _ _]
  (try
    (query-available-licenses (d/db conn))
    (catch Throwable t
      (timbre/error t "error querying licenses")
      (resolve/resolve-as nil {:message  (.getMessage t)
                               :err-data (ex-data t)}))))


(defn query-license-by-term
  [db term]
  (license/by-term db term))


(defn query-by-term
  [{conn :conn} {term :term} _]
  (try
    (query-license-by-term (d/db conn) term)
    (catch Throwable t
      (timbre/error t "error querying licenses")
      (resolve/resolve-as nil {:message  (.getMessage t)
                               :err-data (ex-data t)}))))


(defn entry
  "Look up a license by id"
  [{conn :conn} {id :id} _]
  (d/entity (d/db conn) id))


;; ==============================================================================
;; resolvers ====================================================================
;; ==============================================================================


(def resolvers
  {;; fields
   :license/term            term
   ;; queries
   :license/query-available query-available
   :license/query-by-term   query-by-term
   :license/entry           entry})
