(ns odin.graphql.resolvers.license-term
  (:require [blueprints.models.account :as account]
            [blueprints.models.license :as license]
            [datomic.api :as d]
            [odin.graphql.authorization :as authorization]))


;; ==============================================================================
;; resolvers ====================================================================
;; ==============================================================================


;; fields =======================================================================


(defn available
  [_ _ license]
  (boolean (:license/available license)))


;; queries ======================================================================


(defn license-terms
  [{:keys [conn]} {only_available :only_available} _]
  (if only_available
    (license/available (d/db conn))
    (->> (d/q '[:find [?l ...]
                :where
                [?l :license/term _]]
              (d/db conn))
         (map (partial d/entity (d/db conn))))))


;; authorization ================================================================


;; only admins can query non-available license terms
(defmethod authorization/authorized? :license-term/list
  [_ account {:keys [only_available]}]
  (or (account/admin? account) (true? only_available)))


;; linking ======================================================================


(def resolvers
  {;; fields
   :license-term/available available
   ;; queries
   :license-term/list      license-terms})
