(ns iface.components.ptm.ui.cards
  (:require [cljs.spec.alpha :as s]
            [reagent.core :as r]
            [devtools.defaults :as d]
            [toolbelt.core :as tb]
            [antizer.reagent :as ant]))


(defn- get-card-alignment [type]
  (case type
    :center "centered"
    :left   ""
    ""))


;; it would be nice to have a full bleed image style
(defn single [{:keys [value tag img title subtitle description alignment
                      on-click footer]}]
  [ant/card
   (r/merge-props
    {:bodyStyle {:padding "0px"}
     :value     value
     :on-click  on-click
     :class     "card card-interactive"}
    {:class (get-card-alignment alignment)})
   (when img
     [:div.card-illo
      [:img {:src img}]])
   (when tag
     [:h4.top-0.right-1.absolute
      [:div.pill tag]])
   (when (or title subtitle description)
     [:div.card-description
      (when title [:h3.ma0 title])
      (when subtitle [:h4.ma0 subtitle])
      (when description [:p.mb0 description])])
   (when footer
     [:div.card-footer
      footer])])


(defn single-h1 [{:keys [on-click title subtitle footer]}]
  [ant/card
   {:bodyStyle {:padding "0px"}
    :on-click (when-let [c on-click] c)
    :class "card card-number card-interactive centered"}
   [:div.card-illo
    [:h1.mt2-ns.mt0.mb0 title]
    [:h4.ma0 subtitle]]
   (when footer
     [:div.card-footer
      footer])])


(defn- get-card-size [size]
  (case size
    :third "w-third-l w-100 fl pr4-l pr0"
    :half "w-50-l w-100 fl pr4-l pr0"
    "w-third-l w-100 fl pr4-l pr0"))


(defn- update-group-value [coll v]
  (if (some #(= v %) coll)
    (remove #(= v %) coll)
    (conj coll v)))


(defn test-select [{:keys [value tag img title subtitle description alignment
                           on-click footer size form multiple]}]
  [:div
   {:class (get-card-size size)}
   [ant/card
    (r/merge-props
     {:bodyStyle {:padding "0px"}
      :value     value
      :on-click  (if multiple
                   #(swap! form update :value update-group-value value)
                   #(swap! form assoc :value value))
      :class     "card card-interactive"}
     {:class (get-card-alignment alignment)})
    (when img
      [:div.card-illo
       [:img {:src img}]])
    (when tag
      [:h4.top-0.right-1.absolute
       [:div.pill tag]])
    (when (or title subtitle description)
      [:div.card-description
       (when title [:h3.ma0 title])
       (when subtitle [:h4.ma0 subtitle])
       (when description [:p.mb0 description])])
    (when footer
      [:div.card-footer
       footer])]])


(defn group [{:keys [on-change value multiple] :as props}]
  (let [form     (r/atom {:value value})
        children (map
                  #(update % 1 tb/assoc-when :form form :multiple multiple)
                  (r/children (r/current-component)))]
    (fn []
      [:div.cf.mt5
       (doall
        (map-indexed
         (fn [i c-group]
           (with-meta
             (into [:div.cf.mt2
                    {:value (:value @form)
                     :on-click #(on-change (:value @form))}]
                   c-group) {:key i}))
         (partition 3 3 nil children)))])))
