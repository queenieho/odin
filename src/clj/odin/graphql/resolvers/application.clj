(ns odin.graphql.resolvers.application
  (:require [blueprints.models.account :as account]
            [blueprints.models.application :as application]
            [blueprints.models.approval :as approval]
            [blueprints.models.events :as events]
            [blueprints.models.license :as license]
            [blueprints.models.source :as source]
            [clj-time.core :as t]
            [clojure.set :as set]
            [clojure.string :as string]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [datomic.api :as d]
            [ring.util.response :as response]
            [taoensso.timbre :as timbre]
            [teller.customer :as customer]
            [teller.payment :as payment]
            [toolbelt.datomic :as td]
            [toolbelt.core :as tb]))


;; ==============================================================================
;; fields -----------------------------------------------------------------------
;; ==============================================================================


(defn account
  [_ _ application]
  (application/account application))


(defn approved-by
  [_ _ application]
  (when-let [approval (approval/by-account (application/account application))]
    (approval/approver approval)))


(defn approved-at
  [{conn :conn} _ application]
  (when (application/approved? application)
    (d/q '[:find ?t .
           :in $ ?app
           :where
           [?app :application/status :application.status/approved ?tx]
           [?tx :db/txInstant ?t]]
         (d/db conn) (td/id application))))


(defn status
  [_ _ application]
  (let [status (application/status application)]
    (if (= status :application.status/in-progress)
      :in_progress
      (keyword (name status)))))


(defn term
  [{conn :conn} _ application]
  (when-let [license (application/desired-license application)]
    (license/term license)))


(defn last-updated
  [{conn :conn} _ application]
  (->> [application
        (application/community-fitness application)
        (application/pet application)]
       (remove nil?)
       (map (partial td/updated-at (d/db conn)))
       (t/latest)))


(defn submitted-at
  [{conn :conn} _ application]
  (d/q '[:find ?t .
         :in $ ?app
         :where
         [?app :application/status :application.status/submitted ?tx]
         [?tx :db/txInstant ?t]]
       (d/db conn) (td/id application)))


(defn occupancy
  [_ _ application]
  (when-let [occupancy (application/occupancy application)]
    (keyword (name occupancy))))


(defn move-in-range
  [_ _ application]
  (when-let [range (application/move-in-range application)]
    (keyword (name range))))


;; ==============================================================================
;; mutations --------------------------------------------------------------------
;; ==============================================================================


(defn approve!
  "Approve an application for membership."
  [{:keys [conn requester]} {:keys [application params]} _]
  (let [application (d/entity (d/db conn) application)
        account     (application/account application)]
    (cond
      (not (account/applicant? account))
      (resolve/resolve-as nil {:message "Cannot approve non-applicant!"})

      (not (application/submitted? application))
      (resolve/resolve-as nil {:message "Application must be in `submitted` status for approval!"})

      :otherwise
      (let [license (license/by-term (d/db conn) (:term params))
            unit    (d/entity (d/db conn) (:unit params))]
        @(d/transact conn (conj (approval/approve requester account unit license (:move_in params))
                                (events/account-approved account)
                                (source/create requester)))
        (d/entity (d/db conn) (:db/id application))))))


(defn create!
  "Create a new membership application."
  [{:keys [conn requester]} {:keys [account]} _]
  (let [account (d/entity (d/db conn) account)
        application (application/create)]
    @(d/transact conn
                 [application
                  {:db/id (td/id account)
                   :account/application application}])
    (application/by-account (d/db conn) account)))


(defn- parse-pet-params [pet-params]
  (when-some [ps pet-params]
    (tb/assoc-some
     {:db/id     (or (:id ps) (d/tempid :db.part/starcity))}
     :pet/type  (:type ps)
     :pet/about (:about ps)
     :pet/breed (:breed ps)
     :pet/weight (:weight ps)
     :pet/bitten (:bitten ps)
     :pet/vaccines (:vaccines ps)
     :pet/sterile (:sterile ps)
     :pet/demeanor (:demeanor ps)
     :pet/daytime-care (:daytime_care ps)
     :pet/name (:name ps))))


