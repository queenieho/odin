(ns admin.dashboard.events
  (:require [admin.routes :as routes]))

(defmethod routes/dispatches :dashboard
  [{:keys [params] :as route}]
  (.log js/console "dashboard?" route)
  [])
