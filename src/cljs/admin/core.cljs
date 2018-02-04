(ns admin.core
  (:require [accountant.core :as accountant]
            [admin.content :as content]
            [admin.events]
            [admin.accounts.views]
            [admin.history]
            [admin.kami.views]
            [admin.metrics.views]
            [admin.orders.views]
            [admin.routes :as routes]
            [admin.subs]
            [antizer.reagent :as ant]
            [cljsjs.moment]
            [day8.re-frame.http-fx]
            [goog.dom :as gdom]
            [iface.components.layout :as layout]
            [iface.modules.graphql :as graphql]
            [iface.modules.loading :as loading]
            [iface.modules.modal]
            [iface.modules.notification]
            [iface.modules.payments]
            [iface.utils.routes :as iroutes]
            [reagent.core :as r]
            [re-frame.core :as rf :refer [dispatch subscribe]]
            [toolbelt.re-frame.fx]
            [taoensso.timbre :as timbre]))


(enable-console-print!)


;; ==============================================================================
;; modules ======================================================================
;; ==============================================================================


(loading/install-module!)

(graphql/install-module!
 "/api/graphql"
 {:on-unauthenticated (fn [_]
                        {:route "/logout"})
  :on-error-fx        (fn [[k _]]
                        {:dispatch [:ui/loading k false]})})


;; ==============================================================================
;; views ========================================================================
;; ==============================================================================


(defn- nav-user-menu []
  [ant/menu
   [ant/menu-item {:key "profile-link"}
    [:a {:href (routes/path-for :profile/membership)} "My Profile"]]
   [ant/menu-item {:key "log-out"}
    [:a {:href "/logout"} "Log Out"]]])


(defn navbar []
  (let [account             (subscribe [:user])
        menu-items          (subscribe [:menu/items])
        mobile-menu-showing (subscribe [:layout.mobile-menu/showing?])
        active              (subscribe [:route/root])]
    [layout/navbar {:mobile-menu-showing @mobile-menu-showing
                    :on-menu-click       #(dispatch [:layout.mobile-nav/toggle])}
     [layout/navbar-menu-items @menu-items @active]
     [layout/navbar-menu-profile
      (:name @account) [nav-user-menu]]]))


(defn layout []
  (let [route (subscribe [:route/current])]
    [layout/layout
     [navbar]
     [layout/content
      [content/view @route]]]))


;; ==============================================================================
;; app entry ====================================================================
;; ==============================================================================


(defn render []
  (r/render
   [ant/locale-provider {:locale (ant/locales "en_US")}
    [layout]]
   (gdom/getElement "admin")))


(defn reload! []
  (render)
  (accountant/dispatch-current!))


(defn ^:export run []
  ;; TODO: get account from json
  (rf/dispatch-sync [:app/init {:name "Josh Lehman"}])
  (iroutes/hook-browser-navigation! routes/app-routes)
  (render))