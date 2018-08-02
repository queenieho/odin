(ns apply.applications.views
  (:require [iface.components.ptm.layout :as layout]
            [iface.utils.log :as log]
            [apply.content :as content]
            [apply.routes :as routes]
            ))


(defmethod content/view :applications []
  [layout/layout
   {:nav [layout/nav {:footer [layout/nav-footer
                               [layout/nav-item {:label "Log Out"
                                                 :link  (routes/path-for :logout)
                                                 :icon  "logout"}]]}
          [layout/nav-item {:label    "Application"
                            :disabled false}]]}
   [:h1 "Welcome applications!"]])
