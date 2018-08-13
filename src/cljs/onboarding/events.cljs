(ns onboarding.events
  (:require [onboarding.db :as db]
            [onboarding.routes :as routes]
            [re-frame.core :refer [reg-event-fx path]]
            [starcity.re-frame.chatlio-fx]
            [toolbelt.core :as tb]
            [antizer.reagent :as ant]
            [iface.utils.log :as log]
            [devcards.core :as dc]))


;; ==============================================================================
;; bootstrap ====================================================================
;; ==============================================================================


(reg-event-fx
 :app/init
 (fn [_ [_ account]]
   (log/log "initting..." account)
   {:db       (db/bootstrap account)
    :dispatch [:app.init/fetch account]}))


(reg-event-fx
 :app.init/fetch
 (fn [_ [_ {:keys [id] :as account}]]
   (log/log "fetching" id)
   {:graphql {:query      [[:account {:id id}
                            [:name :id
                             [:application [:id]]]]]
              :on-success [::init-fetch-success]
              :on-failure [:graphql/failure]}}))


(reg-event-fx
 ::init-fetch-success
 (fn [{db :db} [_ response]]
   (log/log "fetched account on init" response)
   {:dispatch [:app.init/start-onboarding]}))


(reg-event-fx
 :app.init/start-onboarding
 (fn [{db :db} _]
   {:chatlio/ready [:init-chatlio]
    :route         (routes/path-for :welcome)}))


;; ==============================================================================
;; update onboarding ============================================================
;; ==============================================================================


(reg-event-fx
 :onboarding/update
 (fn [{db :db} [_ params]]
   ;; TODO add graphql mutation here
   ;; {:dispatch [:ui/loading :step.current/save true]}
   {:dispatch [::onboarding-update-success]}))


(reg-event-fx
 ::onboarding-update-success
 (fn [{db :db} [_ response]]
   ;; TODO add update to local db when this happens
   {:dispatch [:step/advance]}))


;; ==============================================================================
;; top-level ====================================================================
;; ==============================================================================


(reg-event-fx
 :onboarding/start
 (fn [_ _]
   {:route (db/step->route db/first-step)}))


;; ==============================================================================
;; nav ==========================================================================
;; ==============================================================================


(reg-event-fx
 :nav.item/logout
 (fn [_ _]
   {:route (routes/path-for :logout)}))


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
   {:route (next-route db params)
    :dispatch [:ui/loading :step.current/save false]}))


(reg-event-fx
 :step.current/save
 (fn [{db :db} [k params]]
   (merge {:dispatch [:ui/loading k true]}
          (save-step-fx db params))))


(reg-event-fx
 :step.current/next
 (fn [{db :db} [_ params]]
   {:dispatch [:step.current/save params]}))


(reg-event-fx
 :finish
 (fn [{db :db} _]
   {:route (routes/path-for :applications)}))


(reg-event-fx
 :step/edit
 (fn [{db :db} [_ step]]
   {:route (db/step->route step)}))
