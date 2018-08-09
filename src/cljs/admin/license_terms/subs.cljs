(ns admin.license-terms.subs
  (:require [admin.license-terms.db :as db]
            [re-frame.core :refer [reg-sub]]))


(reg-sub
 db/path
 (fn [db _]
   (db/path db)))


(reg-sub
 :license-terms/list
 :<- [db/path]
 (fn [db [_ {:keys [available]}]]
   (let [terms (:license-terms db)]
     (if available
       (filter :available terms)
       terms))))
