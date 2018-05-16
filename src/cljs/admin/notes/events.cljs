(ns admin.notes.events
  (:require [admin.notes.db :as db]
            [re-frame.core :refer [reg-event-db reg-event-fx]]))


(reg-event-db
 :notes.create/toggle
 (fn [db _]
   (update-in db [:showing] not)))
