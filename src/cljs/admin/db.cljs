(ns admin.db
  (:require [admin.accounts.db :as accounts]
            [admin.overview.db :as overview]
            [admin.kami.db :as kami]
            [admin.license-terms.db :as license-terms]
            [admin.metrics.db :as metrics]
            [admin.notes.db :as notes]
            [admin.profile.db :as profile]
            [admin.properties.db :as properties]
            [admin.services.db :as services]
            [iface.modules.payments :as payments]
            [iface.modules.loading :as loading]))


(defn bootstrap [account]
  (merge
   {:lang    :en
    :menu    {:showing false
              :items   [{:key  :overview
                         :name "Overview"
                         :uri  "/overview"}
                        {:key  :accounts
                         :name "People"
                         :uri  "/accounts"}
                        {:key  :properties
                         :name "Communities"
                         :uri  "/properties"}
                        {:key  :services
                         :name "Helping Hands"
                         :uri  "/services"}
                        {:key  :metrics
                         :name "Metrics"
                         :uri  "/metrics"}
                        {:key  :kami
                         :name "Kami"
                         :uri  "/kami"}]}
    :account account
    :route   {:page      :home
              :path      [:home]
              :params    {}
              :requester account}}
   loading/db
   accounts/default-value
   kami/default-value
   license-terms/default-value
   metrics/default-value
   notes/default-value
   payments/default-value
   profile/default-value
   properties/default-value
   services/default-value
   overview/default-value))
