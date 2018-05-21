(ns admin.core
  (:require [accountant.core :as accountant]
            [admin.accounts.views]
            [admin.content :as content]
            [admin.events]
            [admin.fx]
            [admin.history]
            [admin.kami.views]
            [admin.metrics.views]
            [admin.profile.views]
            [admin.properties.views]
            [admin.services.views]
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
            [starcity.re-frame.stripe-fx]
            [toolbelt.re-frame.fx]
            [taoensso.timbre :as timbre]
            [toolbelt.core :as tb]))


(enable-console-print!)


;; ==============================================================================
;; modules ======================================================================
;; ==============================================================================


;; ==============================================================================
;; views ========================================================================
;; ==============================================================================


(defn- create-note-button []
  [ant/button
   {:style    {:margin "auto"}
    :type     :primary
    :size     :large
    :icon     :plus
    :on-click #(dispatch [:note.create/toggle])}
   "Create note"])


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
                    :on-menu-click       #(dispatch [:layout.mobile-menu/toggle])}
     [layout/navbar-menu-items @menu-items @active]
     [layout/navbar-menu-profile
      (:name @account) [nav-user-menu] [create-note-button]]]))


;; create note ===================================================================


(defn create-note-footer [form]
  [:div
   [ant/button
    {:size     :large
     :on-click #(dispatch [:note.create/cancel])}
    "Cancel"]
   [ant/button
    {:type     :primary
     :size     :large
     :on-click #(dispatch [:note.create/create-note! form])}
    "Create"]])


(defn create-note-modal []
  (let [creating-note? (subscribe [:note/showing?])
        form           (subscribe [:note/form])
        accounts       (subscribe [:accounts])
        properties     (subscribe [:properties/list])]
    (r/create-class
     {:component-will-mount
      (fn [_]
        )
      :reagent-render
      (fn [_]
        [ant/modal
         {:title     "Create a note"
          :visible   @creating-note?
          :on-cancel #(dispatch [:note.create/cancel])
          :footer    (r/as-element [create-note-footer @form])}
         [ant/form-item
          {:label "Mentions"}
          [ant/select
           {:style     {:width "100%"}
            :mode      "multiple"
            :value     (mapv str (:refs @form))
            :on-change #(let [ids (mapv tb/str->int (js->clj %))]
                          (dispatch [:note.form/update :refs ids]))}
           (map (fn [{:keys [name id]}]
                  [ant/select-option
                   {:value (str id)
                    :key   id}
                   name])
                @accounts)]]
         [ant/form-item
          {:label "Subject"}
          [ant/input
           {:placeholder "Note subject"
            :value       (:subject @form)
            :on-change   #(dispatch [:note.form/update :subject (.. % -target -value)])}]]
         [ant/form-item
          {:label "Note"}
          [ant/input
           {:type        :textarea
            :rows        10
            :placeholder "Note body"
            :value       (:content @form)
            :on-change   #(dispatch [:note.form/update :content (.. % -target -value)])}]]
         (when (some? (:notify @form))
           [ant/form-item
            [ant/checkbox {:checked   (:notify @form)
                           :on-change #(dispatch [:note.form/update :notify (.. % -target -checked)])}
             "Send Slack notification"]])])})))


(defn layout []
  (let [route          (subscribe [:route/current])]
    [layout/layout
     [create-note-modal]
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
