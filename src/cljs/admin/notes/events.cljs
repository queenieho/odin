(ns admin.notes.events
  (:require [admin.notes.db :as db]
            [clojure.string :as string]
            [iface.utils.formatters :as format]
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
 (fn [{db :db} [k {:keys [refs subject content notify]}]]
   {:dispatch [:ui/loading k true]
    :graphql  {:mutation [[:create_note {:params {:refs    refs
                                                  :subject subject
                                                  :content (-> (format/escape-newlines content)
                                                               (string/replace #"\"" "&quot;")
                                                               (string/replace #"'" "&#39;"))
                                                  :notify  notify}}
                           [:id]]]
               :on-success [::create-note-success k]
               :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::create-note-success
 [(path db/path)]
 (fn [_ [_ k response]]
   {:dispatch-n [[:ui/loading k false]
                 [:note.form/clear]
                 [:note.create/toggle]]}))


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


(reg-event-fx
 :notes/fetch
 [(path db/path)]
 (fn [{db :db} [k params]]
   {:graphql {:query [[:notes {:params params}
                       [:id :subject :content
                        [:author [:id :name]]
                        [:refs [:id :name :type]]]]]
              :on-success [::notes-query-success k]
              :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::notes-query-success
 [(path db/path)]
 (fn [{db :db} [_ k response]]
   (.log js/console response)))
