(ns member.content
  (:require [antizer.reagent :as ant]
            [iface.loading :as loading]
            [iface.utils.routes :as iroutes]
            [reagent.core :as r]
            [toolbelt.core :as tb]))



(defmulti view
  (fn [route]
    (iroutes/route-dispatch view route)))


(defmethod view :default [{:keys [page path root params]}]
  [ant/card {:title "View not found"}
   [:p [:b "Page: "] page]
   [:p [:b "Path: "] path]
   [:p [:b "Root: "] root]
   [:p [:b "Params: "] params]])


;; This component is rendered when the user navigations to the /logout entpoint.
;; Because we need a catch-all route in `member.routes` to prevent from hitting
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
