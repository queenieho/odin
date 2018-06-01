(ns admin.properties.views
  (:require [admin.content :as content]
            [admin.routes :as routes]
            [antizer.reagent :as ant]
            [cljs.core.match :refer-macros [match]]
            [clojure.string :as string]
            [iface.components.typography :as typography]
            [iface.loading :as loading]
            [iface.utils.formatters :as format]
            [iface.utils.time :as time]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [toolbelt.core :as tb]))

;; What do we want to be able to see in a property's detail view?

;; 1. List of units:
;;   - Who lives in them (avatar, name, email)
;;   - What the unit rates are per-term
;;   - Searchable by occupant name or room number
;;   - Can edit unit rates
;;
;; 2. Building financials:
;;   - Rent overview (payment totals paid, pending, unpaid)
;;   - Premium service income
;;   - Security deposit overview
;;   - Operational fees for rent & services modifications
;;   - Default rates per term modifiable



;; ==============================================================================
;; components ===================================================================
;; ==============================================================================


(defn address-input [{:keys [address]}]
  (let [on-change #(dispatch [:community.create.form.address/update %1 %2])]
    [:div
     [:p.fs2.bold "Address"]
     [ant/form-item
      [ant/input
       {:placeholder "Line 1"
        :value       (:lines address)
        :on-change   #(on-change :lines (.. % -target -value))}]]
     #_[ant/form-item
        [ant/input
         {:placeholder "Line 2"
          :value       (:line-2 address)
          :on-change   #(on-change :line-1 (.. % -target -value))}]]
     [:div.columns
      [:div.column.is-6
       [ant/form-item
        [ant/input
         {:placeholder "City/town"
          :value       (:locality address)
          :on-change   #(on-change :locality (.. % -target -value))}]]]
      [:div.column.is-6
       [ant/form-item
        [ant/input
         {:placeholder "State/province/region"
          :value       (:region address)
          :on-change   #(on-change :region (.. % -target -value))}]]]]
     [:div.columns
      [:div.column.is-6
       [ant/form-item
        [ant/input
         {:placeholder "Country"
          :value       (:country address)
          :on-change   #(on-change :country (.. % -target -value))}]]]
      [:div.column.is-6
       [ant/form-item
        [ant/input
         {:placeholder "Postal code"
          :value       (:postal-code address)
          :on-change   #(on-change :postal-code (.. % -target -value))}]]]]]))


(defn license-prices-input [{:keys [license-prices]}]
  (let [on-change #(dispatch [:community.create.form.license-price/update %1 %2])]
    [:div.columns
     (doall
      (for [term [3 6 12]]
        ^{:key term}
        [:div.column.is-4
         [ant/form-item
          {:label (str term " months")}
          [ant/input-number
           {:value     (get license-prices term)
            :on-change #(on-change term %)}]]]))]))


(defn create-community-modal []
  (let [form      @(subscribe [:community.create/form :community])
        on-change #(dispatch [:community.create.form/update %1 %2])]
    [ant/modal
     {:title     "Create New Community"
      :width     "60%"
      :visible   @(subscribe [:modal/visible? :communities.create/modal])
      :on-ok     #(dispatch [:community/create!] #_[:communities.create/upload-cover-photo!])
      :on-cancel #(dispatch [:modal/hide :communities.create/modal])}

     [ant/card
      {:title "General information"}
      [:div.columns
       [:div.column.is-6
        [ant/form-item
         {:label "Community name"}
         [ant/input
          {:placeholder "Community name"
           :value       (:name form)
           :on-change   #(on-change :name (.. % -target -value))}]]]
       [:div.column.is-6
        [ant/form-item
         {:label "Code"}
         [ant/input
          {:placeholder "ex. 52-gilbert"
           :value       (:code form)
           :on-change   #(on-change :code (.. % -target -value))}]]]]
      [address-input form]
      [:hr]
      [:div.columns
       [:div.column.is-6
        [ant/form-item
         {:label "Number of units"}
         [ant/input-number
          {:value     (:units form)
           :on-change #(on-change :units %)}]]]
       [:div.column.is-6
        [ant/form-item
         {:label "When will it be available?"}
         [ant/date-picker
          ;; when i delete the date i get NaN
          {:value     (when (:date form)
                        (time/iso->moment (:date form)))
           :on-change #(on-change :date (time/moment->iso %))}]]]]]

     [ant/card
      {:title "License prices"}
      [license-prices-input form]]

     [ant/card
      {:title "Community cover photo"}
      [ant/form-item
       {:label "Upload cover photo"}
       [:input
        {:type      "file"
         :multiple  true
         :on-change #(dispatch [:communities.create/cover-image-picked (.. % -currentTarget -files)])}]]]]))


(defn create-teller-community-modal []
  (let [{:keys [deposit ops]} @(subscribe [:community.create/form :teller])
        on-change             #(dispatch [:community.create.form.teller/update %1 %2])]
    [ant/modal
     {:title     "Create New Teller Property"
      :width     "60%"
      :visible   @(subscribe [:modal/visible? :communities.teller.create/modal])
      :on-cancel #(dispatch [:modal/hide :communities.teller.create/modal])}

     [ant/card
      {:title "Deposit Account Information"}
      [:div.columns
       [:div.column.is-6
        [ant/form-item
         {:label "Account Number"}
         [ant/input
          {:placeholder "account number"
           :value       (:account deposit)
           :on-change   #(on-change [:deposit :account] (.. % -target -value))}]]]
       [:div.column.is-6
        [ant/form-item
         {:label "Routing Number"}
         [ant/input
          {:placeholder "routing number"
           :value       (:routing deposit)
           :on-change   #(on-change [:deposit :routing] (.. % -target -value))}]]]]
      [:div.columns
       [:div.column.is-6
        [ant/form-item
         {:label "Account holder name"}
         [ant/input
          {:placeholder "John Doe"
           :value       (:name deposit)
           :on-change   #(on-change [:deposit :name] (.. % -target -value))}]]]
       [:div.column.is-6
        [ant/form-item
         {:label "Last 4 SSN"}
         [ant/input
          {:placeholder "0000"
           :value       (:ssn deposit)
           :on-change   #(on-change [:deposit :ssn] (.. % -target -value))}]]]]
      [:div.columns
       [:div.column.is-6
        [ant/form-item
         {:label "Account holder date of birth"}
         [ant/date-picker
          {:value     (when (:dob deposit)
                        (time/iso->moment (:dob deposit)))
           :on-change #(on-change [:deposit :dob] (time/moment->iso %))}]]]]]

     [ant/card
      {:title "Ops Account Information"}
      [:div.columns
       [:div.column.is-6
        [ant/form-item
         {:label "Account Number"}
         [ant/input
          {:placeholder "account number"
           :value       (:account ops)
           :on-change   #(on-change [:ops :account] (.. % -target -value))}]]]
       [:div.column.is-6
        [ant/form-item
         {:label "Routing Number"}
         [ant/input
          {:placeholder "routing number"
           :value       (:routing ops)
           :on-change   #(on-change [:ops :routing] (.. % -target -value))}]]]]
      [ant/form-item
       [ant/checkbox
        {:checked   (:same ops true)
         :on-change #(on-change [:ops :same] (.. % -target -checked))}
        "Same account holder as deposit account"]]
      (when (= false (:same ops))
        [:div
         [:div.columns
          [:div.column.is-6
           [ant/form-item
            {:label "Account holder name"}
            [ant/input
             {:placeholder "John Doe"
              :value       (:name ops)
              :on-change   #(on-change [:ops :name] (.. % -target -value))}]]]
          [:div.column.is-6
           [ant/form-item
            {:label "Last 4 SSN"}
            [ant/input
             {:placeholder "0000"
              :value       (:ssn ops)
              :on-change   #(on-change [:ops :ssn] (.. % -target -value))}]]]]
         [:div.columns
          [:div.column.is-6
           [ant/form-item
            {:label "Account holder date of birth"}
            [ant/date-picker
             {:value     (when (:dob ops)
                           (time/iso->moment (:dob ops)))
              :on-change #(on-change [:ops :dob] (time/moment->iso %))}]]]]])]]))


(defn property-card
  "Display a property as a card form."
  [{:keys [id name cover-image-url href is-loading has-financials]
    :or   {is-loading false, href "#"}
    :as   props}]
  [ant/card {:class   "is-flush"
             :loading is-loading}
   (.log js/console name has-financials)
   [:div.card-image
    [:figure.image
     [:a {:href href}
      [:img {:src    cover-image-url
             :style {:height "196px"}}]]]]

   [:div.card-content
    [:div.content
     [:h5.title.is-5 name]
     [:a {:href href}
      "Details "
      [ant/icon {:type "right"}]]
     (when-not has-financials
       [:a.text-red.align-right
        {:on-click #(dispatch [:communities.create.form.teller/show id])}
        "Add financials "
        [ant/icon {:type "right"}]])]]])


(defn- unit-list-item
  [active {:keys [id name href occupant on-click]}]
  [:a.unit-list-item
   (tb/assoc-when
    {}
    :on-click (when-some [f on-click] #(f id))
    :href href)
   [:li
    {:class (when (= id active) "is-active")}
    [:span.unit-name name]
    [:span.divider {:dangerouslySetInnerHTML {:__html "&#10072;"}}]
    (if-let [occupant occupant]
      [:span.occupied
       [:span.occupant-name (:name occupant)] " until "
       [:span.date (-> occupant :ends format/date-short-num)]]
      [:span.unoccupied "vacant"])]])


(defn- matches-query? [q {:keys [name occupant]}]
  (let [s (string/lower-case (str name " " (:name occupant)))]
    (string/includes? s (string/lower-case q))))


(defn units-list
  "Display a list of units. Can provide the following:

  - `units`: the units to render
  - `page-size`: number of units to show per-page
  - `active`: the `id` of the active unit
  - `on-click`: callback that will be passed the id of the clicked unit"
  [{:keys [units page-size active on-click]
    :or   {page-size 10, active false}}]
  (let [state (r/atom {:current 1 :q ""})]
    (fn [{:keys [units page-size active on-click]
         :or   {page-size 10, active false}}]
      (let [{:keys [current q]} @state
            units'               (->> (drop (* (dec current) page-size) units)
                                      (take (* current page-size))
                                      (filter (partial matches-query? q)))]
        [:div.admin-property-unit-list
         [ant/input {:placeholder "search units by name or occupant"
                     :class       "search-bar"
                     :on-change   #(swap! state assoc :q (.. % -target -value))
                     :suffix (r/as-element [ant/icon {:type :search}])}]
         [:ul.unit-list
          (if (empty? units')
            [:div.has-text-centered {:style {:margin "24px 0"}} "No matches"]
            (map-indexed
             #(with-meta
                [unit-list-item active (assoc %2 :on-click on-click)]
                {:key %1})
             units'))]
         [:div.mt1
          [ant/pagination
           {:size      "small"
            :current   current
            :total     (count units)
            :showTotal (fn [total] (str total " units"))
            :on-change #(swap! state assoc :current %)}]]]))))


(defn rate-input
  "An input for manipulating the rate for a given term."
  [{:keys [term value on-change]}]
  [ant/form-item {:label (str term " Month Rate")}
   [ant/input-number {:formatter #(string/replace (str "$ " %) #"\B(?=(\d{3})+(?!\d))" ",")
                      :parser    #(string/replace % #"\$\s?|(,*)" "")
                      :value     value
                      :on-change on-change}]])


(defn rate-form
  "A form for manipulating the `rates` for multiple terms, for e.g. a property or unit."
  [{:keys [rates on-change on-submit can-submit is-loading]
    :or   {on-change  (constantly nil)
           on-submit  (constantly nil)
           can-submit true}
    :as   opts}]
  [ant/card
   [ant/form
    [:div.columns
     (for [{:keys [term rate] :as r} rates]
       ^{:key term}
       [:div.column
        [rate-input {:value     rate
                     :term      term
                     :on-change (fn [amount]
                                  (on-change r amount))}]])]
    [ant/button
     {:type     :primary
      :disabled (not can-submit)
      :loading  is-loading
      :on-click on-submit}
     "Save"]]])


;; ==============================================================================
;; entry layout =================================================================
;; ==============================================================================


;; units subview ================================================================


(defn- unit->units-list-unit
  [property-id {:keys [id name number occupant]}]
  (tb/assoc-when
   {:id   id
    :name name
    :href (routes/path-for :properties.entry.units/entry
                           :property-id property-id
                           :unit-id id)}
   :occupant (when (some? occupant)
               {:name (:name occupant)
                :ends (get-in occupant [:active_license :ends])})))


(defn- units-rate-form
  [property-id unit-id]
  (let [rates      (subscribe [:property.unit/rates property-id unit-id])
        can-submit (subscribe [:property.unit.rates/can-submit? property-id unit-id])
        is-loading (subscribe [:ui/loading? :property.unit.rates/update!])]
    [:div.column
     [rate-form
      {:rates      @rates
       :can-submit @can-submit
       :is-loading @is-loading
       :on-change  #(dispatch [:property.unit/update-rate unit-id %1 %2])
       :on-submit  #(dispatch [:property.unit.rates/update! property-id unit-id])}]]))


(defn units-subview
  [property-id & {:keys [active]}]
  (let [units (subscribe [:property/units property-id])]
    [:div.columns
     [:div.column.is-one-third
      [units-list
       {:active active
        :units  (map (partial unit->units-list-unit property-id) @units)}]]
     (when (some? active)
       [units-rate-form property-id active])]))


;; overview subview =============================================================


(defn overview-subview
  [property-id]
  (let [property   (subscribe [:property property-id])
        rates      (subscribe [:property/rates property-id])
        can-submit (subscribe [:property.rates/can-submit? property-id])
        is-loading (subscribe [:ui/loading? :property.rates/update!])]
    [:div.columns
     [:div.column.is-half
      [ant/card
       [:p.title.is-4 "Controls"]
       [ant/form-item {:label "Touring Enabled"}
        [ant/switch
         {:checked   (:tours @property)
          :on-change #(dispatch [:property.tours/toggle! property-id])}]]]]
     [:div.column
      [rate-form
       {:rates      @rates
        :can-submit @can-submit
        :is-loading @is-loading
        :on-change  #(dispatch [:property/update-rate property-id %1 %2])
        :on-submit  #(dispatch [:property.rates/update! property-id])}]]]))


;; subview management ===========================================================


(defn- path->selected
  [path]
  (case (vec (rest path))
    [:entry]               :entry
    [:entry :units]        :units
    [:entry :units :entry] :units
    :entry))


(defn menu [property-id path]
  [ant/menu {:mode          :horizontal
             :selected-keys [(path->selected path)]}
   [ant/menu-item {:key :entry}
    [:a {:href (routes/path-for :properties/entry :property-id property-id)}
     "Overview"]]
   [ant/menu-item {:key :units}
    [:a {:href (routes/path-for :properties.entry/units :property-id property-id)}
     "Units"]]])


(defmethod content/view :properties [{:keys [path params]}]
  (let [property-id (tb/str->int (:property-id params))
        property    (subscribe [:property property-id])]
    [:div.container
     (typography/view-header (:name @property))
     [menu property-id path]
     [:div.mt2
      (let [path (vec (rest path))]
        (match [path]
          [[:entry]]               [overview-subview property-id]
          [[:entry :units]]        [units-subview property-id]
          [[:entry :units :entry]] [units-subview property-id
                                    :active (tb/str->int (:unit-id params))]
          :else [:p "unmatched"]))]]))


;; ==============================================================================
;; list layout ==================================================================
;; ==============================================================================


(defn community-row [communities]
  [:div.columns
   (map
    (fn [{:keys [id name cover_image_url units has_financials]}]
      ^{:key id}
      [:div.column.is-4
       [property-card
        {:id              id
         :name            name
         :cover-image-url cover_image_url
         :href            (routes/path-for :properties/entry :property-id id)
         :has-financials  has_financials}]])
    communities)])


(defn communities-list []
  (let [communities (subscribe [:properties/list])]
    [:div
     (map-indexed
      (fn [i plist]
        ^{:key i}
        [community-row plist])
      (partition 3 3 nil @communities))]))


(defmethod content/view :properties/list [_]
  (let [is-loading (subscribe [:ui/loading? :properties/query])]
    [:div
     [create-teller-community-modal]
     [create-community-modal]
     (typography/view-header "Communities" "Manage and view our communities.")
     (if @is-loading
       (loading/fullpage "Loading properties...")
       [:div
        [ant/button
         {:style    {:margin-bottom "20px"}
          :icon     "plus"
          :type     "primary"
          :size     "large"
          :on-click #(dispatch [:modal/show :communities.create/modal])}
         "Add New Community"]
        [communities-list]])]))
