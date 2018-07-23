(ns admin.accounts.subs
  (:require [admin.accounts.db :as db]
            [clojure.string :as string]
            [iface.components.table :as table]
            [iface.utils.norms :as norms]
            [re-frame.core :refer [reg-sub]]
            [toolbelt.core :as tb]))



;; ==============================================================================
;; helper =======================================================================
;; ==============================================================================

(reg-sub
 db/path
 (fn [db _]
   (db/path db)))


(reg-sub
 :accounts
 :<- [db/path]
 (fn [db _]
   (norms/denormalize db :accounts/norms)))


(reg-sub
 :account
 :<- [db/path]
 (fn [db [_ account-id]]
   (norms/get-norm db :accounts/norms account-id)))


(reg-sub
 :account/application
 :<- [db/path]
 (fn [db [_ account-id]]
   (:application (norms/get-norm db :accounts/norms account-id))))


(defn- sum-amount [items]
  (reduce
   (fn [sum item]
     (cond
       (number? (:price item))
       (+ sum (:price item))

       :else
       sum))
   0
   items))


(reg-sub
 :security-deposit/sum-amount
 (fn [_ [_ items]]
   (sum-amount items)))


(defn- refund-amount
  [deposit-amount form]
  (let [charge-amount (sum-amount (:charges form))
        credit-amount (sum-amount (:credits form))
        refund-amount (-> deposit-amount
                          (- charge-amount)
                          (+ credit-amount))]
    (if (js/Number.isNaN refund-amount)
      0
      refund-amount)))

(reg-sub
 :security-deposit/refund-amount
 :<- [:security-deposit/form]
 (fn [form [_ deposit-amount]]
   (refund-amount deposit-amount form)))


(defn- some-item?
  [item]
  (and (not= db/default-key-value item)
       (some? item)))


(defn- some-item-str?
  [item]
  (and (some-item? item)
       (when (string? item)
         (not (empty? item)))))


(defn- validate-items
  [line-items]
  (reduce
   (fn [result {desc :desc types :types price :price}]
     (conj result {:desc  (some-item-str? desc)
                   :types (some-item? types)
                   :price (and (number? price)
                               (<= 0 price))}))
   []
   line-items))


(defn- valid-form-items?
  [items]
  (boolean
   (reduce
    (fn [return item]
      (if (some false? (vals item))
        (reduced false)
        return))
    true
    items)))


(defn- valid-form?
  [db]
  (and (valid-form-items? (:charges-form-validation db))
       (valid-form-items? (:credits-form-validation db))))


(defn- can-submit?
  [deposit-amount form]
  (let [refund-amount (refund-amount deposit-amount form)]
    (false? (or (js/Number.isNaN refund-amount)
                (> refund-amount deposit-amount)
                (> 0 refund-amount)))) )


(defn- form-validation
  [db]
  (let [charges (get-in db [:form :charges])
        credits (get-in db [:form :credits])]
    (-> (assoc-in {} [:charges-form-validation] (validate-items charges))
        (assoc-in [:credits-form-validation] (validate-items credits)))))


(reg-sub
 :security-deposit.form/validation
 :<- [db/path]
 (fn [db _]
   (form-validation db)))


(reg-sub
 :security-deposit/can-submit?
 :<- [db/path]
 (fn [db [_ deposit-amount]]
   (and (valid-form? (form-validation db))
        (can-submit? deposit-amount (:form db)))))


(reg-sub
 :security-deposit.line-item/is-valid?
 :<- [:security-deposit.form/validation]
 (fn [db [_ k refund-type item idx]]
   (or (= db/default-key-value (k item))
       (get-in db [(keyword (str (name refund-type) "-form-validation")) idx k]))))


(reg-sub
 :security-deposit/refundable?
 (fn [_ [_ account]]
   (let [refunded (deposit/refunded? (:deposit account))]
     (cond
       refunded
       "Member has already been refunded their deposit."

       (not (deposit/refundable? (:deposit account)))
       "Member does not have a payout account. Please inform the member to where
       to input details to be refunded their security deposit."

       :else nil))))


(reg-sub
 :security-deposit/input-value
 (fn [_ [_ item k]]
   (let [value (k item)]
     (when-not (= db/default-key-value value) value))))


(reg-sub
 :security-deposit/form
 :<- [db/path]
 (fn [db _]
   (:form db)))


