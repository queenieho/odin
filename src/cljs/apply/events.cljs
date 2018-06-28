(ns apply.events
  (:require [apply.db :as db]
            [apply.routes :as routes]
            [re-frame.core :refer [reg-event-db reg-event-fx path]]
            [toolbelt.core :as tb]
            [iface.utils.log :as log]))


;; ==============================================================================
;; helpers ======================================================================
;; ==============================================================================


(def application-attrs
  [:id :term :move_in :occupancy])


;; ==============================================================================
;; bootstrap ====================================================================
;; ==============================================================================


(reg-event-fx
 :app/init
 (fn [_ [_ account]]
   {:db (db/bootstrap account)
    :dispatch [:app.init/fetch-application account]}))


;; We need to fetch the application when the app is first bootstrapped, so we
;; can determine where the applicant last left off in the process. We must use
;; the account id to find the correct application for this first request, then
;; store the application id in the app db.
(reg-event-fx
 :app.init/fetch-application
 (fn [_ [_ {:keys [id] :as account}]]
   (log/log "fetching account:" id)
   {:graphql {:query [[:account {:id id}
                       [:name :id
                        [:application application-attrs]]]]
              :on-success [::init-fetch-application-success]
              :on-failure [:graphql/failure]}}))


;; At this point, we'll assume that if we received a nil value for the
;; application, then an application simply doesn't exist for that account. In
;; that case, we should create one.
(reg-event-fx
 ::init-fetch-application-success
 (fn [{db :db} [_ response]]
   (if-let [application (get-in response [:data :account :application])]
     {:db (assoc db :application-id (:id application))
      :dispatch [:app.init/somehow-figure-out-where-they-left-off application]}
     {:dispatch [:app.init/create-application (get-in response [:data :account :id])]})))


;;TODO
(reg-event-fx
 :app.init/somehow-figure-out-where-they-left-off
 (fn [{db :db} [_ application]]
   (log/log "let's process this application" application)
   {:db (tb/assoc-when db
                       :application-id (:id application)
                       :logistics.move-in-date/choose-date (:move_in application))}))


;;TODO
(reg-event-fx
 :app.init/create-application
 (fn [_ [_ account-id]]
   (log/log  "no application found for %s, creating new one..." account-id)
   {:graphql {:mutation   [[:application_create {:account account-id}
                            [:id]]]
              :on-success [::init-create-application-success]
              :on-failure [:graphql/failure]}}))


(reg-event-fx
 ::init-create-application-success
 (fn [{db :db} [_ response]]
   (let [application-id (get-in response [:data :application_create :id])]
     {:db (assoc db :application-id application-id)})))


;; ==============================================================================
;; update application ===========================================================
;; ==============================================================================


(defmulti gql->rfdb (fn [k] k))


(defmethod gql->rfdb :default [k]
  (log/log "parsing gql response: you should never reach this method! " k))


(reg-event-fx
 :application/update
 (fn [{db :db} [_ params]]
   (log/log "updating application with params" params)
   {:graphql {:mutation [[:application_update {:application (:application-id db)
                                               :params      params}
                          (into [] (keys params))]]
              :on-success [::application-update-success]
              :on-failure [:graphql/failure]}}))


(reg-event-fx
 ::application-update-success
 (fn [{db :db} [_ response]]
   (let [application (get-in response [:data :application_update])]
     (log/log "saved! here's what i got" application)
     {:db (reduce-kv
           (fn [d k v]
             (assoc d (gql->rfdb k) v))
           db
           application)
      :dispatch [:step/advance]})))


;; ==============================================================================
;; top-level ====================================================================
;; ==============================================================================


(reg-event-fx
 :ptm/start
 (fn [_ _]
   ;; TODO: Do server stuff
   {:route (db/step->route db/first-step)}))


;; ==============================================================================
;; nav ==========================================================================
;; ==============================================================================


(reg-event-fx
 :nav.item/select
 (fn [{db :db} [_ nav-item]]
   (tb/assoc-when
    {}
    :route (when (db/can-navigate? db (:section nav-item))
             (routes/path-for :section/step
                              :section-id (name (:section nav-item))
                              :step-id (name (:first-step nav-item)))))))


;; ==============================================================================
;; steps ========================================================================
;; ==============================================================================


(defn- next-route
  [db params]
  (-> db db/next-step db/step->route))


(defn- default-save-fx [db params]
  (let [res {:route    (next-route db params)
             :dispatch [:ui/loading :step.current/save false]}]
    res))


(defmulti save-step-fx
  (fn [db params]
    (-> db :route db/route->step)))


(defmethod save-step-fx :default [db params]
  (default-save-fx db params))


(reg-event-fx
 :step/advance
 (fn [{db :db} [k params]]
   {:route (next-route db params)}))


(reg-event-fx
 :step.current/save
 (fn [{db :db} [k params]]
   (merge {:dispatch [:ui/loading k true]}
          (save-step-fx db params))))


(reg-event-fx
 :step.current/next
 (fn [{db :db} [_ params]]
   {:dispatch [:step.current/save params]}))
