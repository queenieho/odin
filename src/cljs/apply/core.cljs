(ns apply.core
  (:require [accountant.core :as accountant]
            [antizer.reagent :as ant]
            [apply.content :as content]
            [apply.events]
            [apply.routes :as routes]
            [apply.subs]
            [day8.re-frame.http-fx]
            [goog.dom :as gdom]
            [iface.components.ptm.layout :as layout]
            [iface.modules.graphql :as graphql]
            [iface.utils.routes :as iroutes]
            [reagent.core :as r]
            [re-frame.core :as rf :refer [dispatch subscribe]]
            [iface.components.ptm.icons :as icons]
            [iface.components.ptm.ui.form :as form]))


(defn logout []
  [:div.tr
   [:a {:href "/logout"} "Log Out"]])


(defn- personal-information []
  (let [form (r/atom {:name nil})]
    (fn []
      [:div
       [:div.w-60-l.w-100
        [:h4.section-label "Personal info"]
        [:h1 "Please fill out your personal information"]
        [:p "Some placeholder text here"]]
       [:div.page-content.w-90-l.w-100

        (.log js/console (:num @form))

        [form/form
         [form/form-item
          {:label "Name"
           ;; :optional true
           :error true
           :help  "This is a required field"}
          [form/text
           {:value     (:name @form)
            :on-change #(swap! form assoc :name (.. % -target -value))}]]

         [form/form-item
          {:label "Number"
           ;; :optional true
           :error true
           :help  "This is a required field"}
          [form/number
           {:value     (:num @form)
            :placeholder "select a number"
            :step 5
            :min 5
            :max 25
            :on-change #(swap! form assoc :num (.. % -target -value))}]]

         [form/form-item
          {:label "Radios"}
          [form/radio-group
           {:value     (:radio @form)
            :on-change #(swap! form assoc :radio (.. % -target -checked))
            :name      "radio"}
           [form/radio-option {:id 1} "thing one"]
           [form/radio-option {:id 2} "thing two"]
           [form/radio-option {:id 3} "thing three"]]]

         [form/form-item
          {:label "Checkbox"
           :error true
           }
          [form/checkbox
           {:value     (:checked @form)
            :on-change #(swap! form assoc :checked (.. % -target -checked))}
           "This is a thing to check"]]

         [form/form-item
          {:label "Select"
           ;; :help  "Error!"
           :error true
           }
          [form/select
           {:value       (:select @form)
            :on-change   #(swap! form assoc :select (:value %))
            :rows        5
            :placeholder "some placeholder text"}
           [form/select-option {:value 1} "one"]
           [form/select-option {:value 2} "two"]]]

         [form/form-item
          {:label "Textarea"
           :help  "Error!"}
          [form/textarea
           {:value       (:textarea @form)
            :on-change   #(swap! form assoc :textarea (.. % -target -value))
            :rows        5
            :error       true
            :placeholder "some placeholder text"}]]
         ]


        ]])))


(defn- welcome-1 [{name :name} toggle]
  [:div
   [:div.w-60-l.w-100.center
    [:h1.tc "Welcome to Starcity,"]
    [:h2.tc.handwriting name]]
   [:div.page-content.w-90-l.w-100.center.tc
    [:p.tc "You've taken your first step to joining a Starcity community."]
    [:p.tc "We're looking forward to getting to know you."]
    [:br]
    [:p.tc "I'm here to help you with your application. If you have a"]
    [:p.tc "question, click on the " (icons/icon {:type "help" :class "icon-small"}) " icon to send me a message."]
    [:br]
    [:div
     [ant/avatar
      {:icon "user"}]]
    [:h3 "- Mattlio from Chatlio"]
    [:br]
    [:br]

    #_[ui/button
       {:on-click #(.log js/console "yes, this is dog")
        :type     :secondary
        :class    "mt5"}
       "Let's go!"]

    #_[ui/pill {:active false} "pill"]]])


(defn- welcome-2 []
  [:section.main.main-no-nav.center
   [:div.w-60-l.w-100.center
    [:h1.tc "Here's what you'll need"]]
   [:div.page-content.w-90-l.w-100.center.tc
    [:div.cf.mt5
     [:div.w-third-l.w-100.fl.ph4
      [:svg
       [:use {:xlinkHref "#truck"}]]
      [:h3 "Logistics"]
      [:p "We’ll need to know stuff like which communities you'd like to join, you’re preferred move-in date, and how long you'll be staying with us."]]
     [:div.w-third-l.w-100.fl.ph4
      [:svg
       [:use {:xlinkHref "#paper"}]]
      [:h3 "Personal Information"]
      [:p "Provide us with some personal information so that we can perform a background check and verify your income."]]
     [:div.w-third-l.w-100.fl.ph4
      [:svg
       [:use {:xlinkHref "#credit-card"}]]
      [:h3 "Application Fee"]
      [:p "It’ll cost $25 for each application – that helps cover the cost of the background check."]]]
    [:button.button.mt5
     {:on-click #(.log js/console "click!")}
     "Got it"]]])


(defmethod content/view :welcome [{:keys [requester] :as route}]
  (let [toggle (r/atom false)]
    (fn []
      [:div
       [:div.w-60-l.w-100.center
        [:svg.logo
         [:use {:xlinkHref "#logomark"}]]]
       (if @toggle
         [welcome-2]
         [welcome-1 requester toggle])
       [:div.bg-top]])))


(defn nav []
  [layout/nav
   {:is-progress? true
    :footer       [layout/nav-footer
                   [layout/nav-item {:label "Help"
                                     :icon  "help"}]
                   [layout/nav-item {:label "Log Out"
                                     :icon  "logout"}]]}
   [layout/nav-item {:progress :complete
                     :label    [:span "Member" [:br] "Agreement"]
                     :icon     "check"}]
   [layout/nav-item {:progress :active
                     :label    "Helping Hands"
                     :icon     "moving-box"}]
   [layout/nav-item {:label "Security Deposit"
                     :icon  "credit-card"}]])


(defn footer []
  [layout/footer
   {:primary     {:label  "some"
                  :action #(js/console.log "clicked")}
    :secondary   {:label "sedondardafsasd;lkdfjasdfuewfsadfasdf"
                  :label-small "ned"}
    :destructive {:label "back two"}}])


(defn layout []
  [layout/layout
   {:nav    [nav]
    :footer [footer]
    ;; :pre    (list [:div.bg-top {:key 1}]
    ;;               #_(icons/icon {:type "logomark"}))
    }
   [personal-information]
   #_[welcome-1 {:name "Bob Loblaw"} (r/atom false)]])


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
