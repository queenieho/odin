(ns apply.core
  (:require [accountant.core :as accountant]
            [antizer.reagent :as ant]
            [apply.content :as content]
            [apply.events]
            [apply.routes :as routes]
            [apply.subs]
            [day8.re-frame.http-fx]
            [goog.dom :as gdom]
            [iface.components.layout :as layout]
            [iface.modules.graphql :as graphql]
            [iface.utils.routes :as iroutes]
            [reagent.core :as r]
            [re-frame.core :as rf :refer [dispatch subscribe]]))


(defn logout []
  [:div.tr
   [:a {:href "/logout"} "Log Out"]])

(defn- welcome-1 [{name :name}]
  [:section.main.main-no-nav.center
   [:div.w-60-l.w-100.center
    [:h1.tc "Welcome to starcity,"]
    [:h2.tc name]]
   [:div.page-content.w-90-l.w-100.center.tc
    [:p.tc "You've taken your first step to joining a Starcity community."]
    [:p.tc "We're looking forward to getting to know you."]
    [:br]
    [:p.tc "I'm here to help you with your application. If you have a"]
    [:p.tc "question, click on the Q&A icon to send me a message."]
    [:br]
    [:button.button.bt5
     {:on-click #(.log js/console "click!" name)}
     "Let's go!"]]])


(defmethod content/view :welcome [{:keys [requester] :as route}]
  [:div
   [welcome-1 requester]
   [:div.bg-top]])


(defn layout []
  (let [route (subscribe [:route/current])]
    [layout/layout
     [logout]
     [layout/content
      [content/view @route]]]))


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
