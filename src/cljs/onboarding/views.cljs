(ns onboarding.views
  (:require [antizer.reagent :as ant]
            [clojure.string :as string]
            [clojure.set :as set]
            [iface.loading :as l]
            [iface.components.ptm.layout :as layout]
            [iface.utils.log :as log]
            [onboarding.content :as content]
            [onboarding.db :as db]
            [onboarding.routes :as routes]
            [re-frame.core :refer [dispatch subscribe]]
            [toolbelt.core :as tb]
            [iface.utils.formatters :as format]))


(defn- welcome-item
  [{:keys [icon title body]}]
  [:div.w-third-l.w-100.fl.ph4.mv4
   [:div.pb4
    [:img {:src icon}]]
   [:h3 title]
   (into [:p] body)])


(defn welcome-layout []
  (let [user    (subscribe [:user])
        [name]  (string/split (:name @user) #" ")
        deposit (format/currency (:full-deposit @user))]
    [layout/layout
     {:pre (list [:div.bg-top {:key 1}]
                 [:div.welcome-logo.pt6 {:key 2}
                  [:img {:src "/assets/images/ptm/blue-logomark.svg"}]])}
     [:div
      [:div.w-60-l.w-100.center
       [:h1.tc "Welcome to Starcity, " name "!"]
       [:p.tc "We're so glad you've been selected to join our community. There
       are a few important steps to complete to finalize your move-in
       details."]]
      [:div.page-content.w-90-l.w-100.center.tc
       [:div.cf
        [welcome-item
         {:icon  "/assets/images/ptm/icons/ic-truck-black.svg"
          :title "Confirm Logistics"
          :body  "We want to confirm your move-in date and other details from
          your application."}]
        [welcome-item
         {:icon  "/assets/images/ptm/icons/ic-paper-black.svg"
          :title "Sign Membership Agreement"
          :body  "We're making it official! Review and sign your membership
          agreement online."}]
        [welcome-item
         {:icon  "/assets/images/ptm/icons/ic-credit-card-black.svg"
          :title "Pay Security Deposit"
          :body (list "Last step is to pay the " [:b deposit] " security
          deposit (partial or full) and you'll be ready to move in!")}]]
       [:button.button
        {:on-click #(dispatch [:onboarding/start])}
        "Let's go!"]]]]))


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
                     [layout/nav-item {:label  "Help"
                                       :icon   "help"
                                       :action #(dispatch [:help/toggle])}]
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


(defn layout []
  (let [route (subscribe [:route/current])
        step  (subscribe [:step/current])]
    (log/log "route and step" @route @step)
    (case (:page @route)
      :welcome [welcome-layout]
      :logout  [content/view @route]
      [layout/layout
       {:nav    [nav]
        :footer [footer]}
       [content/view @route]])))
