(ns iface.components.ptm.ui.cards
  (:require [cljs.spec.alpha :as s]
            [reagent.core :as r]
            [devtools.defaults :as d]
            [toolbelt.core :as tb]
            [antizer.reagent :as ant]))


;; helpers ==============================


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


(defn- card-data [tag title subtitle description footer]
  [:div
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
      footer])])


;; single cards =================================================================


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
    [card-data tag title subtitle description footer]]])


(defn single-h1 [{:keys [value on-click title subtitle footer width disabled]
                  :or   {width :third}}]
  [:div
   {:class (get-card-width width)}
   [:div.card.card-number.centered
    {:value    value
     :on-click (when-let [c on-click]
                 #(c value))
     :class    (when-not disabled
                 "card-interactive")}
    [:div.card-illo
     [:h1.mt2-ns.mt0.mb0 title]
     [:h4.ma0 subtitle]]
    (when footer
      [:div.card-footer
       footer])]])


;; multiple selection cards =====================================================


(defn- get-selection-footer [selected count]
  (cond
    (and selected count)
    [:div
     [:a.text-link.text-green
      (str count " Selected")]
     [:img.icon-x {:src "/assets/images/ptm/icons/ic-x.svg"}]]

    selected
    [:div
     [:a.text-link.text-green
      [ant/icon {:type "check"}] " Selected"]
     [:img.icon-x {:src "/assets/images/ptm/icons/ic-x.svg"}]]

    :else
    [:a.text-link "Select"]))


(defn multiple [{:keys [width selected count]
                 :or   {width :third}
                 :as   props}]
  [single
   (r/merge-props
    props
    {:align  :left
     :footer (get-selection-footer selected count)})])


;; carousel =====================================================================


(defn- carousel-next [{on-click :on-click}]
  [:span.chevron.next
   {:on-click #(on-click)}
   [:img {:src "/assets/images/ptm/chevron.svg"}]])


(defn- carousel-back [{on-click :on-click}]
  [:span.chevron.back
   {:on-click #(on-click)}
   [:img {:src "/assets/images/ptm/chevron.svg"}]])


(defn- carousel-dot [{:keys [active]}]
  [:li.dot {:class (when-not active "inactive")}])


(defn- carousel-slide [{:keys [selected img key]}]
  [:li.carousel-slide
   {:class (when selected
             "active")
    :key   key}
   [:img {:src img}]])


(defn- idx-next [idx total]
  (if (= idx (- total 1)) 0 (inc idx)))


(defn- idx-back [idx total]
  (if (zero? idx) (dec total) (dec idx)))


(defn- carousel [images]
  (let [index (r/atom 0)]
    (fn []
      [:div.card-photo.aspect-ratio--6x4
       {:style {:overflow   "hidden"}}
       [:ul.dots
        (doall
         (map-indexed
          #(with-meta
             [carousel-dot
              {:active (when (= %1 @index) true)}]
             {:key %1})
          images))]
       [carousel-next {:on-click #(swap! index idx-next (count images))}]
       [carousel-back {:on-click #(swap! index idx-back (count images))}]
       [:div.chevron-scrim]
       [:ul
        (doall
         (map-indexed
          (fn [idx image]
            [carousel-slide {:img      image
                             :selected (when (= idx @index) true)
                             :key      idx}])
          images))]])))


(defn carousel-card [{:keys [value tag title subtitle description align on-click
                             footer width images selected count]
                      :or   {align :left
                             width :third}}]
  [:div
   {:class (get-card-width width)}
   [:div.card.card-interactive
    {:value value
     :class (get-card-align align)}
    [carousel images]
    [:div
     {:on-click (when-let [c on-click]
                  #(c value))}
     [card-data tag title subtitle description (get-selection-footer selected count)]]]])


;; group ========================================================================


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
                           :selected (when (and (coll? value)
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


;; summary cards ================================================================


;; logistics ================================


(defn- summary-item
  "Summary information item"
  [{:keys [label value edit on-click]}]
  [:div.w-50-l.w-100.fl.ph4
   [:h4.w-50.mv1.fl label]
   [:p.w-50.fl.tr.mv0 value
    (when edit
      [:img.icon-edit {:src      "/assets/images/ptm/icons/ic-edit.svg"
                       :on-click (when-let [c on-click]
                                   #(c))}])]])


(defn- summary-row
  "Row of 2 summary items"
  [items]
  [:div.w-100.cf
   (map-indexed
    (fn [i item]
      ^{:key i}
      [summary-item item])
    items)])


(defn logistics-summary
  "To be used to display move-in summary. This card shows all the logistics selections."
  [title items]
  [:div.w-100.pr4-l.pr0
   [:div.card.cf
    ;; header
    [:div.w-25-l.w-100.fl.pv0.card-top
     [:h2.ma0 title]]
    ;; body
    [:div.w-75-l.w-100.fl.pv3
     (map-indexed
      (fn [i row-items]
        ^{:key i}
        [summary-row row-items])
      (partition 2 2 nil items))]]])


;; unit selection =======================


(defn- toggle [v]
  (if v
    false
    true))


(defn community-selection
  "To be used to display a summary of a community selection, and it's cost breakdown."
  [community units]
  (let [tooltip (r/atom false)]
    (fn [community units]
      [:div.w-50-l.w-100.fl.pr4-l.pr0
       [:div.card
        ;; header
        [:div.card-top
         [:h2.mt0 community]
         [:div.cf
          [:h4.w-70.mv1.fl "Preferred Unit Selections"]
          [:p.w-30.fl.tr.mv0 units]]]
        ;; footer
        [:div.card-footer
         [:h3 "Cost Breakdown"]
         ;; line item
         [:div.cf
          [:h4.w-60.mv1.fl "Suite Fee"
           [:a {:onMouseOver #(swap! tooltip toggle)
                :onMouseOut  #(swap! tooltip toggle)}
            [ant/tooltip {:title     "This is a very long-winded tooltip. We need this to explain the very complicated price breakdown we have here that not even I understand"
                          :placement "right"
                          :visible   @tooltip}
             [:img.icon-small {:src "/assets/images/ptm/icons/ic-help-tooltip.svg"}]]]]
          ]]
        ]])))
