(ns apply.events
  (:require [apply.db :as db]
            [apply.routes :as routes]
            [toolbelt.core :as tb]
            [iface.utils.log :as log]
            [iface.utils.time :as time]
            [iface.utils.formatters :as format]
            [clojure.string :as s]
            [starcity.re-frame.chatlio-fx]
            [re-frame.core :refer [reg-event-db reg-event-fx path]]))


;; ==============================================================================
;; helpers ======================================================================
;; ==============================================================================


(defmulti gql->rfdb (fn [k v] k))


(defmethod gql->rfdb :id [_ v] :application-id)


(defmethod gql->rfdb :status [_ v] :application-status)


(defmethod gql->rfdb :default [k v] nil)


(defmulti gql->value
  "To be used to parse graphql responses into the right format for our app db"
  (fn [k v] k))


(defmethod gql->value :default [k v] v)


(def application-attrs
  [:id :status :term :move_in_range :move_in :occupancy :has_pet :about
   [:pet [:id :name :breed :weight :sterile :vaccines :bitten
          :demeanor :daytime_care :about :type]]
   [:communities [:id :code]]
   [:current_location [:id :locality :region :country :postal_code]]
   [:income [:id :name :uri]]])


(defn parse-gql-response
  "given a re-frame app-db and a graphql application object, dispatch a
  multimethod that will send each datum from the graphql response to the correct
  place within the app-db"
  [db application]
  (reduce-kv
   (fn [d k v]
     (assoc d (gql->rfdb k v) (gql->value k v)))
   db
   application))


(defn- get-account-params
  "Gets the needed params for updating an account's information"
  [{:keys [first-name last-name middle-name phone dob]}]
  (let [date-string (str (:year dob) " " (:month dob) " " (:day dob))]
    (tb/assoc-when {}
                   :first_name first-name
                   :last_name last-name
                   :middle_name middle-name
                   :dob (time/moment->iso date-string)
                   :phone phone)))


;; ==============================================================================
;; bootstrap ====================================================================
;; ==============================================================================


(reg-event-fx
 :app/init
 (fn [_ [_ account]]
   (log/log "initting in application" account)
   {:db       (db/bootstrap account)
    :dispatch-n [[:ui/loading :app/init true]
                 [:app.init/fetch-application account]]}))


;; We need to fetch the application when the app is first bootstrapped, so we
;; can determine where the applicant last left off in the process. We must use
;; the account id to find the correct application for this first request, then
;; store the application id in the app db.
(reg-event-fx
 :app.init/fetch-application
 (fn [_ [_ {:keys [id] :as account}]]
   {:graphql {:query      [[:account {:id id}
                            [:name :id :first_name :phone :middle_name :last_name :dob
                             [:application application-attrs]]]
                           [:properties [:id :name :code :cover_image_url :copy_id
                                         [:application_copy [:name :images :introduction :building
                                                             :neighborhood :community
                                                             [:amenities [:label :icon]]]]
                                         [:rates [:rate :term]]
                                         [:units [[:occupant [:id]]]]]]
                           [:account_background_check {:id id}
                            [:id :consent :created]]
                           [:license_terms
                            [:id :term]]]
              :on-success [::init-fetch-application-success]
              :on-failure [:graphql/failure]}}))


