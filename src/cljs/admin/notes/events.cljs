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
   {:dispatch-n [[:accounts/query {}]
                 [:note.create/toggle]]}))


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
    :graphql  {:mutation
               [[:create_note {:params {:refs    refs
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
                 [:note.create/toggle]
                 [:notes/fetch]]}))


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
                       [:id :subject :content :created :updated
                        [:comments [:id :subject :content :created :updated
                                    [:author [:id :name]]]]
                        [:author [:id :name]]
                        [:refs [:id :name :type]]]]]
              :on-success [::notes-query-success k]
              :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::notes-query-success
 [(path db/path)]
 (fn [{db :db} [_ k response]]
   {:db (assoc db :notes (get-in response [:data :notes]))}))


;; update note ===========================


(reg-event-fx
 :note/edit-note
 [(path db/path)]
 (fn [{db :db} [_ {:keys [id] :as note}]]
   {:dispatch [:note/toggle-editing id]
    :db       (assoc db :form note)}))


(reg-event-fx
 :note/edit-cancel
 [(path db/path)]
 (fn [{db :db} [_ note-id]]
   {:dispatch-n [[:note.form/clear]
                 [:note/toggle-editing note-id]]}))


(reg-event-db
 :note/toggle-editing
 [(path db/path)]
 (fn [db [_ note-id]]
   (update-in db [:editing-notes note-id] not)))


(reg-event-fx
 :note/update!
 [(path db/path)]
 (fn [_ [k {:keys [id subject content]}]]
   {:dispatch [:ui/loading k true]
    :graphql {:mutation
              [[:update_note {:params {:note id
                                       :subject subject
                                       :content (format/escape-newlines content)}}
                [:id]]]
              :on-success [::update-note-success k]
              :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::update-note-success
 [(path db/path)]
 (fn [_ [_ k response]]
   {:dispatch-n [[:ui/loading k false]
                 [:note.form/clear]
                 [:note/toggle-editing (get-in response [:data :update_note :id])]
                 [:notes/fetch]]}))


;; comment ==============================

(reg-event-fx
 :note.comment/show
 [(path db/path)]
 (fn [{db :db} [_ note-id]]
   {:dispatch-n [[:note/toggle-comment-form note-id]]}))


(reg-event-db
 :note/toggle-comment-form
 [(path db/path)]
 (fn [db [_ note-id]]
   (update-in db [:commenting-notes note-id :shown] not)))


(reg-event-db
 :note.comment/update
 [(path db/path)]
 (fn [db [_ note-id text]]
   (assoc-in db [:commenting-notes note-id :text] text)))


(reg-event-fx
 :note/add-comment!
 [(path db/path)]
 (fn [db [k note-id text]]
   {:dispatch [:ui/loading k true]
    :graphql  {:mutation
               [[:add_note_comment {:note   note-id
                                    :text   text}
                 [:id :subject :content :created :updated
                  [:refs [:id :name :type]]
                  [:author [:id :name]]]]]
               :on-success [::add-note-comment-success k note-id]
               :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::add-note-comment-success
 [(path db/path)]
 (fn [{db :db} [_ k note-id response]]
   (let [comment (get-in response [:data :add_note_comment])]
     {:dispatch-n [[:ui/loading k false]
                   [:note.comment/update note-id ""]
                   [:note/toggle-comment-form note-id]]
      :db         (update db :notes (fn [notes]
                                      (map
                                       (fn [note]
                                         (if (= (:id note) note-id)
                                           (-> (update note :comments vec)
                                               (update :comments conj comment))
                                           note))
                                       notes)))})))


;; delete ===============================


(reg-event-fx
 :note/delete!
 (fn [{db :db} [k note-id]]
   {:dispatch [:ui/loading k true]
    :graphql  {:mutation   [[:delete_note {:note note-id}]]
               :on-success [::delete-note-success k note-id]
               :on-failure [:graphql/failure k]}}))

(reg-event-fx
 ::delete-note-success
 [(path db/path)]
 (fn [{db :db} [_ k note-id]]
   {:dispatch-n [[:ui/loading k false]
                 [:notes/fetch]]
    :db         (update db :notes (fn [notes]
                                    (remove #(= note-id (:id %)) notes)))}))
