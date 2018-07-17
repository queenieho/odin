(ns apply.events
  (:require [apply.db :as db]
            [apply.routes :as routes]
            [re-frame.core :refer [reg-event-db reg-event-fx path]]
            [toolbelt.core :as tb]
            [iface.utils.log :as log]
            [iface.utils.time :as time]))


;; ==============================================================================
;; helpers ======================================================================
;; ==============================================================================


(defmulti gql->rfdb (fn [k v] k))


(defmethod gql->rfdb :id [_ v] :application-id)


(defmethod gql->rfdb :default [k v]
  (log/log "parsing gql response: you should never reach this method! " k))


(defmulti gql->value
  "To be used to parse graphql responses into the right format for our app db"
  (fn [k v] k))


(defmethod gql->value :default [k v] v)


(def application-attrs
  [:id :term :move_in_range :move_in :occupancy :has_pet
   [:pet [:id :name :breed :weight :sterile :vaccines :bitten
          :demeanor :daytime_care :about :type]]
   [:communities [:id :code]]
   [:current_location [:id :locality :region :country :postal_code]]])


(defn parse-gql-response
  "given a re-frame app-db and a graphql application object, dispatch a
  multimethod that will send each datum from the graphql response to the correct
  place within the app-db"
  [db application]
  (js/console.log "parsing graphql response... " application)
  (reduce-kv
   (fn [d k v]
     (assoc d (gql->rfdb k v) (gql->value k v)))
   db
   application))


(defn- get-account-params
  "Gets the needed params for updating an account's information"
  [{:keys [first-name last-name middle-name phone dob]}]
  (tb/assoc-when {}
                 :first_name first-name
                 :last_name last-name
                 :middle_name middle-name
                 :dob (when-let [d dob] (time/moment->iso d))
                 :phone phone))


;; ==============================================================================
;; bootstrap ====================================================================
;; ==============================================================================


(reg-event-fx
 :app/init
 (fn [_ [_ account]]
   {:db       (db/bootstrap account)
    :dispatch [:app.init/fetch-application account]}))


;; We need to fetch the application when the app is first bootstrapped, so we
;; can determine where the applicant last left off in the process. We must use
;; the account id to find the correct application for this first request, then
;; store the application id in the app db.
(reg-event-fx
 :app.init/fetch-application
 (fn [_ [_ {:keys [id] :as account}]]
   (log/log "fetching account:" id)
   {:graphql {:query      [[:account {:id id}
                            [:name :id :first_name :middle_name :last_name :dob
                             [:application application-attrs]]]
                           [:properties [:id :name :code :cover_image_url :copy_id
                                         [:application_copy [:name :images :introduction :building
                                                             :neighborhood :community
                                                             [:amenities [:label :icon]]]]
                                         [:rates [:rate]]
                                         [:units [[:occupant [:id]]]]]]
                           [:account_background_check {:id id}
                            [:id :consent :created]]
                           [:license_terms
                            [:id :term]]]
              :on-success [::init-fetch-application-success]
              :on-failure [:graphql/failure]}}))


(defn- create-init-db
  [db {:keys [properties license_terms account]}]
  (let [{:keys [first_name middle_name last_name dob]} account]
    (merge db
           {:communities-options            properties
            :license-options                license_terms
            :personal.background-check/info {:first-name  first_name
                                             :last-name   last_name
                                             :middle-name middle_name
                                             :dob         (when-let [d dob]
                                                            (time/iso->moment d))}})))


;; At this point, we'll assume that if we received a nil value for the
;; application, then an application simply doesn't exist for that account. In
;; that case, we should create one.
(reg-event-fx
 ::init-fetch-application-success
 (fn [{db :db} [_ response]]
   (let [init-db (create-init-db db (:data response))]
     (log/log "application query" init-db)
     (log/log "background check" (get-in response [:data :account_background_check]))
     (if-let [application (get-in response [:data :account :application])]
       {:db       (assoc init-db :application-id (:id application))
        :dispatch [:app.init/somehow-figure-out-where-they-left-off application]}
       {:db       init-db
        :dispatch [:app.init/create-application (get-in response [:data :account :id])]}))))



;;TODO
(reg-event-fx
 :app.init/somehow-figure-out-where-they-left-off
 (fn [{db :db} [_ application]]
   (log/log "processing application..." application)
   {:db       (parse-gql-response db application)
    :dispatch [:app.init/route-to-last-saved]}))


(reg-event-fx
 :app.init/route-to-last-saved
 (fn [{db :db} _]
   (log/log "last saved step" (-> db db/last-saved db/step->route))
   {:route (-> db db/last-saved db/step->route) #_(routes/path-for :section/step :section-id :logistics :step-id :move-in-date)}))


;;TODO
(reg-event-fx
 :app.init/create-application
 (fn [_ [_ account-id]]
   (log/log  "no application found for %s, creating new one..." account-id)
   {:graphql {:mutation   [[:application_create {:account account-id}
                            [:id]]]
              :on-success [::init-create-application-success]
              :on-failure [:graphql/failure]}
    :route   (routes/path-for :welcome)}))


(reg-event-fx
 ::init-create-application-success
 (fn [{db :db} [_ response]]
   (let [application-id (get-in response [:data :application_create :id])]
     {:db (assoc db :application-id application-id)})))


;; ==============================================================================
;; update application ===========================================================
;; ==============================================================================


(reg-event-fx
 :application/update
 (fn [{db :db} [_ params]]
   ;; NOTE somehow graphql doesn't like communities being in a list
   ;; so I have to move it into a vector before the mutation
   (let [application-params (-> (tb/transform-when-key-exists params
                                  {:communities #(into [] %)})
                                (dissoc :first-name :last-name :middle-name :dob))
         account-params     (get-account-params params)]
     (log/log "updating application " application-params)
     {:dispatch [:ui/loading :step.current/save true]
      :graphql  {:mutation   [[:application_update {:application (:application-id db)
                                                    :params      application-params}
                               application-attrs]
                              [:update_account {:id   (get-in db [:account :id])
                                                :data account-params}
                               [:first_name :middle_name :last_name :dob :phone]]]
                 :on-success [::application-update-success]
                 :on-failure [:graphql/failure]}})))


(reg-event-fx
 ::application-update-success
 (fn [{db :db} [_ response]]
   (let [application (get-in response [:data :application_update])]
     {:db       (parse-gql-response db application)
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
   {:route    (next-route db params)
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
