(ns onboarding.core
  (:require [accountant.core :as accountant]
            [antizer.reagent :as ant]
            [day8.re-frame.http-fx]
            [goog.dom :as gdom]
            [iface.modules.graphql :as graphql]
            [iface.utils.routes :as iroutes]
            [onboarding.events]
            [onboarding.routes :as routes]
            [onboarding.sections.security-deposit]
            [onboarding.sections.helping-hands]
            [onboarding.sections.member-agreement]
            [onboarding.subs]
            [onboarding.views :as views]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch-sync]]
            [toolbelt.re-frame.fx]))


(enable-console-print!)


(defn render []
  (r/render
   [ant/locale-provider {:locale (ant/locales "en_US")}
    [views/layout]]
   (gdom/getElement "onboarding")))


(defn ^:export run []
  (let [account (js->clj (aget js/window "account") :keywordize-keys true)]
    (graphql/configure
     "/api/graphql"
     {:on-unauthenticated (fn [_]
                            {:route "/logout "})
      :on-error-fx        (fn [[k _]]
                            {:dispatch [:ui/loading k false]})})
    (dispatch-sync [:app/init account])
    (iroutes/hook-browser-navigation! routes/app-routes)
    (render)))
