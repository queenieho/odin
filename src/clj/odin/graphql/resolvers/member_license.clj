(ns odin.graphql.resolvers.member-license
  (:require [blueprints.models.account :as account]
            [blueprints.models.member-license :as member-license]
            [blueprints.models.source :as source]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [datomic.api :as d]
            [odin.graphql.authorization :as authorization]
            [odin.graphql.resolvers.utils.autopay :as autopay-utils]
            [odin.graphql.resolvers.utils.plans :as plans-utils]
            [taoensso.timbre :as timbre]
            [teller.customer :as tcustomer]
            [teller.payment :as tpayment]
            [teller.plan :as tplan]
            [teller.subscription :as tsubscription]
            [toolbelt.date :as date]
            [toolbelt.core :as tb]))

;; ==============================================================================
;; helpers ======================================================================
;; ==============================================================================


(defn- license-customer
  "Given a member's `license`, produce the teller customer."
  [teller license]
  (tcustomer/by-account teller (member-license/account license)))


(defn- autopay-on?
  [teller license]
  (let [customer (license-customer teller license)]
    (-> (tsubscription/query teller {:customers [customer]
                                     :payment-types   [:payment.type/rent]})
        seq
        boolean)))


;; ==============================================================================
;; fields -----------------------------------------------------------------------
;; ==============================================================================


(defn autopay-on
  "Whether or not autopay is active for this license."
  [{teller :teller} _ license]
  (autopay-on? teller license))


(defn- payment-within
  [teller license date]
  (let [customer (license-customer teller license)
        tz       (member-license/time-zone license)
        from     (date/beginning-of-month date tz)
        to       (date/end-of-month date tz)]
    (when (some? customer)
      (first
       (tpayment/query teller {:customers     [customer]
                               :payment-types [:payment.type/rent]
                               :statuses      [:payment.status/due]
                               :from          from
                               :to            to})))))


(defn rent-status
  "What's the status of this license owner's rent?"
  [{teller :teller} _ license]
  (when-some [payment (payment-within teller license (java.util.Date.))]
    (cond
      (tpayment/due? payment)     :due
      (tpayment/pending? payment) :pending
      (tpayment/paid? payment)    :paid
      (tpayment/overdue? payment) :overdue
      :otherwise                  :due)))


(defn status
  "The status of the member license."
  [_ _ license]
  (keyword (name (member-license/status license))))


(defn- rent-payments
  "All rent payments made by the owner of this license."
  [{teller :teller} _ license]
  (tpayment/query teller {:customers     [(license-customer teller license)]
                          :payment-types [:payment.type/rent]}))


;; ==============================================================================
;; mutations --------------------------------------------------------------------
;; ==============================================================================


(defn- reassign-autopay!
  [{:keys [conn teller requester]} {:keys [license unit rate]}]
  (try
    (let [license-after (d/entity (d/db conn) license)
          account       (member-license/account license-after)
          customer      (tcustomer/by-account teller account)
          old-sub       (->> (tsubscription/query teller {:customers     [customer]
                                                          :payment-types [:payment.type/rent]})
                             (tb/find-by tsubscription/active?))
          old-plan      (tsubscription/plan old-sub)
          source        (tsubscription/source old-sub)
          new-plan      (tplan/create! teller (plans-utils/plan-name teller license-after) :payment.type/rent rate)]
      (tsubscription/cancel! old-sub)
      (tplan/deactivate! old-plan)
      (tsubscription/subscribe! customer new-plan {:source   source
                                                   :start-on (autopay-utils/autopay-start customer)})
      (d/entity (d/db conn) license))
    (catch Throwable t
      (timbre/error t ::reassign-room {:license license :unit unit :rate rate})
      (resolve/resolve-as nil {:message "Failed to completely reassign room! Likely to do with autopay..."}))))


(defn reassign!
  "Reassign a the member with license `license` to a new `unit`."
  [{:keys [conn teller requester] :as ctx} {{:keys [license unit rate] :as params} :params} _]
  (let [license-before (d/entity (d/db conn) license)]
    (when (or (not= rate (member-license/rate license-before))
              (not= unit (member-license/unit license-before)))
      @(d/transact conn [{:db/id               license
                          :member-license/rate rate
                          :member-license/unit unit}
                         (source/create requester)])
      (if (autopay-on? teller license-before)
        (reassign-autopay! ctx params)
        (d/entity (d/db conn) license)))))


;; ==============================================================================
;; resolvers --------------------------------------------------------------------
;; ==============================================================================


(defmethod authorization/authorized? :member-license/reassign! [_ account _]
  (account/admin? account))


(def resolvers
  {;; fields
   :member-license/status        status
   :member-license/autopay-on    autopay-on
   :member-license/rent-payments rent-payments
   :member-license/rent-status   rent-status
   ;; mutations
   :member-license/reassign!     reassign!})