(defn- create-init-db
  [db {:keys [properties license_terms account account_background_check]}]
  (let [{:keys [first_name middle_name last_name dob phone]} account
        [month day year]                                     (when dob
                                                               (-> dob
                                                                   format/date-short-num
                                                                   (s/split #"/")))]
    (merge db
           {:communities-options            properties
            :license-options                license_terms
            :background-check-id            (:id account_background_check)
            :personal/phone-number          phone
            :personal.background-check/info {:first-name  first_name
                                             :last-name   last_name
                                             :middle-name middle_name
                                             :dob         {:month (when-let [m month]
                                                                    (js/parseInt m))
                                                           :day   (when-let [d day]
                                                                    (js/parseInt d))
                                                           :year  (when-let [y year]
                                                                    (js/parseInt y))}}})))


;; At this point, we'll assume that if we received a nil value for the
;; application, then an application simply doesn't exist for that account. In
;; that case, we should create one.
(reg-event-fx
 ::init-fetch-application-success
 (fn [{db :db} [_ response]]
   (let [init-db (create-init-db db (:data response))]
     (if-let [application (get-in response [:data :account :application])]
       {:db       (assoc init-db
                         :application-id (:id application)
                         :application-status (:status application))
        :dispatch [:app.init/application-dashboard application]}
       {:db       init-db
        :dispatch [:app.init/create-application (get-in response [:data :account :id])]}))))


(reg-event-fx
 :app.init/application-dashboard
 (fn [{db :db} [_ application]]
   {:db       (parse-gql-response db application)
    :dispatch [:ui/loading :app/init false]
    :chatlio/ready [:init-chatlio]
    :route    (routes/path-for :applications)}))


(defn- last-saved-step [db]
  (cond
    ;; if theres consent to background check is false
    (false? (:personal/background-check db))
    :personal.background-check/declined

    ;; if move-out date is too far
    (>= (time/days-between (:logistics.move-in-date/choose-date db)) 45)
    :logistics.move-in-date/outside-application-window

    (not-empty (:personal/about db))
    :payment/review

    ;; if there's income verification
    (not-empty (:personal/income db))
    :personal/about

    ;; if there's background-check information
    (db/step-complete? db :personal.background-check/info)
    :personal/income

    ;; if there's consent to background check
    (true? (:personal/background-check db))
    :personal.background-check/info

    ;; if there's a phone number
    ;; the phone number sometimes is collected before app
    ;; so check if a term has been selected too
    (and (not-empty (:personal/phone-number db)) (some? (:community/term db)))
    :personal/background-check

    ;; if term has been selected
    (some? (:community/term db))
    :personal/phone-number

    ;; if community has been selected
    (not-empty (:community/select db))
    :community/term

    ;; if there's no pets, or pets and yes information
    (false? (:logistics/pets db))
    :community/select

    ;; if theres pets AND their information has been filled in
    (db/step-complete? db :logistics.pets/dog)
    :community/select

    (db/step-complete? db :logistics.pets/other)
    :community/select

    ;; if there's pets but no info
    ;; figure out which pet page to go toggle
    (true? (:logistics/pets db))
    (if (= :dog (get-in db [:logistics.pets/dog :type]))
      :logistics.pets/dog
      :logistics.pets/other)

    ;; if occupancy has been selected
    (some? (:logistics/occupancy db))
    :logistics/pets

    ;; if logistics/move in is asap or flexible
    (or (= :asap (:logistics/move-in-date db))
        (= :flexible (:logistics/move-in-date db)))
    :logistics/occupancy

    ;; if move-in/choose-date is filled
    (and (= :date (:logistics/move-in-date db))
         (some? (:logistics.move-in-date/choose-date db)))
    :logistics/occupancy

    ;; if choose date is nil
    (and (= :date (:logistics/move-in-date db))
         (nil? (:logistics.move-in-date/choose-date db)))
    :logistics.move-in-date/choose-date

    :otherwise
    :logistics/move-in-date))


(reg-event-fx
 :app.init/route-to-last-saved
 (fn [{db :db} _]
   {:route (db/step->route (last-saved-step db))}))


(reg-event-fx
 :app.init/create-application
 (fn [_ [_ account-id]]
   {:graphql {:mutation   [[:application_create {:account account-id}
                            [:id]]
                           [:create_background_check {:account account-id}
                            [:id :consent]]]
              :on-success [::init-create-application-success]
              :on-failure [:graphql/failure]}
    :route   (routes/path-for :welcome)}))


(reg-event-fx
 ::init-create-application-success
 (fn [{db :db} [_ response]]
   (let [application-id (get-in response [:data :application_create :id])
         bg-check-id    (get-in response [:data :create_background_check :id])]
     {:db            (assoc db :application-id application-id
                            :background-check-id bg-check-id)
      :dispatch      [:ui/loading :app/init false]
      :chatlio/ready [:init-chatlio]})))


(reg-event-fx
 :init-chatlio
 (fn [_ _]
   (let [email (aget js/window "account" "email")
         name  (aget js/window "account" "name")]
     {:chatlio/show     false
      :chatlio/identify [email {:name name}]})))


(reg-event-fx
 :help/toggle
 (fn [{db :db} _]
   (let [is-open (:chatlio/show db)]
     {:db           (assoc db :chatlio/show (not is-open))
      :chatlio/show (not is-open)})))


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
                                (dissoc :first-name :last-name :middle-name :dob
                                        :background-check-consent))
         account-params     (get-account-params params)
         bg-check-params    (tb/assoc-some {}
                                           :consent (:background-check-consent params))]
     {:dispatch [:ui/loading :step.current/save true]
      :graphql  {:mutation   [[:application_update {:application (:application-id db)
                                                    :params      application-params}
                               application-attrs]
                              [:update_account {:id   (get-in db [:account :id])
                                                :data account-params}
                               [:first_name :middle_name :last_name :dob :phone]]
                              [:update_background_check
                               {:background_check_id (:background-check-id db)
                                :params              bg-check-params}
                               [:id :consent]]]
                 :on-success [::application-update-success]
                 :on-failure [:graphql/failure]}})))


(reg-event-fx
 ::application-update-success
 (fn [{db :db} [_ response]]
   (let [application (get-in response [:data :application_update])
         bg-check    (get-in response [:data :update_background_check])
         new-db      (if (some? (:has_pet application))
                       (dissoc db :logistics.pets/dog :logistics.pets/other)
                       db)]
     {:db       (merge
                 (parse-gql-response new-db application)
                 {:personal/background-check (:consent bg-check)})
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


(reg-event-fx
 :finish
 (fn [{db :db} _]
   {:route (routes/path-for :applications)}))


(reg-event-fx
 :step/edit
 (fn [{db :db} [_ step]]
   {:route (db/step->route step)}))
