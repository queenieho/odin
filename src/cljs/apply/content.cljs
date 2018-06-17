(ns apply.content
  (:require [antizer.reagent :as ant]
            [iface.loading :as loading]
            [iface.utils.routes :as iur]
            [reagent.core :as r]
            [apply.db :as db]))

(defmulti view db/route->step)


(defmethod view :default [{:keys [page path root params]}]
  [ant/card {:title "View not found"}
   [:p [:b "Page: "] (pr-str page)]
   [:p [:b "Path: "] (pr-str path)]
   [:p [:b "Root: "] root]
   [:p [:b "Params: "] params]])


;; This component is rendered when the user navigations to the /logout entpoint.
;; Because we need a catch-all route in `apply.routes` to prevent from hitting
;; the server on un-implemented routes, this component is rendered and
;; /immediately/ reloads the window, causing a forced server request.
(defn- logout! []
  (r/create-class
   {:component-will-mount
    (fn [_]
      (.reload js/window.location))
    :reagent-render
    (fn []
      (loading/fullpage :text "Logging out..."))}))


(defmethod view :logout [_] [logout!])
