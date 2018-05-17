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
