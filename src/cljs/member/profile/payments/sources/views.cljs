(ns member.profile.payments.sources.views
  (:require [antizer.reagent :as ant]
            [iface.components.form :as form]
            [iface.components.payments :as payments]
            [iface.components.typography :as typography]
            [iface.media :as media]
            [iface.tooltip :as tooltip]
            [member.l10n :as l10n]
            [member.profile.payments.sources.views.forms :as forms]
            [member.routes :as routes]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]
            [clojure.string :as str]))

(defn- is-unverified [{:keys [type status] :as source}]
  (and (= type :bank) (not= status "verified")))


(defn- is-default [{:keys [type default]}]
  (and (= type :card) (true? default)))


(defn account-digits
  [{:keys [type last4]}]
  (case type
    :card (str "xxxxxxxxxxxx" last4)
    (str "xxxxxx" last4)))


(defn add-new-source-button
  "Button for adding a new Payment Source."
  []
  [ant/button {:size     :large
               :icon     "plus-circle-o"
               :on-click #(dispatch [:modal/show :payment.source/add])}
   (l10n/translate :btn-add-new-account)])


(defn source-list-item
  [{:keys [id type name last4 default autopay] :as source}]
  (let [current (subscribe [:payment.sources/current])]
    [:a.source-list-item
     {:class (when (= id (:id @current)) "is-active")
      :href  (routes/path-for :profile.payment/sources :query-params {:source-id id})}

     [:div.source-list-item-info
      [:p.bold name]
      [:p (account-digits source)]
      (when (= type :card) [:span (:expires source)])

      [:div.source-item-end
       (if (is-unverified source)
         [:p.italic
          [ant/icon {:type "exclamation-circle" :style {:font-size ".8rem"}}]
          [:span.fs3 {:style {:margin-left 4}} "Unverified"]]
         [:span
          (payments/payment-source-icon (or type :bank))
          (when autopay [ant/icon {:type :sync :style {:margin-left "8px"}}])
          (when default [ant/icon {:type :check-circle :style {:margin-left "10px"}}])])]]]))


(defn source-list
  "A vertical menu listing the linked payment sources."
  []
  (let [sources (subscribe [:payment/sources])]
    [:div.source-list
     (doall
      (map-indexed
       #(with-meta [source-list-item %2] {:key %1})
       @sources))
     [add-new-source-button]]))


(defn- show-cannot-remove-bank [source]
  (ant/modal-warning
   {:title   (r/as-element
              [:span.title.is-5 (str "Cannot remove " (:name source))])
    :width   640
    :ok-text "OK, got it."
    :content (r/as-element
              [:div
               [:p "You must have a bank account linked in order to pay rent."]
               [:p "If you wish to remove this bank, please link another one first."]])}))


(defn- source-actions-menu []
  (let [source     (subscribe [:payment.sources/current])
        can-remove (subscribe [:payment.sources.current/can-remove?])]
    [ant/menu
     [ant/menu-item {:key "removeit"}
      [:a.text-red
       {:href     "#"
        :on-click #(if @can-remove
                     (dispatch [:modal/show :payment.source/remove])
                     (show-cannot-remove-bank @source))}
       "Remove this account"]]]))