(defn- parse-current-location-params [current-location-params]
  (when-some [ps current-location-params]
    (tb/assoc-when
     {:db/id (or (:id ps) (d/tempid :db.part/starcity))}
     :address/country (:country ps)
     :address/locality (:locality ps)
     :address/region (:region ps)
     :address/postal-code (:postal_code ps))))


(defn- parse-communities-params [params]
  (when-some [p params]
    (map
     (fn [community]
       (td/id community))
     p)))


(defn- parse-update-params [params]
  (tb/transform-when-key-exists params
    {:occupancy        #(keyword "application.occupancy" (name %))
     :move_in_range    #(keyword "application.move-in-range" (name %))
     :pet              #(parse-pet-params %)
     :communities      #(parse-communities-params %)
     :term             #(td/id %)
     :current_location #(parse-current-location-params %)}))


(defn- update-communities-tx
  [application communities-params]
  (let [existing     (set (map td/id (application/communities application)))
        [keep added] (map set ((juxt filter remove) (partial contains? existing) communities-params))
        removed      (set/difference existing (set/union keep added))]
    (cond-> []
      (not (empty? added))
      (concat (map #(vector :db/add (td/id application) :application/communities %) added))

      (not (empty? removed))
      (concat (map #(vector :db/retract (td/id application) :application/communities %) removed)))))


(defn- create-community-select-tx
  [existing communities]
  (let [id (:db/id existing)]
    (cond-> []
      (and (not=
            (set (map td/id (:application/communities existing)))
            (set (map td/id communities))))
      (concat (update-communities-tx existing communities)))))


;;TODO - flexibilify!
(defn update!
  "Update some attribute of a membership application."
  [{:keys [conn requester]} {:keys [application params]} _]
  (let [application (d/entity (d/db conn) application)
        account     (application/account application)
        params      (parse-update-params params)]
    (timbre/info "\n\n\n application is: " application)
    (timbre/info "\n\n\n application params are: " params)
    (cond
      (not (account/applicant? account))
      (resolve/resolve-as nil {:message "Cannot update applications for non-applicant!"})
      :otherwise
      (do
        @(d/transact conn (concat
                           [(tb/assoc-some
                             {:db/id (td/id application)}
                             :application/move-in-range (:move_in_range params)
                             :application/move-in (:move_in params)
                             :application/occupancy (:occupancy params)
                             :application/has-pet (:has_pet params)
                             :application/pet (:pet params)
                             :application/license (:term params)
                             :application/address (:current_location params)
                             :application/about (:about params))]
                           (when-some [communities (:communities params)]
                             (create-community-select-tx application communities))
                           (when (and (application/has-pet? application)
                                      (false? (:has_pet params)))
                             (let [pet (application/pet application)]
                               [[:db/retract (td/id application) :application/pet (td/id pet)]]))))
        (clojure.pprint/pprint (d/entity (d/db conn) (td/id application)))))
    (d/entity (d/db conn) (td/id application))))


;; submit ===============================


(def application-fee
  "Application fee in dollars."
  25.0)


(defn submit!
  "Submit an application after payment has been made."
  [{:keys [conn requester teller]} {:keys [application token]} _]
  (let [application (d/entity (d/db conn) application)
        account     (application/account application)]
    (cond
      (nil? token)
      (-> (response/response {:errors ["You must submit payment."]})
          (response/status 400))
      :otherwise
      (do
        (let [customer (customer/create! teller (account/email account)
                                         {:account account
                                          :source  token})]
          (payment/create! customer application-fee :payment.type/application-fee
                           {:source (first (customer/sources customer :payment-source.type/card))})
          @(d/transact conn (application/submit application)))))
    (d/entity (d/db conn) (td/id application))))


;; ==============================================================================
;; resolvers --------------------------------------------------------------------
;; ==============================================================================


(def resolvers
  {:application/account       account
   :application/approved-at   approved-at
   :application/approved-by   approved-by
   :application/term          term
   :application/status        status
   :application/submitted-at  submitted-at
   :application/updated       last-updated
   :application/occupancy     occupancy
   :application/move-in-range move-in-range
   ;; mutations
   :application/approve!      approve!
   :application/create!       create!
   :application/update!       update!
   :application/submit!       submit!})
