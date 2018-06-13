(ns iface.components.ptm.ui.form
  (:require [cljs.spec.alpha :as s]
            [reagent.core :as r]
            [devtools.defaults :as d]))


(defn form []
  (into [:form] (r/children (r/current-component))))


;; is there a way to pass props from form item into it's children?
;; this way we can pass error states from here instead of from outside
(defn form-item [{:keys [label optional help error] :as props}]
  [:div
   [:label {:for label} label]
   (when optional
     [:span.small [:em " Optional"]])
   (into [:div]
         ;; if error == true, give input the error style
         (r/children (r/current-component)))
   (when help
     [:p.small.red help])])


(defn text [{:keys [error] :as props}]
  (let [props' (-> props
                   (merge {:type "text"
                           :class (when error "error")})
                   (dissoc :error))]
    [:input props']))


(defn textarea [{:keys [error] :as props}]
  (let [props' (-> props
                   (merge {:class (when error "error")})
                   (dissoc :error))]
    [:textarea props']))


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


(defn checkbox [{:keys [error] :as props}]
  (let [props' (-> props
                   (merge {:type "checkbox"})
                   (dissoc :error))]
    [:div {:class (if error "checkbox error" "checkbox")}
     (into [:label
            [:input props']
            [:span.checkbox-style]]
           (r/children (r/current-component)))]))


;; ideally we can implement radio groups like select and select options
;; but need to find out how to alter props in a child component
(defn radio [{:keys [] :as props}]
  (let [props' (-> props
                   (merge {:type "radio"})
                   (dissoc :label))]
    [:div.radio
     (into [:label
            [:input props']
            [:span.radio-style]]
           (r/children (r/current-component)))]))


(defn radio-group [{:keys [name options] :as props}]
  [:div
   (map
    #(with-meta
      [radio (assoc % :name name) (:label %)] {:key (:value %)})
    options)])
