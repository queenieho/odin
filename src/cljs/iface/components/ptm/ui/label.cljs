(ns iface.components.ptm.ui.label
  (:require [reagent.core :as r]
            [cljs.spec.alpha :as s]))


(defn label [props]
  (into [:div.pill]
        (r/children (r/current-component))))
