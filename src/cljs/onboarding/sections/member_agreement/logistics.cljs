(ns onboarding.sections.member-agreement.logistics
  (:require [antizer.reagent :as ant]
            [iface.components.ptm.layout :as layout]
            [iface.utils.formatters :as format]
            [iface.utils.log :as log]
            [onboarding.content :as content]
            [onboarding.db :as db]
            [onboarding.events :as events]
            [re-frame.core :refer [dispatch
                                   subscribe
                                   reg-event-fx
                                   reg-sub]]
            [reagent.core :as r]
            [onboarding.routes :as routes]
            [iface.components.ptm.ui.modal :as modal]
            [iface.components.ptm.ui.form :as form]))


(def step :member-agreement/logistics)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :member-agreement/sign)


(defmethod db/previous-step step
  [db]
  nil)


(defmethod db/has-back-button? step
  [_]
  false)


(defmethod db/step-complete? step
  [db step]
  true)


;; events =======================================================================


(defmethod events/save-step-fx step
  [db params]
  {:db       (assoc db step params)
   :dispatch [:step/advance]})


(reg-event-fx
 :change-date
 (fn [{db :db} _]
   {:route (db/step->route :member-agreement.logistics/change-date)}))


;; views ========================================================================


;; summary item =========================


(defn- summary-item
  "Summary information item"
  [{:keys [label value edit on-click]}]
  [:div.w-100.fl.pv1
   [:h4.w-40.mv1.fl label]
   [:p.w-60.fl.tr.mv0 value
    (when edit
      [:img.icon-edit {:src      "/assets/images/ptm/icons/ic-edit.svg"
                       :on-click (when-let [c on-click]
                                   #(c))}])]])


;; line item ============================


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


(defn- line-item [{:keys [type label tooltip price]}]
  [:div.cf
   [line-label label tooltip]
   (if (= type :line)
     [:p.w-40.fl.tr.mv0 (format/currency price)]
     [:h3.w-40.fl.tr.mt1.mb3 (format/currency price)])])


;; content ==============================


(defmethod content/view step
  [_]
  (let [is-open (r/atom false)
        value   (r/atom {:date nil})]
    (fn []
      [:div
       [modal/modal
        {:visible  @is-open
         :on-close #(swap! is-open not)}
        [:div.lightbox-content-small
         [:h1 "Select new move-in date."]
         [:p "This will only affect the date you will be moving in. Your member
     agreement will still have the date you selected during your
     application."]
         [:div.pv4
          [form/inline-date {:value        (when-let [d (:date @value)] d)
                             :on-day-click #(when-not (.. %2 -disabled)
                                              (log/log %)
                                              (swap! value assoc :date %))
                             :show-from    (js/Date.)
                             :disabled     {:before (js/Date.)}}]]]]
       [layout/header
        {:title   "Finalize your move-in logistics."
         :subtext (list "Please review your application and make sure everything
     looks accurate and up to date. If you need to make any additional changes,
     contact your " [:a {:href ""} "Community Manager"])}]
       [:div.w-100
        [:div.page-content
         [:div.card.cf
          [:div.w-60-l.w-100.fl.pv0
           [:div.card-top
            {:style {:overflow "auto"}}
            [:h2.ma0.mb3 "Union Square West"]
            [:h3.ma0.pv1 "Logistics"]
            [summary-item {:label    "Move-in date"
                           :value    "4-25-2018"
                           :edit     true
                           :on-click #(swap! is-open not)}]
            [summary-item {:label "Adult Occupants"
                           :value "Single"}]
            [summary-item {:label "Term Length"
                           :value "12 months"}]
            [summary-item {:label "Pet"
                           :value "Dog"}]]
           [:div.card-section
            [:h3.ma0.pv1 "Membership Fee"]
            [line-item {:type    :line
                        :label   "Suite Fee"
                        :tooltip "Something about the suite fee"
                        :price   1600}]
            [line-item {:type    :line
                        :label   "Community Fee"
                        :tooltip "Something about the suite fee"
                        :price   300}]
            [line-item {:type    :line
                        :label   "Pet Fee"
                        :tooltip "Something about the suite fee"
                        :price   75}]
            [:hr.ph4]
            [line-item {:type  :total
                        :label "First total rent due 4-1-2018"
                        :price 1975}]]
           [:div.card-section
            [:h3.ma0.pv1 "Security Deposit"]
            [line-item {:type  :total
                        :label "Security Deposit"
                        :price 1975}]]]
          [:div.w-40-l.w-100.fl.pv0.card-img-onboard
           {:style {:background-image (str "url('" "http://placekitten.com/1200/1200" "')")}}]]]]])))
