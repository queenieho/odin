(ns iface.components.ptm.ui.card
  (:require [cljs.spec.alpha :as s]
            [reagent.core :as r]
            [devtools.defaults :as d]
            [toolbelt.core :as tb]
            [antizer.reagent :as ant]
            [iface.utils.formatters :as format]))


;; specs ========================================================================


(s/def ::align
  #{:center :left})

(s/def ::card-width
  #{:third :half})

(s/def ::community
  string?)

(s/def ::count
  integer?)

(s/def ::description
  string?)

(s/def ::disabled
  boolean?)

(s/def ::footer
  (s/or :string string? :element vector?))

(s/def ::images
  coll?)

(s/def ::img
  string?)

(s/def ::items
  coll?)

(s/def ::line-items
  coll?)

(s/def ::on-change
  fn?)

(s/def ::on-click
  fn?)

(s/def ::selected
  boolean?)

(s/def ::show-count
  boolean?)

(s/def ::subtitle
  string?)

(s/def ::tag
  string?)

(s/def ::title
  string?)

(s/def ::total
  map?)

(s/def ::units
  integer?)

(s/def ::value
  some?)

(s/def ::width
  #{:third :half})


;; components ===================================================================


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
      (when-let [d description]
        (if (string? d)
          [:p.mb0.mt3 description]
          d))])
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

(s/fdef single
  :args (s/cat :props (s/keys :req-un [::value
                                       ::title]
                              :opt-un [::on-click
                                       ::tag
                                       ::img
                                       ::subtitle
                                       ::description
                                       ::align
                                       ::footer
                                       ::width
                                       ::disabled])))


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

(s/fdef single-h1
  :args (s/cat :props (s/keys :req-un [::value
                                       ::title
                                       ::subtitle]
                              :opt-un [::on-click
                                       ::footer
                                       ::width
                                       ::disabled])))


;; multiple selection cards =====================================================


(defn- get-selection-footer [selected count on-click value]
  (cond
    (and selected count)
    [:div
     {:on-click (when-let [c on-click]
                  #(c value))}
     [:a.text-link.text-green
      (str count " Selected")]
     [:img.icon-x {:src "/assets/images/ptm/icons/ic-x.svg"}]]

    selected
    [:div
     {:on-click (when-let [c on-click]
                  #(c value))}
     [:a.text-link.text-green
      [ant/icon {:type "check"}] " Selected"]
     [:img.icon-x {:src "/assets/images/ptm/icons/ic-x.svg"}]]

    :else
    [:div
     {:on-click (when-let [c on-click]
                  #(c value))}
     [:a.text-link "Select"]]))


(defn multiple [{:keys [width selected count on-click value on-card-click
                        disabled align img title subtitle description tag]
                 :or   {width :third
                        align :left}
                 :as   props}]
  [:div
   {:class (get-card-width width)}
   [:div.card.card-interactive
    [:div
     {:on-click (when-let [c on-card-click] c)
      :class    (get-card-align align)}
     (when img
       [:div.card-illo
        [:img.v-mid {:src img}]])
     [card-data tag title subtitle description nil]]
   [:div.card-footer
    (get-selection-footer selected count on-click value)]]])

(s/fdef single
  :args (s/cat :props (s/keys :req-un [::value
                                       ::title
                                       ::on-click]
                              :opt-un [::tag
                                       ::img
                                       ::subtitle
                                       ::description
                                       ::align
                                       ::width
                                       ::selected
                                       ::count])))


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
  [:li.carousel-slide.carousel-image-small
   {:style {:background-image (str "url('" img "')")}
    :class (when selected
             "active")
    :key   key}
   #_[:img {:src img}]])


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
                             width images selected count on-card-click]
                      :or   {align :left
                             width :third}}]
  [:div
   {:class (get-card-width width)}
   [:div.card.card-interactive
    {:value value
     :class (get-card-align align)}
    [carousel images]
    [:div
     {:on-click (when-let [c on-card-click] c)}
     [card-data tag title subtitle description nil]]
    [:div.card-footer
     (get-selection-footer selected count on-click value)]]])

(s/fdef carousel-card
  :args (s/cat :props (s/keys :req-un [::value
                                       ::title
                                       ::on-click
                                       ::images]
                              :opt-un [::tag
                                       ::subtitle
                                       ::description
                                       ::align
                                       ::width
                                       ::selected
                                       ::count])))


;; group ========================================================================


(def card-count
  {:half  2
   :third 3})


;; I built in the selection state into the group, not sure we want to do it this way
;; or leave it for the user to implement?
(defn group [{:keys [on-change value card-width show-count]
              :or   {card-width :third}}]
  (let [n        (card-width card-count)
        c        (r/children (r/current-component))
        children (map
                  #(update % 1 tb/assoc-when
                           :width card-width
                           :on-click (fn [val] (on-change val))
                           :selected (and (coll? value)
                                          (some (fn [v] (= (:value (second %)) v)) value))
                           :count (when show-count
                                    (count value)))
                  (if (map? (second (first c)))
                    c
                    (first c)))]
    [:div.cf.mt5
     (doall
      (map-indexed
       (fn [i c-group]
         (with-meta
           (into [:div.cf.mt2
                  {:value value}]
                 c-group) {:key i}))
       (partition n n nil children)))]))

(s/fdef group
  :args (s/cat :props (s/keys :req-un [::on-change
                                       ::value]
                              :opt-un [::card-width
                                       ::show-count])))


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
  [{:keys [title items]}]
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


(s/fdef logistics-summary
  :args (s/cat :props (s/keys :req-un [::title
                                       ::items])))


;; unit selection =======================


(defn- toggle [v]
  (if v
    false
    true))


(defn- line-label [label tooltip]
  (let [show (r/atom false)]
    (fn [label]
      [:h4.w-60.mv1.fl label
       (when tooltip
         [:a {:onMouseOver #(swap! show toggle)
              :onMouseOut  #(swap! show toggle)}
          [ant/tooltip {:title     tooltip
                        :placement "right"
                        :visible   @show}
           [:img.icon-small {:src "/assets/images/ptm/icons/ic-help-tooltip.svg"}]]])])))


(defn- line-item [type label tooltip cost & rest]
  (let [cstr  (format/currency cost)
        price (if (not-empty (remove nil? rest))
                (str cstr " - " (format/currency (first rest)))
                cstr)]
    [:div.cf
     [line-label label tooltip]
     (if (= type :line)
       [:p.w-40.fl.tr.mv0 price]
       [:h3.w-40.fl.tr.mt1.mb3 price])]))


(defn- community-breakdown
  [{:keys [community units on-click line-items total]}]
  [:div
   [:div.card-top
    [:h2.mt0 community]
    [:div.cf
     [:h4.w-70.mv1.fl "Preferred Unit Selections"]
     [:p.w-30.fl.tr.mv0 units
      (when-let [c on-click]
        [:img.icon-edit {:src      "/assets/images/ptm/icons/ic-edit.svg"
                         :on-click #(c)}])]]]
   [:div.card-footer
    [:h3 "Cost Breakdown"]
    ;; line items
    (map-indexed
     (fn [i {:keys [label tooltip price max]}]
       ^{:key i}
       [line-item :line label tooltip price max])
     line-items)
    [:hr]
    (let [{:keys [label tooltip price max]} total]
      [line-item :total label tooltip price max])]])

(s/fdef community-breakdown
  :args (s/cat :props (s/keys :req-un [::community
                                       ::units
                                       ::line-items
                                       ::total]
                              :opt-un [::on-click])))


(defn community-selection
  "To be used to display a summary of a community selection, and it's cost breakdown."
  [{:keys [community units on-click line-items total]
    :as   props}]
  [:div.w-50-l.w-100.fl.pr4-l.pr0
   [:div.card
    [community-breakdown props]]])

(s/fdef community-selection
  :args (s/cat :props (s/keys :req-un [::community
                                       ::units
                                       ::line-items
                                       ::total]
                              :opt-un [::on-click])))


(defn coapplicant-community-selection
  "Displays a summary of a selected community in the coapplicant's view."
  [{:keys [community units line-items total image]
    :as   props}]
  [:div.card
   [:div.w-60-l.w-100.fl.pv0
    [community-breakdown props]]
   [:div.w-40-l.w-100.fl.pv0.card-img-background
    {:style {:background-image (str "url('" image "')")}}]])

(s/fdef coapplicant-community-selection
  :args (s/cat :props (s/keys :req-un [::community
                                       ::units
                                       ::line-items
                                       ::total])))
