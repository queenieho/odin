(ns iface.components.ptm.ui.tag
  (:require [reagent.core :as r]
            [cljs.spec.alpha :as s]
            [antizer.reagent :as ant]
            [toolbelt.core :as tb]))


(defn select [{:keys [selected on-click value]}]
  (into [:div.pill.mr1
         {:on-click (when-let [c on-click]
                      #(c value))
          :value    value
          :class    (if selected
                      "pill-active"
                      "pill-inactive")}]
        (r/children (r/current-component))))


(defn group-select [{:keys [value on-change] :as props}]
  (let [children (map
                  #(update % 1 tb/assoc-when
                           :selected (when
                                         (some (fn [v] (= v (:value (second %)))) value)
                                       true)
                           :on-click (fn [v] (on-change v)))
                  (first (r/children (r/current-component))))]
    (into [:div
           {:value value}]
          children)))


(defn delete [{:keys [value values on-click]}]
  [:div.pill.pill-active.mr1
   (into [:span
         {:value value}]
         (r/children (r/current-component)))
   [:span.delete.ml1
    {:on-click #(on-click value)}
    "×"]])


(defn group-delete [{:keys [value on-change]}]
  (let [children (map
                  #(update % 1 tb/assoc-when
                          :values value
                          :on-click (fn [v] (on-change v)))
                  (first (r/children (r/current-component))))]
    (into [:div
           {:value value}]
          children)))
