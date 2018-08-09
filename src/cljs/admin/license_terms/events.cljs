(ns admin.license-terms.events
  (:require [admin.license-terms.db :as db]
            [re-frame.core :refer [path reg-event-db reg-event-fx]]))


(reg-event-fx
 :license-terms/query
 [(path db/path)]
 (fn [_ [k _]]
   {:dispatch [:ui/loading k true]
    :graphql  {:query
               [[:license_terms {:only_available false}
                 [:id :term :available]]]
               :on-success [::license-terms-success k]
               :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::license-terms-success
 [(path db/path)]
 (fn [{db :db} [_ k result]]
   {:dispatch [:ui/loading k false]
    :db       (assoc db :license-terms (get-in result [:data :license_terms]))}))
