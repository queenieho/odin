(ns iface.components.ptm.ui.cards
  (:require [cljs.spec.alpha :as s]
            [reagent.core :as r]
            [devtools.defaults :as d]
            [toolbelt.core :as tb]
            [antizer.reagent :as ant]))


(defn- get-card-align [type]
  (case type
    :center " centered"
    :left   ""
    ""))


(defn- get-card-width [width]
  (case width
    :third "w-third-l w-100 fl pr4-l pr0"
    :half  "w-50-l w-100 fl pr4-l pr0"
    "w-third-l w-100 fl pr4-l pr0"))


;; it would be nice to have a full bleed image style
(defn single [{:keys [value tag img title subtitle description align
                      on-click footer width disabled]
               :or   {align :center
                      width :third}}]
  [:div
   {:class (get-card-width width)}
   [:div.card
    {:value    value
     :on-click (when-let [c on-click]
                 #(c value))
     :class    (str
                (when-not disabled
                  "card-interactive")
                (get-card-align align))}
    (when img
      [:div.card-illo
       [:img.v-mid {:src img}]])
    (when tag
      [:h4.top-0.right-1.absolute
       [:div.pill tag]])
    (when (or title subtitle description)
      [:div.card-description
       (when title [:h3.ma0 title])
       (when subtitle [:h4.ma0 subtitle])
       (when description [:p.mb0.mt3 description])])
    (when footer
      [:div.card-footer
       footer])]])


(defn single-h1 [{:keys [value on-click title subtitle footer width disabled]
                  :or   {width :third}}]
  [:div
   {:class (get-card-width width)}
   [:div.card.card-number.centered
    {:value    value
     :on-click (when-let [c on-click]
                 #(c value))
     :class    (when-not disabled
                 "card-interactive")
     }
    [:div.card-illo
     [:h1.mt2-ns.mt0.mb0 title]
     [:h4.ma0 subtitle]]
    (when footer
      [:div.card-footer
       footer])]])


(defn multiple [{:keys [width selected count]
                 :or   {width :third}
                 :as   props}]
  [single
   (r/merge-props
    props
    {:align  :left
     :footer (cond
               (and selected count) [:a.text-link.text-green
                                     (str count " Selected")]
               selected             [:a.text-link.text-green
                                     [ant/icon {:type "check"}] " Selected"]
               :else                [:a.text-link "Select"])})])


(def card-count
  {:half  2
   :third 3})


;; I built in the selection state into the group, not sure we want to do it this way
;; or leave it for the user to implement?
(defn group [{:keys [on-change value multiple card-width show-count]
              :or   {card-width :third}}]
  (let [n        (card-width card-count)
        children (map
                  #(update % 1 tb/assoc-when
                           :width card-width
                           :on-click (fn [val] (on-change val))
                           :selected (when (and (seq? value)
                                                (some (fn [v] (= (:value (second %)) v)) value))
                                       true)
                           :count (when show-count
                                    (count value)))
                  (r/children (r/current-component)))]
    [:div.cf.mt5
     (doall
      (map-indexed
       (fn [i c-group]
         (with-meta
           (into [:div.cf.mt2
                  {:value value}]
                 c-group) {:key i}))
       (partition n n nil children)))]))
