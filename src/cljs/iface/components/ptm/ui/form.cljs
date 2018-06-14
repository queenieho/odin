(ns iface.components.ptm.ui.form
  (:require [cljs.spec.alpha :as s]
            [reagent.core :as r]
            [devtools.defaults :as d]
            [toolbelt.core :as tb]))


(defn form []
  (into [:form] (r/children (r/current-component))))


(defn form-item [{:keys [label optional help error] :as props}]
  [:div
   [:label {:for label} label]
   (when optional
     [:span.small [:em " Optional"]])
   (let [children (map
                   #(update % 1 tb/assoc-when :error error)
                   (r/children (r/current-component)))]
     (into [:div] children))
   (when help
     [:p.small.red help])])


(defn text [{:keys [error] :as props}]
  [:input (-> props
              (merge {:type  "text"
                      :class (when error "error")})
              (dissoc :error))])


(defn number [{:keys [error] :as props}]
  [:input (-> props
              (merge {:type  "number"
                      :class (when error "error")})
              (dissoc :error))])


(defn textarea [{:keys [error] :as props}]
  [:textarea (-> props
                 (merge {:class (when error "error")})
                 (dissoc :error))])


(defn select-option [{:keys [value] :as props}]
  (into [:option props]
        (r/children (r/current-component))))


(defn select [{:keys [error placeholder] :as props}]
  (let [props' (-> props
                   (assoc :default-value (when placeholder ""))
                   (dissoc :error :placeholder))]
    [:div.select {:class (when error "select error")}
     (into [:select props'
            (when placeholder
              [select-option
               {:value    ""
                :disabled true}
               placeholder])]
           (r/children (r/current-component)))]))


(defn checkbox [{:keys [error value id] :as props}]
  (let [props' (-> props
                   (merge {:type "checkbox"
                           ;; :checked (some #(= % id) value)
                           })
                   (dissoc :error))]
    [:div {:class (if error "checkbox error" "checkbox")}
     (into [:label
            [:input props']
            [:span.checkbox-style]]
           (r/children (r/current-component)))]))


(defn checkbox-group [{:keys [error name on-change value] :as props}]
  (let [children (map
                  #(update % 1 tb/assoc-when
                           :error error
                           :on-change on-change)
                  (r/children (r/current-component)))]
    (into [:div] children)))


(defn radio-option [{:keys [] :as props}]
  (let [props' (-> props
                   (merge {:type "radio"})
                   (dissoc :label))]
    [:div.radio
     (into [:label
            [:input props']
            [:span.radio-style]]
           (r/children (r/current-component)))]))


(defn radio-group [{:keys [name options] :as props}]
  (let [children (map
                  #(update % 1 tb/assoc-when :name name)
                  (r/children (r/current-component)))]
    (into [:div] children)))
