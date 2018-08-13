(ns onboarding.routes
  (:require [iface.utils.routes :as iroutes]
            [re-frame.core :refer [dispatch reg-event-fx reg-fx]]))


(def app-routes
  ["/onboarding"
   [["/welcome" :welcome]

    [["/" :section-id] [[["/" :step-id] [["" :section/step]
                                         [["/" :substep-id] :section.step/substep]]]]]

    ["/logout" :logout]

    [true :home]]])


(def path-for
  (partial iroutes/path-for app-routes))


(defmulti dispatches
  "Define additional events to dispatch when `route` is navigated to.

  Matches either the key identified by `:page`, or to the first key in
  `path` (which represents the 'root' route.)."
  (fn [route]
    (iroutes/route-dispatch dispatches route)))


(defmethod dispatches :default [route] [])



(defmethod dispatches :home [_]
  [[::home]])


(iroutes/install-events-handlers! dispatches)


(reg-event-fx
 ::home
 (fn [_ _]
   {:route (path-for :welcome)}))


;; (def path-for (partial bidi/path-for app-routes))


#_(defn hook-browser-navigation! []
  (accountant/configure-navigation!
   {:nav-handler  (fn [path]
                    (let [match  (bidi/match-route app-routes path)
                          page   (:handler match)
                          params (:route-params match)]
                      (dispatch [:app/route page params])))
    :path-exists? (fn [path]
                    (boolean (bidi/match-route app-routes path)))})
  (accountant/dispatch-current!))

#_(reg-fx
 :route
 (fn [new-route]
   (if (vector? new-route)
     (let [[route query] new-route]
       (accountant/navigate! route query))
     (accountant/navigate! new-route))))
