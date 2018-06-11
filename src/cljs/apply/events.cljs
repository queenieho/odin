(ns apply.events
  (:require [apply.db :as db]
            [re-frame.core :refer [reg-event-db reg-event-fx]]))


(reg-event-fx
 :app/init
 (fn [_ [_ account]]
   {:db (db/bootstrap account)}))
