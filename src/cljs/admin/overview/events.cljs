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
   #_[:dashboard.orders/fetch]
   [:services.orders/query {:statuses [:pending :placed]}]
   [:payments/fetch]
   [:accounts.list/fetch {:sort-order :asc
                          :sort-by :unit
                          :selected-view "member"}]
   #_[:dashboard.payments/fetch]
   #_[:dashboard.members/fetch]])


(reg-event-fx
 :overview.orders/fetch
 [(path db/path)]
 (fn [{db :db} k]
   {:graphql {:query [[:orders {:params {:statuses [:pending :placed]}}
                       [:id :name :status :created
                        [:account [:id :name]]]]]
              :on-success [::orders-fetch]
              :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::orders-fetch
 [(path db/path)]
 (fn [{db :db} [_ response]]
   {:db (assoc db :orders (get-in response [:data :orders]))}))
