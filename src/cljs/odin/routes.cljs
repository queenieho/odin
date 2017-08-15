(ns odin.routes
  (:require [accountant.core :as accountant]
            [bidi.bidi :as bidi]
            [re-frame.core :refer [dispatch reg-fx]]
            [toolbelt.core :as tb]))


(def app-routes
  [""
   [

    ;; ["/people" [["" :account/list]
    ;;               [["/" :account-id]
    ;;                [["" :account/entry]]]]]

    ["/profile" [["" :profile/membership]
                 ;; NOTE: Unnecessary because this is the default
                 ;; ["/membership" :profile/membership]
                 ["/contact" :profile/contact]
                 ["/payments"
                  [[""         :profile.payment/history]
                   ["/sources" :profile.payment/sources]]]
                 ["/settings"
                  [["/change-password" :profile.settings/change-password]]]]]

    ;; ["/communities" [["" :properties]]]

    ;; ["/services" [["" :services]]]

    [true :home]

    ]])


;; :profile.payment/sources => [:profile :payment :sources]


(defmulti dispatches (fn [route] (:page route)))


(defmethod dispatches :default [route]
  [])



(defn hook-browser-navigation!
  "Wire up the bidi routes to the browser HTML5 navigation."
  [routes]
  (accountant/configure-navigation!
   {:nav-handler  (fn [path]
                    (let [match  (bidi/match-route app-routes path)
                          page   (:handler match)
                          params (:route-params match)]
                      (dispatch [:route/change page params])))
    :path-exists? (fn [path]
                    (boolean (bidi/match-route app-routes path)))})
  (accountant/dispatch-current!))


(def path-for
  "Produce the path (URI) for `key`."
  (partial bidi/path-for app-routes))


(reg-fx
 :route
 (fn [new-route]
   (if (vector? new-route)
     (let [[route query] new-route]
       (accountant/navigate! route query))
     (accountant/navigate! new-route))))
