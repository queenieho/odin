(ns admin.notes.views
  (:require [antizer.reagent :as ant]
            [clojure.string :as string]
            [iface.components.notes :as inotes]
            [iface.utils.formatters :as format]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]))


;; note UI moved here
;; TODO query mentions properly from wherever you are
;; TODO move things to iface components with
;; parametrized subscriptions and event handlers
;; TODO when creating note from an account detail page
;; we should add the current account onto de mentions automatically


(defn- note-byline [{:keys [created updated author]}]
  (let [updated (when (not= created updated) updated)]
    [:p.byline
     (str "by " (:name author) " at "
          (format/date-time-short created)
          (when-some [d updated]
            (str " (updated at " (format/date-time-short d) ")")))]))


;; TODO improve upon this...
(defn- note-mentions [refs]
  (let [mentions (apply str (interpose ", " (map #(:name %) refs)))]
    [:p.fs1
     [:b "Mentions: "] mentions]))


(declare note-content)


(defn- note-comment [{:keys [content] :as note}]
  [:article.media
   [:figure.media-left [ant/icon {:type "message"}]]
   [:div.media-content
    [:p.body {:dangerouslySetInnerHTML {:__html (format/newlines->line-breaks content)}}]
    [note-byline note]]])


(defn note-action [props text]
  [:a (merge {:href ""} props)
   text])


(defn- note-actions
  [{:keys [id subject content created updated author] :as note} is-comment]
  (let [is-commenting (subscribe [:note/commenting? id])
        account       (subscribe [:user])
        is-author     (= (:id author) (:id @account))]
    [:div.actions
     (when-not is-comment
       [note-action
        {:on-click #(dispatch [:note/toggle-comment-form id])}
        [:span
         [ant/icon {:type (if @is-commenting "up" "down")}]
         " Comment"]])
     (when is-author
       [note-action
        {:on-click #(dispatch [:note/edit-note note])}
        "Edit"])
     (when is-author
       [note-action
        {:class    "text-red"
         :on-click (fn []
                     (ant/modal-confirm
                      {:title   "Are you sure?"
                       :content "This cannot be undone!"
                       :on-ok   #(dispatch [:note/delete! id])}))}
        "Delete"])]))


(defn note-body-text [{:keys [id subject content comments refs] :as note}]
  [:div
   (when (some? refs) [note-mentions refs])
   (when (some? subject) [:p.subject subject])
   [:p.body {:dangerouslySetInnerHTML {:__html (format/newlines->line-breaks content)}}]
   [note-byline note]
   [note-actions note]])


(defn note-edit-form [{:keys [id subject content refs] :as note}]
  (let [form      (subscribe [:note/edit-form])
        on-change #(dispatch [:note.form/update %1 %2])]
    [:div
     [ant/form-item
      {:label "Subject"}
      [ant/input
       {:placeholder "Note subject"
        :value       (:subject @form)
        :on-change   #(on-change :subject (.. % -target -value))}]]
     [ant/form-item
      {:label "Note"}
      [ant/input
       {:type        :textarea
        :rows        5
        :placeholder "Note body"
        :value       (:content @form)
        :on-change   #(on-change :content (.. % -target -value))}]]
     [ant/button
      {:on-click #(dispatch [:note/edit-cancel id])}
      "Cancel"]
     [ant/button
      {:on-click #(dispatch [:note/update! @form])}
      "Save Edit"]]))


(defn note-comment-form [{:keys [id] :as note}]
  (let [comment   (subscribe [:note/comment-text id])
        on-change #(dispatch [:note.comment/update %1 %2])]
    [:div
    [ant/form-item
     {:label "Comment"}
     [ant/input
      {:type        :textarea
       :rows        5
       :placeholder "Note comment"
       :value       @comment
       :on-change   #(on-change id (.. % -target -value))}]]
    [ant/button
     {:on-click #(dispatch [:note/add-comment! id @comment])}
     "Comment"]]))


(defn note-body [{:keys [id subject content comments refs] :as note}]
  (let [is-commenting (subscribe [:note/commenting? id])
        is-editing    (subscribe [:note/editing? id])]
    [:article.media
     [:div.media-content
      (if @is-editing
        [note-edit-form note]
        [note-body-text note])
      (when @is-commenting
        [note-comment-form note])
      (map-indexed
       #(with-meta [note-content %2 true] {:key %1})
       comments)]]))


(defn note-content
  ([note]
   [note-content note false])
  ([{:keys [id subject content comments refs] :as note} is-comment]
   [:div
    (if is-comment
      [note-comment note]
      [note-body note])]))


(defn note-card [note]
  [ant/card {:class "note"}
   [note-content note]
   ])


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
