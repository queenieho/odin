(ns migrations.notes
  (:require [datomic.api :as d]))


(defn get-notes
  [conn]
  (d/q '[:find ?n ?a
         :where
         [?a :account/notes ?n]]
       (d/db conn)))


(defn add-refs-to-notes-tx [notes]
  (map (fn [[note-id account-id]]
         [:db/add note-id :note/refs account-id]) notes))


(defn add-refs-to-existing-notes
  [conn]
  (->> (get-notes conn)
       (add-refs-to-notes-tx)
       (d/transact conn)
       (deref)))


(comment

  (do
    (def conn odin.datomic/conn)
    (def teller odin.teller/teller))

  )
