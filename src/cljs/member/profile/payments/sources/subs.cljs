(ns member.profile.payments.sources.subs
  (:require [member.profile.payments.sources.db :as db]
            [re-frame.core :as rf :refer [reg-sub]]
            [toolbelt.core :as tb]
            [iface.utils.time :as time]))


(reg-sub
 ::sources
 (fn [db _]
   (db/path db)))


(reg-sub
 ::payout-account
 (fn [db _]
   (db/add-payout db)))


;; =============================================================================
;; All Sources
;; =============================================================================


(reg-sub
 :payment/sources
 :<- [:user]
 :<- [:payment-sources]
 (fn [[user sources] [_ type]]
   (let [sources (filter #(= (:id user) (get-in % [:account :id])) sources)]
     (if (some? type)
       (filter #(= (:type %) type) sources)
       sources))))


(reg-sub
 :payment.sources/service-source
 :<- [:payment/sources]
 (fn [sources _]
   (tb/find-by #(and (:default %) (= :card (:type %))) sources)))


(reg-sub
 :payment.sources/verified-banks
 :<- [:payment/sources :bank]
 (fn [banks _]
   (filter #(= (:status %) "verified") banks)))


(reg-sub
 :payment.sources/has-verified-bank?
 :<- [:payment.sources/verified-banks]
 (fn [banks _]
   (not (empty? banks))))


;; =============================================================================
;; Current Sources
;; =============================================================================


(reg-sub
 :payment.sources/current-id
 :<- [::sources]
 (fn [db _]
   (:current db)))


(reg-sub
 :payment.sources/current
 :<- [:payment/sources]
 :<- [:payment.sources/current-id]
 (fn [[sources source-id] _]
   (tb/find-by (fn [source] (= source-id (:id source))) sources)))


(reg-sub
 :payment.sources.current/can-remove?
 :<- [:payment/sources :bank]
 :<- [:payment.sources/current]
 (fn [[banks current] _]
   (if (= :bank (:type current))
     (< 1 (count banks))
     true)))


;; =============================================================================
;; Add Source
;; =============================================================================


(reg-sub
 ::add-source
 (fn [db _]
   (db/add-path db)))


(reg-sub
 :payment.sources.add/available-types
 :<- [::add-source]
 (fn [db _]
   (:available-types db)))


(reg-sub
 :payment.sources.add/type
 :<- [::add-source]
 (fn [db _]
   (:type db)))


(reg-sub
 :payment.sources.bank.verify/microdeposits
 :<- [::add-source]
 (fn [db _]
   (:microdeposits db)))


;; ==============================================================================
;; Add Deposit Payout Account ===================================================
;; ==============================================================================


(reg-sub
 :payment/account
 :<- [:payment/sources]
 (fn [sources _]
   (:account (first sources))))


(reg-sub
 :account/payout-account
 :<- [:payment/account]
 (fn [account _]
   (:payout_account account)))


(reg-sub
 :payout-account/form
 :<- [::payout-account]
 (fn [db _]
   (:form db)))


(defn- some-item?
  [item]
  (and (not= db/default-key-value item)
       (some? item)))


(defn- some-item-str?
  ([item]
   (and (some-item? item)
        (when (string? item)
          (not (empty? item)))))
  ([item num]
   (and (some-item-str? item)
        (= num (count item)))))


(defn- form-validation
  [{:keys [line1 line2 city state postal-code country
           dob ssn routing-number account-number]}]
  {;; Address
   :line1       (some-item-str? line1)
   ;; :line2       (some-item-str? line2) ;; Ignore since it's optional
   :city        (some-item-str? city)
   :state       (some-item-str? state (:state db/char-limit))
   :postal-code (some-item-str? postal-code (:postal-code db/char-limit))
   :country     (some-item-str? country (:country db/char-limit))

   ;; Personal
   :dob (some-item? dob)
   :ssn (some-item-str? ssn (:ssn db/char-limit))

   ;; Bank
   :routing-number (some-item-str? routing-number (:routing-number db/char-limit))
   :account-number (some-item-str? account-number (:account-number db/char-limit))})


(reg-sub
 :payout-account.form/validation
 :<- [:payout-account/form]
 (fn [form _]
   (form-validation form)))

(reg-sub
 :payout-account.form/is-valid?
 :<- [:payout-account.form/validation]
 :<- [:payout-account/form]
 (fn [[vdb form] [_ k]]
   (or (= db/default-key-value (k form))
       (k vdb)
       (= k :line2))))


(reg-sub
 :payout-account/can-submit?
 :<- [:payout-account.form/validation]
 (fn [db _]
   (every? true? (vals db))))


(reg-sub
 :payout-account/input-value
 :<- [:payout-account/form]
 (fn [form [_ k]]
   (let [value (k form)]
     (when-not (= db/default-key-value value) value))))


(reg-sub
 :payout-account.date/default-date
 (fn [_]
   (-> (js/moment (time/now))
       (.subtract 13 "year" )
       (.subtract 1 "day"))))


;; =============================================================================
;; Autopay
;; =============================================================================


(reg-sub
 :payment.sources/can-enable-autopay?
 :<- [:payment.sources/has-verified-bank?]
 :<- [:member.rent/unpaid?]
 (fn [[verified unpaid] _]
   (and verified (not unpaid))))


(reg-sub
 :payment.sources/autopay-source
 :<- [:payment/sources]
 (fn [sources _]
   (tb/find-by :autopay sources)))


(reg-sub
 :payment.sources/autopay-on?
 :<- [:payment.sources/autopay-source]
 (fn [source _]
   (:autopay source)))
