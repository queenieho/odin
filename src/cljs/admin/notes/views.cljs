(ns admin.notes.views
  (:require [antizer.reagent :as ant]
            [clojure.string :as string]
            [iface.components.notes :as inotes]
            [iface.utils.formatters :as format]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]))


(declare note-content)


(defn note-body [{:keys [id subject content comments refs author] :as note}]
  (let [is-commenting @(subscribe [:note/commenting? id])
        is-editing    (subscribe [:note/editing? id])]
    [:article.media
     [:div.media-content
      (if @is-editing
        [inotes/edit-form {:note-id   id
                           :form      @(subscribe [:note.edit/form])
                           :on-change #(dispatch [:note.form/update %1 %2])
                           :on-cancel #(dispatch [:note.edit/cancel %])
                           :on-ok     #(dispatch [:note/update! %])}]
        [inotes/body-text {:note          note
                           :is-commenting is-commenting
                           :on-comment    #(dispatch [:note.comment/show %])
                           :on-edit       #(dispatch [:note/edit %])
                           :on-delete     #(dispatch [:note/delete! %])
                           :is-author     @(subscribe [:note/is-author? (:id author)])}])
      (when is-commenting
        [inotes/comment-form {:note-id   (:id note)
                              :comment   @(subscribe [:note/comment-text id])
                              :on-change #(dispatch [:note.comment/update %1 %2])
                              :on-ok     #(dispatch [:note/add-comment! %1 %2])}])
      (map-indexed
       #(with-meta [note-content %2 true] {:key %1})
       (sort-by :created comments))]]))


(defn note-content
  ([note]
   [note-content note false])
  ([{:keys [id subject content comments refs] :as note} is-comment]
   [:div
    (if is-comment
      [inotes/comment note]
      [note-body note])]))


(defn note-card [note]
  [ant/card {:class "note"}
   [note-content note]])


(defn pagination []
  (let [pagination (subscribe [:accounts.entry.notes/pagination])]
    [:div.mt3
     [ant/pagination
      {:show-size-changer   true
       :on-show-size-change #(dispatch [:accounts.entry.notes/change-pagination %1 %2])
       :default-current     (:page @pagination)
       :total               (:total @pagination)
       :show-total          (fn [total range]
                              (format/format "%s-%s of %s notes"
                                             (first range) (second range) total))
       :page-size-options   ["5" "10" "15" "20"]
       :default-page-size   (:size @pagination)
       :on-change           #(dispatch [:accounts.entry.notes/change-pagination %1 %2])}]]))
