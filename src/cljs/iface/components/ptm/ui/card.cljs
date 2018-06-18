(ns iface.components.ptm.ui.card
  (:require [cljs.spec.alpha :as s]
            [reagent.core :as r]
            [devtools.defaults :as d]
            [toolbelt.core :as tb]
            [antizer.reagent :as ant]))


(defn single [{:keys [tag img title subtitle description] :as props}]
  [ant/card
   {:class     "card card-interactive centered"
    :bodyStyle {:padding "0px"}
    }
   [:div.card-illo
    [:img {:src img}]]
   [:h3.ma0 title]
   [:h4.ma0 subtitle]
   [:p.mb0 description]])