(defn source-detail
  "Display information about the currently-selected payment source."
  []
  (let [{:keys [type name] :as source} @(subscribe [:payment.sources/current])]
    [ant/card {:class "mb2"}
     [:div.flexrow.align-start
      [payments/payment-source-icon type]
      [:div.ml1
       [:h3.lh13 name]
       [:p (account-digits source)]]]

     [:div.card-controls
      [ant/dropdown {:trigger ["click"]
                     :overlay (r/as-element [source-actions-menu])}
       [:a.ant-dropdown-link
        [:span "More"]
        [ant/icon {:type "down"}]]]]

     [:div.mt2
      (when (is-unverified source)
        [ant/button {:type     "primary"
                     :on-click #(dispatch [:modal/show :payment.source/verify-account])}
         [ant/icon {:type "check-circle"}]
         [:span "Verify Account"]])]]))


(defn source-payment-history
  "Display the transaction history for a given payment source."
  []
  (let [{:keys [payments name]} @(subscribe [:payment.sources/current])]
    [ant/card {:class "is-flush stripe-style"}
     [payments/payments-table payments false]]))


(defn bank-radio-option
  [{:keys [id name last4] :as bank}]
  [ant/radio {:value id} (str name " " (account-digits bank))])


(defn modal-enable-autopay-footer [selected-autopay-source]
  (let [is-submitting (subscribe [:ui/loading? :payment.sources.autopay/enable!])]
    [:div
     [ant/button
      {:on-click #(dispatch [:modal/hide :payment.source/autopay-enable])
       :size     :large}
      "I'd rather pay manually."]
     [ant/button {:type     "primary"
                  :size     :large
                  :loading  @is-submitting
                  :on-click #(dispatch [:payment.sources.autopay/enable! selected-autopay-source])}
      "Great! Let's do it"]]))


(defn modal-confirm-enable-autopay []
  (let [is-visible (subscribe [:modal/visible? :payment.source/autopay-enable])
        banks      (subscribe [:payment/sources :bank])
        selected   (r/atom (-> @banks first :id))]
    (fn []
      [ant/modal {:title     "Autopay your rent?"
                  :visible   @is-visible
                  :on-cancel #(dispatch [:modal/hide :payment.source/autopay-enable])
                  :footer    (r/as-element [modal-enable-autopay-footer @selected])}
       [:div
        [:p "Autopay automatically transfers your rent payment each month. We
          recommend enabling this feature, so you never need to worry about
          making rent on time."]
        [:p.bold "Choose a bank account to use for Autopay:"]
        [ant/radio-group {:default-value @selected
                          :class         "vertical-radio"
                          :disabled      (< (count @banks) 2)
                          :on-change     #(reset! selected (.. % -target -value))}
         (map-indexed
          (fn [idx {key :key :as item}]
            (-> (bank-radio-option item)
                (with-meta {:key idx})))
          @banks)]]])))



(defn modal-disable-autopay-footer
  [selected-autopay-source]
  (let [is-submitting (subscribe [:ui/loading? :payment.sources.autopay/disable!])]
    [:div
     [ant/button {:on-click #(dispatch [:modal/hide :payment.source/autopay-disable])} "Cancel"]
     [ant/button {:type     "primary"
                  :loading  @is-submitting
                  :on-click #(dispatch [:payment.sources.autopay/disable! selected-autopay-source])}
      "Disable Autopay"]]))


(defn modal-confirm-disable-autopay []
  (let [is-visible     (subscribe [:modal/visible? :payment.source/autopay-disable])
        autopay-source (subscribe [:payment.sources/autopay-source])]
    [ant/modal {:title     "Turn off autopay?"
                :visible   @is-visible
                :footer    (r/as-element [modal-disable-autopay-footer (:id @autopay-source)])
                :on-ok     #(dispatch [:payment.sources.autopay/disable! (:id @autopay-source)])
                :on-cancel #(dispatch [:modal/hide :payment.source/autopay-disable])}
     [:div
      [:p "Autopay automatically transfers your rent each month, one day before your Due Date. We recommend enabling this feature, so you never need to worry about making rent on time."]]]))


(defn modal-verify-account-footer []
  (let [current-id    (subscribe [:payment.sources/current-id])
        is-submitting (subscribe [:ui/loading? :payment.sources.bank/verify!])
        amounts       (subscribe [:payment.sources.bank.verify/microdeposits])
        amount-1      (:amount-1 @amounts)
        amount-2      (:amount-2 @amounts)]
    [:div
     [ant/button {:on-click #(dispatch [:modal/hide :payment.source/verify-account])} "Cancel"]
     [ant/button {:type     "primary"
                  :loading  @is-submitting
                  :on-click #(dispatch [:payment.sources.bank/verify! @current-id amount-1 amount-2])}
      "Verify Amounts"]]))


(defn modal-verify-account []
  (let [bank       (subscribe [:payment.sources/current])
        amounts    (subscribe [:payment.sources.bank.verify/microdeposits])
        is-visible (subscribe [:modal/visible? :payment.source/verify-account])]
    [ant/modal {:title   (str "Verify " (:name @bank))
                :visible @is-visible
                :footer  (r/as-element [modal-verify-account-footer])}
     [:div
      [:p "If the two microdeposits have posted to your account, enter them below to verify ownership."]
      [:p.fs2 "Note: Amounts should be entered in " [:i "cents"] " (e.g. '32' not '0.32')"]
      [:form.form-verify-microdeposits.mt2 {:on-submit #(.preventDefault %)}
       [ant/input-number {:default-value (:amount-1 @amounts)
                          :min           1
                          :max           99
                          :placeholder   "00"
                          :size          "large"
                          :on-change     #(dispatch [:payment.sources.bank.verify/edit-amount :amount-1 %])}]
       [ant/input-number {:default-value (:amount-2 @amounts)
                          :min           1
                          :max           99
                          :placeholder   "00"
                          :size          "large"
                          :on-change     #(dispatch [:payment.sources.bank.verify/edit-amount :amount-2 %])}]]]]))


(defn modal-confirm-remove-account []
  (let [is-visible     (subscribe [:modal/visible? :payment.source/remove])
        removing       (subscribe [:ui/loading? :payment.source/delete!])
        current-source (subscribe [:payment.sources/current])]
    (fn []
      [ant/modal {:title     "Remove this account?"
                  :width     640
                  :visible   @is-visible
                  :ok-text   "Yes, remove"
                  :on-cancel #(dispatch [:modal/hide :payment.source/remove])
                  :footer    [(r/as-element
                               ^{:key "cancel"}
                               [ant/button
                                {:type     :ghost
                                 :on-click #(dispatch [:modal/hide :payment.source/remove])}
                                "Cancel"])
                              (r/as-element
                               ^{:key "delete"}
                               [ant/button
                                {:type     :primary
                                 :loading  @removing
                                 :on-click #(dispatch [:payment.source/delete! (:id @current-source)])}
                                "Yes, remove it."])]}
       [:p "If you remove this account, it will no longer be available for settling payments."]])))


(def ^:private tab-icon-classes
  {:bank    "fa-bank"
   :card    "fa-credit-card"
   :bitcoin "fa-bitcoin"})


(def ^:private tab-labels
  {:bank    "Bank Account"
   :card    "Credit Card"
   :bitcoin "Bitcoin"})


(defn form-tab [tab-type selected-type]
  [:li {:class (when (= selected-type tab-type) "is-active")}
   [:a {:on-click #(dispatch [:payment.sources.add/select-type tab-type])}
    [:span.icon.is-small [:i.fa {:class (get tab-icon-classes tab-type)}]]
    [:span (get tab-labels tab-type)]]])


(defn tabs []
  (let [this (r/current-component)]
    [:div.tabs (r/props this)
     (into [:ul] (r/children this))]))


(defn modal-add-source []
  (let [type         (subscribe [:payment.sources.add/type])
        is-visible   (subscribe [:modal/visible? :payment.source/add])
        source-types (subscribe [:payment.sources.add/available-types])]
    (r/create-class
     {:component-will-mount
      (fn [_]
        (dispatch [:stripe/load-scripts "v2"])
        (dispatch [:stripe/load-scripts "v3"]))
      :reagent-render
      (fn []
        [ant/modal {:title     (l10n/translate :btn-add-new-account)
                    :width     640
                    :visible   @is-visible
                    :on-ok     #(dispatch [:modal/hide :payment.source/add])
                    :on-cancel #(dispatch [:modal/hide :payment.source/add])
                    :footer    nil}
         [:div
          [tabs {:class "is-centered"}
           (doall
            (map-indexed
             #(with-meta [form-tab %2 @type] {:key %1})
             @source-types))]

          (case @type
            :bank    (r/as-element (ant/create-form (forms/bank-account)))
            :card    (r/as-element (ant/create-form
                                    (form/credit-card {:is-submitting @(subscribe [:ui/loading? :payment.sources.add.card/save-stripe-token!])
                                                       :on-add-card   #(dispatch [:payment.sources.add.card/save-stripe-token! %])
                                                       :on-click      #(dispatch [:modal/hide :payment.source/add])})))
            :bitcoin [forms/bitcoin-account]
            [:div])]])})))


(defn no-sources []
  [:div.box
   [:h3 "You don't have any accounts linked yet."]

   [:div.steps-vertical
    [media/step "Link a payment source so you can settle your charges." "bank"]
    [media/step "Turn on Autopay and never worry about a late payment again." "history"]
    [media/step
     [ant/button {:type     :primary
                  :size     :large
                  :on-click #(dispatch [:modal/show :payment.source/add])}
      [:span.icon.is-small [:i.fa.fa-plus-square-o]]
      [:span (l10n/translate :btn-add-new-account)]]]]])


(defn- autopay-tooltip []
  (let [has-verified (subscribe [:payment.sources/has-verified-bank?])
        rent-unpaid  (subscribe [:member.rent/unpaid?])
        this         (r/current-component)]
    [ant/tooltip
     {:title (cond
               (not @has-verified) "To enable Autopay, you must first add and verify a bank account."
               @rent-unpaid        "You must pay your outstanding rent payment before enabling Autopay."
               :otherwise          nil)}
     (into [:span] (r/children this))]))


(defn- last-4
  [v]
  (-> (take-last 4 v)
      (str/join)))


(defn- payout-confirmation-content []
  (let [form @(subscribe [:payout-account/form])]
    [:div
    "Are you sure the information below is correct?"
     [:p "Routing Number: *****" (last-4 (:routing-number form))]
     [:p "Account Number: ********" (last-4 (:account-number form))]
     [:p "Date of Birth: " (:dob form)]
     [:p "Social Security Number: " (last-4 (:ssn form))]
     [:p "Address Line1: " (:line1 form)]
     [:p "Address Line2: " (:line2 form)]
     [:p "City: " (:city form)]
     [:p "State: " (:state form)]
     [:p "Postal Code: " (:postal-code form)]
     [:p "Country: " (:country form)]]))


(defn- payout-confirmation-modal []
  (let [account @(subscribe [:payment/account])]
    (ant/modal-confirm
     {:title   "Confirm Deposit Information"
      :content (r/as-element [payout-confirmation-content])
      :on-type :primary
      :ok-text "Confirm!"
      :on-ok   #(dispatch [:payout-account/create! (:id account)])})))


(defn- payout-modal-footer []
  (let [is-loading (subscribe [:ui/loading? :payout-account/create!])
        can-submit (subscribe [:payout-account/can-submit?])
        on-cancel  #(dispatch [:modal/hide :payout-account/modal])]
    [:div
     [ant/button
      {:on-click #(on-cancel)}
      "Cancel"]
     [ant/tooltip
      {:title (when (not @can-submit)
                "Please fill or fix all required fields.")}
      [ant/button
       {:loading  @is-loading
        :disabled (not @can-submit)
        :on-click #(payout-confirmation-modal)
        :type     :primary}
       "Submit!"]]]))


(defn- payout-dob []
  (let [is-valid (subscribe [:payout-account.form/is-valid? :dob])
        value (subscribe [:payout-account/input-value :dob])
        default-date (subscribe [:payout-account.date/default-date])
        on-change #(dispatch [:payout-account.form/update! :dob %])]
    [ant/form-item
     (merge
      {:label "Date of Birth:"
       :read-only true}
      (if @is-valid
        {}
        {:help            "Please provide your date of birth."
         :has-feedback    true
         :validate-status "error"}))
     [ant/date-picker
      {:value @value
       :default-value @default-date
       :disabled-date #(> % @default-date)
       :allow-clear false
       :format "MMM Do YY"
       :on-change #(on-change %)}]]))


(defn- payout-field
  [{:keys [k label help required]}]
  (let [is-valid  (subscribe [:payout-account.form/is-valid? k])
        value     (subscribe [:payout-account/input-value k])
        on-change #(dispatch [:payout-account.form/update! k %])]
    [ant/form-item
     (merge
      {:label     label
       :read-only true}
      (if @is-valid
        {}
        {:help            help
         :has-feedback    true
         :validate-status "error"}))
     [ant/input
      {:value @value
       :placeholder label
       :required required
       :on-change #(on-change (.. % -target -value))}]]))


(defn- payout-bank-info []
  [:div
   [:h3 "Bank Information"]
   [:div.columns
    [:div.column
     [payout-field {:k        :routing-number
                    :label    "Routing Number:"
                    :help     "Please enter your 9 digit routing number"
                    :required true}]]
    [:div.column
     [payout-field {:k        :account-number
                    :label    "Account Number:"
                    :help     "Please enter your 12 digit account number"
                    :required true}]]]
   [:br]])


(defn- payout-personal-info []
  [:div
   [:h3 "Personal Information"]
   [:div.columns
    [:div.column
     [payout-dob]]
    [:div.column
     [payout-field {:k        :ssn
                    :label    "Social Security Number:"
                    :help     "Please enter your 9 digit social security number."
                    :required true}]]]
   [:br]])


(defn- payout-address-info []
  [:div
   [:h3 "Current Address"]
   [:div.columns
    [:div.column
     [payout-field {:k        :line1
                    :label    "Address Line1:"
                    :help     "Please enter an address."
                    :required true}]]
    [:div.column
     [payout-field {:k     :line2
                    :label "Address Line2:"}]]]
   [:div.columns
    [:div.column
     [payout-field {:k        :city
                    :label    "City:"
                    :help     "Please enter a City."
                    :required true}]]
    [:div.column
     [payout-field {:k        :state
                    :label    "State:"
                    :help     "Please enter your 2 letter State code."
                    :required true}]]]
   [:div.columns
    [:div.column
     [payout-field {:k        :postal-code
                    :label    "Postal Code:"
                    :help     "Please enter your 5 digit postal code."
                    :required true}]]
    [:div.column
     [payout-field {:k        :country
                    :label    "Country:"
                    :help     "Please enter your 2 letter country code."
                    :required true}]]]])


(defn- payout-modal-description []
  [:div
   [:p "Help us, help you by filling out the information below so we can
     directly deposit your security deposit. Although we need your personal
     information, it is only for validation purposes. We take great care to not
     store any personal information."]
   [:p.bold.align-center
    {:style {:color "#1890ff"}}
    "NOTE: This only works for US bank accounts."]
   [:br]])


(defn- payout-modal []
  (let [is-visible (subscribe [:modal/visible? :payout-account/modal])
        form       (subscribe [:payout-account/form])]
    [ant/modal
     {:title    "Deposit Refund Information"
      :width    "40%"
      :visible  @is-visible
      :on-close #(dispatch [:modal/hide :payout-account/modal])
      :footer   (r/as-element [payout-modal-footer])}
     [payout-modal-description]
     [payout-bank-info]
     [payout-personal-info]
     [payout-address-info]]))


(defn source-settings []
  (let [autopay-on      (subscribe [:payment.sources/autopay-on?])
        autopay-allowed (subscribe [:payment.sources/can-enable-autopay?])
        card-sources    (subscribe [:payment/sources :card])
        service-source  (subscribe [:payment.sources/service-source])
        setting-svc-src (subscribe [:ui/loading? :payment.source/set-default!])
        refundable      (subscribe [:account/refundable])]
    [:div.page-controls.columns
     [:div.column {:style {:padding 0}}

      [:span.bold.mr1
       {:class (when-not @autopay-allowed "subdued")}
       (if @autopay-on "Autopay On" "Autopay Off")]

      [autopay-tooltip
       [ant/switch {:checked   @autopay-on
                    :disabled  (not @autopay-allowed)
                    :on-change #(dispatch [:payment.sources.autopay/confirm-modal @autopay-on])}]]

      [:span.ml1
       [tooltip/info
        (r/as-element
         [:span "With Autopay enabled, rent payments will automatically be withdrawn from your bank account on the "
          [:b "1st"] " of each month."])]]]

     (when-not @refundable
       [:div.column {:style {:padding 0}}
        [ant/button
         {:type :primary
          :on-click #(dispatch [:modal/show :payout-account/modal])}
         "Add Deposit Info!"]])

     [:div.column.is-6 {:style {:padding 0}}
      [:span.bold.mr2 "Pay for Services with:"]
      (if @setting-svc-src
        [ant/spin {:style {:width 150}}]
        [ant/select
         {:style     {:width 150}
          :value     (:id @service-source)
          :disabled  (empty? @card-sources)
          :on-change #(dispatch [:payment.source/set-default! %])}
         (doall
          (for [{id :id :as source} @card-sources]
            ^{:key id} [ant/select-option {:value id :disabled (= id (:id @service-source))}
                        (payments/source-name source)]))])
      [:span.ml1
       [tooltip/info
        (if (empty? @card-sources)
          "To pay for premium services, please link a credit or debit card."
          "This is the payment method that will be used for premium service orders.")]]]]))


(defn- source-view [sources]
  (if (empty? sources)
    ;; Empty State
    [no-sources]
    ;; Show Sources
    [:div
     [source-settings]
     [:div.columns
      [:div.column.fw200
       [:h3 "Accounts"]
       [source-list]]
      [:div.column
       [:h3 "Details"]
       [source-detail]
       [:h3 "Transaction History"]
       [source-payment-history]]]]))


(defn sources []
  (let [is-loading (subscribe [:ui/loading? :payment.sources/fetch])
        sources    (subscribe [:payment/sources])]
    [:div
     [modal-add-source]
     [modal-confirm-remove-account]
     [modal-verify-account]
     [modal-confirm-enable-autopay]
     [modal-confirm-disable-autopay]
     [payout-modal]
     (typography/view-header
      (l10n/translate :payment-sources)
      "Edit your payment accounts, enable Autopay, and set default payment sources.")
     (if (and (empty? @sources) @is-loading)
       [:div.loading-box.tall [ant/spin]]
       (source-view @sources))]))
