(ns iface.components.notes
  (:refer-clojure :exclude [comment])
  (:require [antizer.reagent :as ant]
            [iface.utils.formatters :as format]
            [reagent.core :as r]
            [toolbelt.core :as tb]
            [clojure.string :as string]
            [admin.routes :as routes]))


;; ==============================================================================
;; notes ui =====================================================================
;; ==============================================================================


(defn- note-byline [{:keys [created updated author]}]
  (let [updated (when (not= created updated) updated)]
    [:p.byline
     (str "by " (:name author) " at "
          (format/date-time-short created)
          (when-some [d updated]
            (str " (updated at " (format/date-time-short d) ")")))]))


(defn- get-route [type id]
  (case type
    :account   (routes/path-for :accounts/entry :account-id id)
    :property  (routes/path-for :properties/entry :property-id id)
    :otherwise nil))


(defn- note-mentions [refs]
  (let [{:keys [name id type]} (last refs)]
    [:span
    [:p.fs1
     [:b "Mentions: "]
     (map
      (fn [{:keys [name id type]}]
        ^{:key id}
        [:span
         [:a {:href (get-route type id)} name]
         ", "])
      (butlast refs))
     [:a {:href (get-route type id)} name]]]))


(defn note-action [props text]
  [:a (merge {:href ""} props)
   text])


(defn- note-actions
  [{:keys [note is-author is-commenting on-comment on-edit on-delete] :as note}]
  [:div.actions
   [note-action
    {:on-click #(on-comment (:id note))}
    [:span
     [ant/icon {:type (if is-commenting "up" "down")}]
     " Comment"]]
   (when is-author
     [note-action
      {:on-click #(on-edit note)}
      "Edit"])
   (when is-author
     [note-action
      {:class    "text-red"
       :on-click (fn []
                   (ant/modal-confirm
                    {:title   "Are you sure?"
                     :content "This cannot be undone!"
                     :on-ok   #(on-delete (:id note))}))}
      "Delete"])])


(defn body-text [{:keys [note] :as props}]
  (let [{:keys [refs subject content]} note]
    [:div
     (when (some? refs) [note-mentions refs])
     (when (some? subject) [:p.subject subject])
     [:p.body {:dangerouslySetInnerHTML {:__html (format/newlines->line-breaks content)}}]
     [note-byline note]
     [note-actions props]]))


(defn comment [{:keys [content] :as note}]
  [:article.media
   [:figure.media-left [ant/icon {:type "message"}]]
   [:div.media-content
    [:p.body {:dangerouslySetInnerHTML {:__html (format/newlines->line-breaks content)}}]
    [note-byline note]]])


(defn comment-form [{:keys [note-id comment on-change on-ok]}]
  [:div
   [ant/form-item
    {:label "Comment"}
    [ant/input
     {:type        :textarea
      :rows        5
      :placeholder "Note comment"
      :value       comment
      :on-change   #(on-change note-id (.. % -target -value))}]]
   [ant/button
    {:on-click #(on-ok note-id comment)}
    "Comment"]])


(defn edit-form [{:keys [note-id form on-change on-cancel on-ok]}]
  [:div
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
      :rows        5
      :placeholder "Note body"
      :value       (:content form)
      :on-change   #(on-change :content (.. % -target -value))}]]
   [ant/button
    {:on-click #(on-cancel note-id)}
    "Cancel"]
   [ant/button
    {:on-click #(on-ok form)}
    "Save Edit"]])


;; ==============================================================================
;; create note ==================================================================
;; ==============================================================================


(defn create-button [{:keys [on-click]}]
  [ant/button
   {:style    {:margin "auto"}
    :type     :primary
    :size     :large
    :icon     :plus
    :on-click #(on-click)}
   "Create note"])


(defn create-modal-footer [form on-cancel on-submit can-submit loading]
  [:div
   [ant/button
    {:size     :large
     :on-click on-cancel}
    "Cancel"]
   [ant/button
    {:type     :primary
     :size     :large
     :loading  loading
     :disabled can-submit
     :on-click #(on-submit form)}
    "Create"]])


(defn- get-name [opt]
  (goog.object/getValueByKeys opt "props" "children"))


(defn create-modal [{:keys [is-creating form members properties loading
                                 on-cancel on-change on-submit can-submit]}]
  [ant/modal
   {:title     "Create a note"
    :visible   is-creating
    :on-cancel on-cancel
    :footer    (r/as-element [create-modal-footer form on-cancel on-submit can-submit loading])}

   [ant/form-item
    {:label "Mentions"}
    [ant/select
     {:style         {:width "100%"}
      :mode          "multiple"
      :value         (mapv str (:refs form))
      :filter-option (fn [val opt]
                       (let [q (string/lower-case (get-name opt))]
                         (string/includes? q (string/lower-case val))))
      :on-change     #(let [ids (mapv tb/str->int (js->clj %))]
                        (on-change :refs ids))}
     [ant/select-opt-group
      {:label "Members"}
      (map
       (fn [{:keys [name id]}]
         [ant/select-option
          {:value (str id)
           :key   id}
          name])
       members)]
     [ant/select-opt-group
      {:label "Communities"}
      (map
       (fn [{:keys [name id]}]
         [ant/select-option
          {:value (str id)
           :key   id}
          name])
       properties)]]]
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
       "Send Slack notification"]])])
