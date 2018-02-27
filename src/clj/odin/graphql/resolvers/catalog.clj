(ns odin.graphql.resolvers.catalog
  (:require [datomic.api :as d]
            [blueprints.models.catalogue :as catalogue]))


(defn query [{conn :conn} _ _]
  (->> (d/q '[:find [?c ...]
              :where
              [?c :catalogue/code _]]
            (d/db conn))
       (map (partial d/entity (d/db conn)))))


(def resolvers
  {:catalog/list query})
