(ns admin.notes.subs
  (:require [re-frame.core :refer [reg-sub]]
            [toolbelt.core :as tb]
            [admin.notes.db :as db]))


(reg-sub
 db/path
 (fn [db _]
   (db/path db)))


(reg-sub
 :note/showing?
 :<- [db/path]
 (fn [db _]
   (:creating db)))


(reg-sub
 :note/form
 :<- [db/path]
 (fn [db _]
   (:form db)))


(defn- matching-account [account-id refs]
  (some #(= account-id (:id %)) refs))


(reg-sub
 :notes/by-account
 :<- [db/path]
 (fn [db [_ account-id]]
   (->> (:notes db)
        (filter #(matching-account account-id (:refs %)))
        (sort-by :created >))))
