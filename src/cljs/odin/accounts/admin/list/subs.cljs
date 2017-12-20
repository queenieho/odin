(ns odin.accounts.admin.list.subs
  (:require [odin.accounts.admin.list.db :as db]
            [re-frame.core :refer [reg-sub]]
            [toolbelt.core :as tb]
            [iface.table :as table]))


(reg-sub
 db/path
 (fn [db _]
   (db/path db)))


(reg-sub
 :admin.accounts.list/query-params
 :<- [db/path]
 (fn [db _]
   (:params db)))


(reg-sub
 :admin.accounts.list/selected-role
 :<- [db/path]
 (fn [db _]
   (get-in db [:params :selected-role])))


(def sortfns
  {:property     {:path [:property :name]}
   :unit         {:path [:active_license :unit :number]}
   :license_end  (assoc table/date-sort-comp :path [:active_license :ends])
   :license_term {:path [:active_license :term]}
   :move_in      (assoc table/date-sort-comp :path [:application :move_in])
   :created      (assoc table/date-sort-comp :path [:application :created])
   :updated      (assoc table/date-sort-comp :path [:application :updated])
   :submitted    (assoc table/date-sort-comp :path [:application :submitted])})


(reg-sub
 :admin.accounts/list
 :<- [:admin.accounts.list/query-params]
 :<- [:accounts]
 (fn [[params accounts] _]
   (if (or (nil? (:sort-by params)) (nil? (:sort-order params)))
     accounts
     (table/sort-rows params sortfns accounts))))
