(ns admin.overview.subs
  (:require [admin.overview.db]
            [re-frame.core :refer [reg-sub]]))


;; TODO filter out accounts with a move-out date
;; TODO keep only accounts with a eot 45 days from now
(reg-sub
 :overview.accounts/end-of-term
 :<- [:accounts/list]
 (fn [accounts _]
   accounts))
