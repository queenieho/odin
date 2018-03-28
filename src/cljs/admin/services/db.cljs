(ns admin.services.db
  (:require [admin.routes :as routes]
            [admin.services.orders.db :as orders-db]))



(def path ::path)

(def form-defaults {:name        ""
                    :description ""
                    :code        ""
                    :properties  []
                    :catalogs    []
                    :active      false
                    :price       0.0
                    :cost        0.0
                    :billed      :once
                    :rental      false
                    :fields      []})

(def default-value
  (merge {path {:from        ""
                :to          ""
                :service-id  nil
                :search-text ""
                :is-editing  false
                :form        form-defaults}}
         orders-db/default-value))
