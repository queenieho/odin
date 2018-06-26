(ns iface.components.ptm.ui.label
  (:require [reagent.core :as r]
            [cljs.spec.alpha :as s]))


#_(defn label
  [props]
  (into [:div (r/merge-props {:class "pill"} props)] (r/children (r/current-component))))


#_(defn label
  ([children]
   (label {:class "pill"} (r/children (r/current-component))))
  ([props children]
   (into [:div (r/merge-props props {:class "pill"}) (r/children (r/current-component))])))

(defn label
  []
  (let [this  (r/current-component)
        props (r/props this)]
    (js/console.log "lookit dem porps" props)
    (js/console.log "lookit dem argv" (r/argv this))
    (js/console.log "lookit dem childrens" (r/children this))
    [:div {:class "pill"} (r/as-element (r/children this))]))
