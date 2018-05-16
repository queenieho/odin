(ns admin.overview.events
  (:require [admin.routes :as routes]
            [admin.services.orders.events]
            [admin.overview.db :as db]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [toolbelt.core :as tb]))

(defmethod routes/dispatches :overview
  [{:keys [params] :as route}]
  [;; get all `active` orders
   [:services.orders/query {:statuses [:pending :placed]}]
   [:payments/fetch]
   [:accounts.list/fetch {:sort-order :asc
                          :sort-by :unit
                          :selected-view "member"}]])
