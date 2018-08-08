(ns apply.core
  (:require [accountant.core :as accountant]
            [antizer.reagent :as ant]
            [apply.applications.views]
            [apply.content :as content]
            [apply.db :as db]
            [apply.events]
            [apply.routes :as routes]
            [apply.sections.logistics]
            [apply.sections.community]
            [apply.sections.personal]
            [apply.sections.payment]
            [apply.subs]
            [day8.re-frame.http-fx]
            [goog.dom :as gdom]
            [cljsjs.moment]
            [cljsjs.react-day-picker]
            [clojure.string :as s]
            [iface.components.ptm.layout :as layout]
            [iface.modules.graphql :as graphql]
            [iface.modules.modal]
            [iface.utils.routes :as iroutes]
            [reagent.core :as r]
            [re-frame.core :as rf :refer [dispatch subscribe]]
            [iface.components.ptm.icons :as icons]
            [iface.components.ptm.ui :as ui]
            [iface.components.ptm.ui.button :as button]
            [iface.components.ptm.ui.form :as form]
            [iface.components.ptm.ui.button :as button]
            [toolbelt.core :as tb]
            [iface.utils.log :as log]
            [iface.loading :as loading]))


(defn- welcome-1 [{name :name} toggle]
  [:div
   [:div.w-60-l.w-100.center
    [:h1.tc "Welcome to Starcity,"]
    [:h2.tc.handwriting.f2 (first (s/split name #" "))]]
   [:div.page-content.w-90-l.w-100.center.tc
    [:p.tc "We'd love to have you join our community."]
    [:br]
    ;; [:p.tc "I'm here to help with your application. If you have a question,"]
    ;; [:p.tc "just click on the " (icons/icon {:type "help" :class "icon-small"}) " icon to send me a message."]
    [:br]
    [:div
     [:img.br-100.h3.w3.dib
      {:src "/assets/images/bio-matthew.jpg"}]]
    [:h3.handwriting "-Matt"]
    [:br]

    [button/primary
     {:on-click #(swap! toggle not)
      :class    "mt4"}
     "Let's go!"]]])


(defn- welcome-2 []
  [:div
   [:div.w-60-l.w-100.center
    [:h1.tc "Here's what you'll need"]]
   [:div.page-content.w-90-l.w-100.center.tc
    [:div.cf.mt3
     [:div.w-third-l.w-100.fl.ph4
      [:div.pb4
       [:img {:src "/assets/images/ptm/icons/ic-truck-black.svg"}]]
      [:h3 "Logistics"]
      [:p "We’ll need to know stuff like which communities you'd like to join, you’re preferred move-in date, and how long you'll be staying with us."]]
     [:div.w-third-l.w-100.fl.ph4
      [:div.pb4
       [:img {:src "/assets/images/ptm/icons/ic-paper-black.svg"}]]
      [:h3 "Personal Information"]
      [:p "Provide us with some personal information so that we can perform a background check and verify your income."]]
     [:div.w-third-l.w-100.fl.ph4
      [:div.pb4
       [:img {:src "/assets/images/ptm/icons/ic-credit-card-black.svg"}]]
      [:h3 "Application Fee"]
      [:p "It’ll cost $25 for each application – that helps cover the cost of the background check."]]]
    [:button.button.mt5
     {:on-click #(dispatch [:ptm/start])}
     "Got it"]]])


(defn- nav-item
  [{:keys [label icon section] :as nav-item}]
  (let [is-enabled  (subscribe [:nav.item/enabled? nav-item])
        is-complete (subscribe [:nav.item/complete? nav-item])
        route       (subscribe [:route/current])
        progress    (cond
                      (and (not= section :payment) @is-complete)                :complete
                      (and (not= section :payment)
                           (= section (-> @route :params :section-id keyword))) :active
                      (and (= section :payment)
                           (= section (-> @route :params :section-id keyword))) true
                      :otherwise                                                nil)]

    [layout/nav-item {:progress progress
                      :label    label
                      :icon     (if @is-complete
                                  "check"
                                  icon)
                      :disabled (not @is-enabled)
                      :action   (fn [] (dispatch [:nav.item/select nav-item]))}]))


(defn nav []
  (let [nav-items (subscribe [:nav/items])]
    [layout/nav
     {:is-progress? true
      :footer       [layout/nav-footer
                     [layout/nav-item {:label "Help"
                                       :icon  "help"}]
                     [layout/nav-item {:label "Log Out"
                                       :link  (routes/path-for :logout)
                                       :icon  "logout"}]]}
     (doall
      (map-indexed #(with-meta [nav-item %2] {:key %1}) @nav-items))]))


(defn footer []
  (let [has-next     (subscribe [:ui.step.current/has-next?])
        has-prev     (subscribe [:ui.step.current/has-back?])
        next         (subscribe [:step.current/next])
        prev         (subscribe [:step.current/previous])
        next-loading (subscribe [:ui/loading? :step.current/save])
        next-label   (subscribe [:step.current.next/label])
        next-enabled (subscribe [:step/complete?])]
    [layout/footer
     (tb/assoc-when
      {}
      :primary (when @has-next
                 {:label    @next-label
                  :disabled (not @next-enabled)
                  :loading  @next-loading
                  :action   #(dispatch [:step.current/next])})
      :destructive (when @has-prev
                     {:label "back"
                      :link  (db/step->route @prev)}))]))


(defn welcome-layout []
  (let [screen-two (r/atom false)]
    (fn []
      (let [applicant (subscribe [:user])]
        [layout/layout
         {:pre (list [:div.bg-top {:key 1}]
                     [:div.welcome-logo.pt6
                      [:img {:src "/assets/images/ptm/blue-logomark.svg"}]])}
         (if-not @screen-two
           [welcome-1 @applicant screen-two]
           [welcome-2])]))))


(defn layout []
  (let [route (subscribe [:route/current])
        step  (subscribe [:step/current])]
    (case (:page @route)
      :welcome      [welcome-layout]
      :applications [content/view @route]
      :logout       [content/view @route]
      [layout/layout
       {:nav [nav] :footer [footer]}
       [content/view @route]])))


;; ==============================================================================
;; app entry ====================================================================
;; ==============================================================================


(defn render []
  (r/render
   [ant/locale-provider {:locale (ant/locales "en_US")}
    [layout]]
   (gdom/getElement "apply")))


(defn reload! []
  (render)
  (accountant/dispatch-current!))


(defn ^:export run []
  (let [account (js->clj (aget js/window "account") :keywordize-keys true)]
    (graphql/configure
     "/api/graphql"
     {:on-unauthenticated (fn [_]
                            {:route "/logout"})
      :on-error-fx        (fn [[k _]]
                            {:dispatch [:ui/loading k false]})})
    (rf/dispatch-sync [:app/init account])
    (iroutes/hook-browser-navigation! routes/app-routes)
    (render)))
