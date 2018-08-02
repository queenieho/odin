(ns apply.sections.payment.review
  (:require [apply.content :as content]
            [apply.db :as db]
            [apply.events :as events]
            [antizer.reagent :as ant]
            [clojure.string :as s]
            [re-frame.core :refer [dispatch subscribe reg-sub reg-event-fx reg-fx]]
            [iface.components.form :as iform]
            [iface.components.ptm.ui.form :as form]
            [iface.components.ptm.ui.card :as card]
            [iface.utils.formatters :as format]
            [iface.utils.log :as log]
            [iface.utils.time :as time]
            [iface.components.ptm.ui.button :as button]
            [reagent.core :as r]))


(def step :payment/review)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  :payment/complete)


(defmethod db/next-step step
  [db]
  :payment/complete)


(defmethod db/previous-step step
  [db]
  :personal/about)


(defmethod db/has-back-button? step
  [_]
  true)


(defmethod db/step-complete? step
  [db step]
  (not (step db)))


;; events =======================================================================


(reg-event-fx
 ::update-terms-and-conditions
 (fn [{db :db} [_ v]]
   {:db (assoc db step v)}))


;; next =================================


(defmethod events/save-step-fx step
  [db params]
  {:stripe-checkout {:on-success [::submit-payment]}})


(reg-event-fx
 ::submit-payment
 (fn [{db :db} [_ token]]
   (log/log token (string? (:id token)))
   {:graphql {:mutation [[:application_submit_payment {:application (:application-id db)
                                                       :token       (:id token)}
                          [:id :status]]]
              :on-success [:step/advance]
              :on-failure [:graphql/failure ::submit-payment]}}))


(reg-fx
 :stripe-checkout
 (fn [{:keys [_ on-success]}]
   (let [checkout (aget js/window "StripeCheckout")
         conf     {:name            "Starcity"
                   :description     "Member Application"
                   :amount          (.-amount js/stripe)
                   :key             (.-key js/stripe)
                   :zipCode         true
                   :allowRememberMe true
                   :locale          "auto"
                   :token           (fn [token]
                                      (dispatch (conj on-success (js->clj token :keywordize-keys true))))}]
     ((aget checkout "open") (clj->js conf)))))


;; subs =========================================================================


(defn- move-in [db]
  (let [option (:logistics/move-in-date db)]
    (if (= option :date)
      (format/date-short-num (:logistics.move-in-date/choose-date db))
      (s/capitalize (name option)))))


(defn- pet [db]
  (let [pet (:logistics/pets db)]
    (cond
      (false? pet)             "None"
      (:logistics.pets/dog db) "Dog"
      :else                    "Other")))


(reg-sub
 :review/logistics
 (fn [db _]
   [{:label    "Move-in Date"
     :value    (move-in db)
     :edit     true
     :on-click #(log/log "edit move-in")}
    {:label "Occupants"
     :value (->> (:logistics/occupancy db)
                 name
                 s/capitalize)
     :edit  true}
    {:label "Term length"
     :value (str (:community/term db) " months")
     :edit  true}
    {:label "Pet"
     :value (pet db)
     :edit  true}]))


(defn- suite-fee [rates term]
  (let [rate (->> (filter #(= (:term %) term) rates)
                  first
                  :rate)]
    (- rate 300)))


;; NOTE currently hard coding this in
;; we will want to save these prices somewhere in the future
(defn- fees-init [db]
  (->> [{:label   "Membership Fee"
         :tooltip "Your membership fee pays for all the amenities included in your Starcity membership, such as TV services, wifi, and house cleaning."
         :price   (if (= :single (:logistics/occupancy db))
                    300
                    600)}
        {:label "Pet Fee"
         :price (when (not (false? (:logistics/pets db)))
                  75)}]
       (remove #(nil? (:price %)))))


(defn- total [suite-price other-fees]
  (let [price (reduce
               (fn [total p]
                 (+ total (:price p)))
               suite-price
               other-fees)
        max   (reduce
               (fn [total p]
                 (+ total (if (:max p)
                            (:max p)
                            (:price p))))
               suite-price
               other-fees)]
    {:label "Total"
     :price  price
     :max    (when (> max price)
               max)}))


(reg-sub
 :review/communities
 (fn [db _]
   (let [selections      (:community/select db)
         communities     (filter
                          #(some (fn [s] (= (:id %) s)) selections)
                          (:communities-options db))
         line-items-init (fees-init db)]
     (map
      (fn [c]
        (let [sfee (suite-fee (:rates c) (:community/term db))]
          {:id         (:id c)
           :community  (:name c)
           :line-items (conj line-items-init
                             {:label   "Suite Fee"
                              :tooltip "Suites in this building vary in price due to size and features."
                              :price   sfee})
           :total      (total sfee line-items-init)}))
      communities))))


(reg-sub
 :review/personal
 (fn [db _]
   (let [{:keys [first-name last-name middle-name dob current_location]} (:personal.background-check/info db)
         {:keys [locality region country postal_code]}                   current_location]
     [{:label "First Name"
       :value first-name
       :edit  true}
      {:label "Last Name"
       :value last-name
       :edit  true}
      {:label "Middle Name"
       :value middle-name
       :edit  true}
      {:label "Date of Birth"
       :value (-> (time/moment->iso dob)
                  (format/date-short-num))
       :edit  true}
      {:label "Country"
       :value country
       :edit  true}
      {:label "Region"
       :value region
       :edit  true}
      {:label "locality"
       :value locality
       :edit  true}
      {:label "Postal Code"
       :value postal_code
       :edit  true}])))


;; views ========================================================================


(defmethod content/view step
  [_]
  (let [checked     (subscribe [:db/step step])
        logistics   (subscribe [:review/logistics])
        communities (subscribe [:review/communities])
        personal    (subscribe [:review/personal])]
    [:div
     [:div.w-60-l.w-100
      [:h1 "Let's take a moment to check over all the details."]]
     [:div.w-100-l.w-100
      [:div.page-content
       [card/logistics-summary {:title "Logistics"
                                :items @logistics}]
       [:div {:style {:overflow "auto"}}
        (map
         #(with-meta
            [card/community-selection %]
            {:key (:id %)})
         @communities)]
       [card/logistics-summary {:title "Personal Information"
                                :items @personal}]
       [:p.mb2 "Please take a moment to review our "
        [:a {:href "https://starcity.com/terms"} "Terms of Service"] " and "
        [:a {:href "https://starcity.com/privacy"} "Privacy Policy"] "."]
       [form/checkbox
        {:checked   (or @checked false)
         :on-change #(dispatch [::update-terms-and-conditions (.. % -target -checked)])}
        "I have read and agree to the Terms of Service and Privacy Policy."]]]]))