(reg-sub
 :security-deposit/types
 :<- [db/path]
 (fn [db _]
   (:types db)))


;; ==============================================================================
;; list =========================================================================
;; ==============================================================================

(reg-sub
 :accounts.list/query-params
 :<- [db/path]
 (fn [db _]
   (:params db)))


(reg-sub
 :accounts.list/selected-view
 :<- [db/path]
 (fn [db _]
   (get-in db [:params :selected-view])))


(def sortfns
  {:property     {:path [:property :name]}
   :unit         {:path [:active_license :unit :number]}
   :license_end  (assoc table/date-sort-comp :path [:active_license :ends])
   :license_term {:path [:active_license :term]}
   :move_in      (assoc table/date-sort-comp :path [:application :move_in])
   :created      (assoc table/date-sort-comp :path [:application :created])
   :updated      (assoc table/date-sort-comp :path [:application :updated])
   :submitted    (assoc table/date-sort-comp :path [:application :submitted])})


(defn- sort-accounts
  [{:keys [sort-by sort-order] :as params} accounts]
  (if (or (nil? sort-by) (nil? sort-order))
    accounts
    (table/sort-rows params sortfns accounts)))


(defn- role-filter
  [{selected-view :selected-view} accounts]
  (if (= selected-view "member")
    (filter (comp some? :active_license) accounts)
    accounts))


(reg-sub
 :accounts/list
 :<- [:accounts.list/query-params]
 :<- [:accounts]
 (fn [[params accounts] _]
   (->> accounts
        (role-filter params)
        (sort-accounts params))))


;; ==============================================================================
;; entry ========================================================================
;; ==============================================================================


(reg-sub
 :accounts.entry/selected-tab
 :<- [db/path]
 (fn [db _]
   (:tab db)))


;; approval =====================================================================


(reg-sub
 :accounts.entry.approval/units
 :<- [db/path]
 (fn [db _]
   (->> (:units db)
        (sort-by :number))))


;; reassignment =================================================================


(reg-sub
 :accounts.entry.reassign/form-data
 :<- [db/path]
 (fn [db [_ k]]
   (let [form (:reassign-form db)]
     (if (some? k)
       (get form k)
       form))))


;; transition ===================================================================


(reg-sub
 :accounts.entry.transition/form-data
 :<- [db/path]
 (fn [db [_ k]]
   (let [form (:transition-form db)]
     (if (some? k)
       (get form k)
       form))))


;; notes ========================================================================


(reg-sub
 :accounts.entry.note/editing
 :<- [db/path]
 (fn [db [_ id]]
   (get-in db [:editing-notes id])))


(reg-sub
 :accounts.entry.create-note/showing?
 :<- [db/path]
 (fn [db _]
   (get db :showing-create-note)))


(reg-sub
 :accounts.entry.create-note/form-data
 :<- [db/path]
 (fn [db [_ account-id]]
   (get-in db [:create-form account-id])))


(reg-sub
 :accounts.entry/can-create-note?
 :<- [db/path]
 (fn [db [_ account-id]]
   (let [{:keys [subject content]} (get-in db [:create-form account-id])]
     (and (not (string/blank? subject))
          (not (string/blank? content))))))


(reg-sub
 :accounts.entry.notes/pagination
 :<- [db/path]
 (fn [db _]
   (let [total (count (:notes db))]
     (assoc (:notes-pagination db) :total total))))


(reg-sub
 :accounts.entry/notes
 :<- [db/path]
 :<- [:accounts.entry.notes/pagination]
 (fn [[db {:keys [size page]}] _]
   (let [notes (:notes db)]
     (->> notes
          (drop (* (dec page) size))
          (take size)))))


(reg-sub
 :accounts.entry.note/comment-form-shown?
 :<- [db/path]
 (fn [db [_ note-id]]
   (boolean (get-in db [:commenting-notes note-id :shown]))))


(reg-sub
 :accounts.entry.note/comment-text
 :<- [db/path]
 (fn [db [_ note-id]]
   (get-in db [:commenting-notes note-id :text])))


;; orders =======================================================================


(reg-sub
 :account/orders
 :<- [db/path]
 (fn [db [_ account-id]]
   (->> (norms/get-norm db :accounts/norms account-id)
        :orders
        (sort-by :created))))
