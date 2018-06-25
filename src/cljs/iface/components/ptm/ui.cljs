(ns iface.components.ptm.ui
  (:require [antizer.reagent :as ant]
            [reagent.core :as r]
            [cljs.spec.alpha :as s]))


(defn pill [{:keys [active on-change on-delete selectable]}]
  (into [:div.pill {:class (str
                            (when active "pill-active")
                            (when (and (false? active) selectable) " pill-inactive")
                             )}]
        (r/children (r/current-component))))
