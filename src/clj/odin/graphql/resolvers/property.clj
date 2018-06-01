(ns odin.graphql.resolvers.property
  (:require [blueprints.models.license :as license]
            [blueprints.models.property :as property]
            [blueprints.models.source :as source]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [odin.graphql.authorization :as authorization]
            [blueprints.models.account :as account]
            [teller.property :as tproperty]))

;; ==============================================================================
;; fields =======================================================================
;; ==============================================================================


(defn license-prices
  [_ _ property]
  (let [ps (:property/licenses property)]
    (filter
     #(let [license (:license-price/license %)]
        (or (:license/available license)
            (nil? (:license/available license))))
     ps)))


(defn tours
  "Is touring enabled?"
  [_ _ property]
  (boolean (:property/tours property)))


(defn has-financials
  "Do we have financial information?"
  [{:keys [teller]} _ property]
  (boolean (:entity (tproperty/by-community teller property))))


;; ==============================================================================
;; mutations ====================================================================
;; ==============================================================================


(defn update-existing [unit term rate]
  (when-let [lp (tb/find-by (comp #{term} :license/term :license-price/license)
                            (:property/licenses unit))]
    {:db/id               (:db/id lp)
     :license-price/price rate}))


(defn create-new [db unit term rate]
  {:db/id         (:db/id unit)
   :unit/licenses {:license-price/price   rate
                   :license-price/license (:db/id (license/by-term db term))}})


(def allowed-term?
  #{3 6 12})


(defn set-rate!
  "Set the rate for the property for the given term."
  [{:keys [conn requester]} {:keys [id term rate]} _]
  (when-not (allowed-term? term)
    (resolve/resolve-as nil {:message (format "'%s' is not a valid term length!")
                             :term    term}))
  (let [property (d/entity (d/db conn) id)]
    @(d/transact conn [(or (update-existing property term rate)
                           (create-new property term rate))
                       (source/create requester)])
    (d/entity (d/db conn) id)))


(defn toggle-touring!
  "Toggle a property's `:property/tours` attribute on/off."
  [{:keys [conn requester]} {:keys [id]} _]
  (let [property (d/entity (d/db conn) id)]
    @(d/transact conn [[:db/add id :property/tours (not (:property/tours property))]])
    (d/entity (d/db conn) id)))


(defn- parse-license-prices [db license-prices]
  (->> (map
        (fn [lprices]
          (tb/transform-when-key-exists lprices
            {:term  #(:db/id (license/by-term db %))
             :price #(float %)}))
        license-prices)
       (filter :term)))


(defn- parse-create-params [db {:keys [address] :as params}]
  (tb/transform-when-key-exists params
    {:units          #(property/create-units (:code params) %)
     :license_prices #(property/create-license-prices (parse-license-prices db %))
     :address        (fn [{:keys [lines locality region country postal_code]}]
                       (property/create-address lines locality region country postal_code))}))


(defn create!
  "Create a new community"
  [{:keys [conn requester]} {params :params} _]
  (let [{:keys [name code units address available_on license_prices]}
        (parse-create-params (d/db conn) params)]
    @(d/transact conn [(property/create name code units available_on license_prices
                                        :address address)
                       (source/create requester)])
    (d/entity (d/db conn) [:property/code code])))


;; ==============================================================================
;; queries ======================================================================
;; ==============================================================================


(defn entry
  "Look up a single property by id."
  [{conn :conn} {id :id} _]
  (d/entity (d/db conn) id))


(defn query
  [{conn :conn} _ _]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :property/code _]]
            (d/db conn))
       (map (partial d/entity (d/db conn)))))


;; ==============================================================================
;; resolvers ====================================================================
;; ==============================================================================


(defmethod authorization/authorized? :property/create! [_ account _]
  (account/admin? account))


(defmethod authorization/authorized? :property/set-rate! [_ account _]
  (account/admin? account))


(defmethod authorization/authorized? :property/toggle-touring! [_ account _]
  (account/admin? account))


(def resolvers
  {;; fields
   :property/license-prices  license-prices
   :property/tours           tours
   :property/has-financials  has-financials
   ;; mutations
   :property/create!         create!
   :property/set-rate!       set-rate!
   :property/toggle-touring! toggle-touring!
   ;; queries
   :property/entry           entry
   :property/query           query})
