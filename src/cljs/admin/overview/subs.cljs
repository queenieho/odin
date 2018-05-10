(ns admin.overview.subs
  (:require [admin.overview.db]
            [re-frame.core :refer [reg-sub]]
            [iface.utils.time :as time]))


;; TODO filter out accounts with a move-out date
;; TODO keep only accounts with a eot 45 days from now
(reg-sub
 :overview.accounts/end-of-term
 :<- [:accounts/list]
 (fn [accounts _]
   (->> (remove #(some? (get-in % [:active_license :move_out :date])) accounts)
        #_(remove #(< 45 (time/days-between (get-in % [:active_license :ends])))))))


(reg-sub
 :overview.accounts/move-out
 :<- [:accounts/list]
 (fn [accounts _]
   (let [accounts' (map
                    #(if (even? (count (:name %)))
                       (assoc-in % [:active_license :move_out] {:date     #inst "2018-06-06"
                                                                :pre_walk #inst "2018-05-20"})
                       %)
                    accounts)]
     accounts
     #_(filter #(some? (get-in % [:active_license :move_out :date])) accounts))))
