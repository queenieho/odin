(ns iface.components.notifications
  (:require [antizer.reagent :as ant]
            [reagent.core :as r]
            [re-frame.core :refer [reg-event-fx]]
            [toolbelt.core :as tb]))


(defn level->class-name
  "Acceptable inputs are :info, :warning, :success, and :danger."
  [level]
  (case level
    :danger  "is-danger"
    :warning "is-warning"
    :success "is-success"
    :info    "is-info"
    ""))

(defn banner
  "Renders a notification element on-screen, intended to communicate important messages."
  ([message]
   [banner message :nil true])
  ([message level]
   [banner message level true])
  ([message level is-cancelable]
   [:div.notification {:class (level->class-name level)}
    (if (= is-cancelable true) [:button.delete])
    message]))

(defn banner-danger [message]
  [banner message :danger false])

(defn banner-warning [message]
  [banner message :warning false])

(defn banner-info
  ([message]
   [banner-info message true])
  ([message cancelable]
   [banner message :info cancelable]))

(defn banner-success
  ([message]
   [banner-success message true])
  ([message cancelable]
   [banner message :success cancelable]))


(defn banner-icon
  [level]
  (case level
    :warning [ant/icon {:type "exclamation-circle-o"}]
    :danger [ant/icon {:type "exclamation-circle-o"}]
    [:span]))


(defn banner-global
  ([message]
   [banner-global message :info])
  ([message level]
   [:div.global-notification {:class (level->class-name level)}
    [banner-icon level]
    [:span message]])
  ([message level uri]
   [:a.global-notification {:class (level->class-name level)
                            :href  uri}
    [banner-icon level]
    [:span message]]))