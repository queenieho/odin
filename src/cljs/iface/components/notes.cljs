(ns iface.components.notes
  (:require [antizer.reagent :as ant]
            [iface.utils.formatters :as format]
            [reagent.core :as r]
            [toolbelt.core :as tb]))


;; ==============================================================================
;; notes ui =====================================================================
;; ==============================================================================


(defn note-action [props text]
  [:a (merge {:href ""} props)
   text])


(defn- note-actions
  ([props]
   [note-actions props false])
  ([{:keys [note is-deleting is-commenting account
            comment-click edit-click delete-click] :as props} is-comment]
   (let [{:keys [id author]} note
         is-author     (= (:id author) (:id account))]
     [:div.actions
      (when-not is-comment
        [note-action
         {:on-click #(comment-click id)}
         [:span
          [ant/icon {:type (if is-commenting "up" "down")}]
          " Comment"]])
      (when is-author
        [note-action
         {:on-click #(edit-click id)}
         "Edit"])
      (when is-author
        [note-action
         {:class    "text-red"
          :on-click (fn []
                      (ant/modal-confirm
                       {:title   "Are you sure?"
                        :content "This cannot be undone!"
                        :on-ok   #(delete-click id)}))}
         "Delete"])])))


(defn- note-byline [{:keys [created updated author]}]
  (let [updated (when (not= created updated) updated)]
    [:p.byline
     (str "by " (:name author) " at "
          (format/date-time-short created)
          (when-some [d updated]
            (str " (updated at " (format/date-time-short d) ")")))]))


(declare note-content)


(defn- note-body
  ([props]
   [note-body props false])
  ([{:keys [note is-commenting] :as props} is-comment]
   (let [{:keys [id subject content comments]} note]
     [:article.media
      (when is-comment
        [:figure.media-left [ant/icon {:type "message"}]])
      [:div.media-content
       (when (some? subject) [:p.subject subject])
       [:p.body {:dangerouslySetInnerHTML {:__html (format/newlines->line-breaks content)}}]
       (note-byline note)
       [note-actions props is-comment]
       (when is-commenting
         #_[comment-form note])
       (map
        #(with-meta [note-content (assoc props :note %) true] {:key (:id %)})
        comments)]])))


(defn- note-content
  ([props]
   [note-content props false])
  ([{:keys [note is-editing] :as props} is-comment]
   (if-not is-editing
     ;; not editing
     [note-body props is-comment]
     ;; is editing
     #_[edit-note-form note is-comment])))


(defn note-card [props]
  [ant/card {:class "note"}
   [note-content props]])




;; ==============================================================================
;; create note ==================================================================
;; ==============================================================================


(defn create-note-footer [form on-cancel on-submit]
  [:div
   [ant/button
    {:size     :large
     :on-click on-cancel}
    "Cancel"]
   [ant/button
    {:type     :primary
     :size     :large
     :on-click #(on-submit form)}
    "Create"]])


(defn create-note-modal [props]
  (r/create-class
   {:component-will-mount
    (fn [_]
      )
    :reagent-render
    (fn [{:keys [is-creating form accounts properties
                on-cancel on-change on-submit]}]
      [ant/modal
       {:title     "Create a note"
        :visible   is-creating
        :on-cancel on-cancel
        :footer    (r/as-element [create-note-footer form on-cancel on-submit])}
       [ant/form-item
        {:label "Mentions"}
        [ant/select
         {:style     {:width "100%"}
          :mode      "multiple"
          :value     (mapv str (:refs form))
          :on-change #(let [ids (mapv tb/str->int (js->clj %))]
                        (on-change :refs ids))}
         (map
          (fn [{:keys [name id]}]
            [ant/select-option
             {:value (str id)
              :key   id}
             name])
          accounts)]]
       [ant/form-item
        {:label "Subject"}
        [ant/input
         {:placeholder "Note subject"
          :value       (:subject form)
          :on-change   #(on-change :subject (.. % -target -value))}]]
       [ant/form-item
        {:label "Note"}
        [ant/input
         {:type        :textarea
          :rows        10
          :placeholder "Note body"
          :value       (:content form)
          :on-change   #(on-change :content (.. % -target -value))}]]
       (when (some? (:notify form))
         [ant/form-item
          [ant/checkbox {:checked   (:notify form)
                         :on-change #(on-change :notify (.. % -target -checked))}
           "Send Slack notification"]])])}))
