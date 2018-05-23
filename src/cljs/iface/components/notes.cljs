(ns iface.components.notes
  (:require [antizer.reagent :as ant]
            [reagent.core :as r]
            [toolbelt.core :as tb]))


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
