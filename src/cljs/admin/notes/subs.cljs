(ns admin.notes.subs
  (:require [re-frame.core :refer [reg-sub]]
            [toolbelt.core :as tb]
            [admin.notes.db :as db]))


(reg-sub
 db/path
 (fn [db _]
   (db/path db)))


(reg-sub
 :note/showing?
 :<- [db/path]
 (fn [db _]
   (:creating db)))


(reg-sub
 :note/form
 :<- [db/path]
 (fn [db _]
   (:form db)))


(reg-sub
 :note/can-submit?
 :<- [:note/form]
 (fn [{:keys [refs subject content]} _]
   (not (and (not-empty refs) (some? subject) (some? content)))))


(defn- matching-id [id refs]
  (some #(= id (:id %)) refs))


(reg-sub
 :notes/by-account
 :<- [db/path]
 (fn [db [_ account-id]]
   (->> (:notes db)
        (filter #(matching-id account-id (:refs %)))
        (sort-by :created >))))


(reg-sub
 :notes/by-community
 :<- [db/path]
 (fn [db [_ community-id]]
   (->> (:notes db)
        (filter #(matching-id community-id (:refs %)))
        (sort-by :created >))))


(reg-sub
 :note/editing?
 :<- [db/path]
 (fn [db [_ note-id]]
   (get-in db [:editing-notes note-id])))


(reg-sub
 :note/edit-form
 :<- [db/path]
 (fn [db _]
   (:form db)))


(reg-sub
 :note/commenting?
 :<- [db/path]
 (fn [db [_ note-id]]
   (get-in db [:commenting-notes note-id :shown])))


(reg-sub
 :note/comment-text
 :<- [db/path]
 (fn [db [_ note-id]]
   (get-in db [:commenting-notes note-id :text])))


(reg-sub
 :note/is-author
 (fn [db [_ author-id]]
   (= author-id (get-in db [:account :id]))))


(reg-sub
 :note.mentions.options/members
 :<- [:accounts]
 (fn [db _]
   (filter #(= :member (:role %)) db)))
