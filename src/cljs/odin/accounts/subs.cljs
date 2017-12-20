(ns odin.accounts.subs
  (:require [odin.accounts.db :as db]
            [odin.accounts.admin.subs]
            [odin.utils.norms :as norms]
            [re-frame.core :refer [reg-sub]]))


(reg-sub
 db/path
 (fn [db _]
   (db/path db)))


(reg-sub
 :accounts
 :<- [db/path]
 (fn [db _]
   (norms/denormalize db :accounts/norms)))


;; TODO: Conflicts in `odin.subs`
;; (reg-sub
;;  :account
;;  :<- [db/path]
;;  (fn [db [_ account-id]]
;;    (norms/get-norm db :accounts/norms account-id)))