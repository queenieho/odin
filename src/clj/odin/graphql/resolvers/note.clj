(ns odin.graphql.resolvers.note
  (:require [odin.graphql.authorization :as authorization]
            [blueprints.models.account :as account]
            [blueprints.models.events :as events]
            [blueprints.models.note :as note]
            [blueprints.models.property :as property]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [datomic.api :as d]
            [blueprints.models.source :as source]
            [taoensso.timbre :as timbre]
            [toolbelt.core :as tb]))


(defn- get-account [note]
  (if-let [parent (note/parent note)]
    (get-account parent)
    (note/account note)))


(defn account [_ _ note]
  (get-account note))


(defn refs [_ _ note]
  (note/refs note))


(defn note-ref-type [_ _ ref]
  (cond
    (account/email ref) :account
    (property/code ref) :property
    :otherwise nil))


(defn note-ref-name [_ _ ref]
  (cond
    (account/email ref) (account/short-name ref)
    (property/code ref) (property/name ref)
    :otherwise nil))


;; ==============================================================================
;; queries ======================================================================
;; ==============================================================================


(defn- query-notes
  [db params]
  (->> (tb/transform-when-key-exists params {:refs (partial map (partial d/entity db))})
       (note/query db)))


(defn query
  "Query notes"
  [{conn :conn} {params :params} _]
  (try
    (query-notes (d/db conn) params)
    (catch Throwable t
      (timbre/error t "error querying notes")
      (resolve/resolve-as nil {:message  (.getMessage t)
                               :err-data (ex-data t)}))))


(defn entry
  "Get one note by id"
  [{conn :conn} {id :id} _]
  (d/entity (d/db conn) id))


;; ==============================================================================
;; mutations ====================================================================
;; ==============================================================================


(defn add-comment!
  [{:keys [conn requester]} {:keys [note text]} _]
  (let [parent  (d/entity (d/db conn) note)
        comment (note/create-comment requester text)]
    @(d/transact conn [(note/add-comment parent comment)
                       (events/note-comment-created note comment)
                       (source/create requester)])
    (note/by-uuid (d/db conn) (note/uuid comment))))


(defn create!
  [{:keys [conn requester]} {{:keys [refs subject content notify]} :params} _]
  (let [note (note/create subject content refs :author requester)]
    @(d/transact conn (tb/conj-when
                       [note
                        (source/create requester)]
                       (when notify (events/note-created note))))
    (note/by-uuid (d/db conn) (note/uuid note))))


(defn delete!
  [{:keys [conn requester]} {:keys [note]} _]
  @(d/transact conn [[:db.fn/retractEntity note]
                     (source/create requester)])
  :ok)


(defn update!
  [{:keys [conn requester]} {{:keys [note subject content]} :params} _]
  (let [note (d/entity (d/db conn) note)]
    @(d/transact conn [(if (nil? subject)
                         (note/update note :content content)
                         (note/update note :subject subject :content content))])
    (d/entity (d/db conn) (:db/id note))))


(defmethod authorization/authorized? :note/create! [_ account _]
  (account/admin? account))


(defmethod authorization/authorized? :note/update! [{conn :conn} account params]
  (let [note (d/entity (d/db conn) (get-in params [:params :note]))]
    (and (account/admin? account) (= (:db/id account) (-> note :note/author :db/id)))))


(defmethod authorization/authorized? :note/delete! [{conn :conn} account params]
  (let [note (d/entity (d/db conn) (:note params))]
    (and (account/admin? account) (= (:db/id account) (-> note :note/author :db/id)))))


(defmethod authorization/authorized? :note/add-comment! [_ account _]
  (account/admin? account))


(def resolvers
  {;; fields
   :note/account      account
   :note/refs         refs
   :note.ref/type     note-ref-type
   :note.ref/name     note-ref-name
   ;; mutations
   :note/add-comment! add-comment!
   :note/create!      create!
   :note/delete!      delete!
   :note/update!      update!
   ;; queries
   :note/query        query
   :note/entry        entry})
