(ns admin.properties.subs
  (:require [admin.properties.db :as db]
            [clojure.string :as string]
            [iface.utils.norms :as norms]
            [re-frame.core :refer [reg-sub]]
            [toolbelt.core :as tb]))



(reg-sub
 db/path
 (fn [db _]
   (db/path db)))


(reg-sub
 :properties/list
 :<- [db/path]
 (fn [db _]
   (->> (norms/denormalize db :properties/norms)
        (sort-by #(string/lower-case (:name %))))))


(reg-sub
 :property
 :<- [db/path]
 (fn [db [_ property-id]]
   (norms/get-norm db :properties/norms property-id)))


(reg-sub
 :property/units
 :<- [db/path]
 (fn [db [_ property-id]]
   (->> (norms/get-norm db :properties/norms property-id)
        :units
        (sort-by :number))))


(reg-sub
 :property/rates
 :<- [db/path]
 (fn [db [_ property-id]]
   (sort-by :term (get-in db [:property-rates property-id]))))


(reg-sub
 :property.rates/can-submit?
 :<- [db/path]
 (fn [db [_ property-id]]
   (let [property (norms/get-norm db :properties/norms property-id)]
     (not= (set (get-in db [:property-rates property-id]))
           (set (:rates property))))))


(reg-sub
 :property.unit/rates
 :<- [db/path]
 (fn [db [_ property-id unit-id]]
   (sort-by :term (get-in db [:unit-rates unit-id]))))


(reg-sub
 :property.unit.rates/can-submit?
 :<- [db/path]
 (fn [db [_ property-id unit-id]]
   (let [unit (db/unit db property-id unit-id)]
     (not= (set (get-in db [:unit-rates unit-id]))
           (set (db/unit-rates unit))))))


(reg-sub
 :community.create/form
 :<- [db/path]
 (fn [db [_ form-key]]
   (get-in db [:form form-key])))


(reg-sub
 :financial/form
 :<- [db/path]
 (fn [db [_]]
   (:financial-form db)))


(reg-sub
 :verify/form
 :<- [db/path]
 (fn [db [_]]
   (:verify-accounts db)))
