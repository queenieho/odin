(ns admin.notes.events
  (:require [admin.notes.db :as db]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]))


(reg-event-db
 :note.create/toggle
 [(path db/path)]
 (fn [db _]
   (update-in db [:creating] not)))


(reg-event-fx
 :note.create/open
 [(path db/path)]
 (fn [{db :db} _]
   {:dispatch-n [[:notes.create/toggle]]}))


(reg-event-fx
 :note.create/cancel
 [(path db/path)]
 (fn [{db :db} _]
   {:dispatch-n [[:note.form/clear]
                 [:note.create/toggle]]}))


(reg-event-fx
 :note.create/create-note!
 [(path db/path)]
 (fn [{db :db} [k]]
   {:dispatch [:ui/loading k true]
    }))


(reg-event-db
 :note.form/update
 [(path db/path)]
 (fn [db [_ key value]]
   (assoc-in db [:form key] value)))


(reg-event-db
 :note.form/clear
 [(path db/path)]
 (fn [db _]
   (-> (dissoc db :form)
       (assoc-in [:form :notify] true))))
