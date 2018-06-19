(ns iface.components.ptm.ui.alert
  (:require [reagent.core :as r]
            [cljs.spec.alpha :as s]))

;; spec =========================================================================


(s/def ::icon
  string?)

(s/def ::type
  #{:warning :info :success :danger})

(s/def ::message
  string?)


;; alert components ==================================================================


(defn- alert-style [type]
  (case type
    :warning "alert-yellow"
    :info "alert-blue"
    :success "alert-green"
    :danger "alert-red"
    ""))


(defn alert [{:keys [icon type]}]
  [:div.alert {:class (alert-style type)}
   (when icon
     [:div.alert-icon
      [:img.v-mid
       {:src icon}]])
   (into [:div.alert-text]
         (r/children (r/current-component)))])

(s/fdef alert
  :args (s/cat :props (s/keys :opt-un [::icon
                                       ::type])))


(defn alert-warning [{:keys [icon message] :as props}]
  [alert
   {:type :warning
    :icon icon}
   message])

(s/fdef alert-warning
  :args (s/cat :props (s/keys :req-un [::message]
                              :opt-un [::icon])))


(defn alert-info [{:keys [icon message] :as props}]
  [alert
   {:type :info
    :icon icon}
   message])

(s/fdef alert-info
  :args (s/cat :props (s/keys :req-un [::message]
                              :opt-un [::icon])))


(defn alert-success [{:keys [icon message] :as props}]
  [alert
   {:type :success
    :icon icon}
   message])

(s/fdef alert-success
  :args (s/cat :props (s/keys :req-un [::message]
                              :opt-un [::icon])))


(defn alert-danger [{:keys [icon message] :as props}]
  [alert
   {:type :danger
    :icon icon}
   message])

(s/fdef alert-danger
  :args (s/cat :props (s/keys :req-un [::message]
                              :opt-un [::icon])))
