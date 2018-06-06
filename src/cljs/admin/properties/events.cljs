(ns admin.properties.events
  (:require [admin.properties.db :as db]
            [admin.routes :as routes]
            [ajax.core :as ajax]
            [antizer.reagent :as ant]
            [clojure.set :as set]
            [iface.utils.norms :as norms]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [taoensso.timbre :as timbre]
            [toolbelt.core :as tb]))


;; ==============================================================================
;; events =======================================================================
;; ==============================================================================


(reg-event-fx
 :properties/query
 [(path db/path)]
 (fn [{db :db} [k params]]
   {:dispatch [:ui/loading k true]
    :graphql  {:query
               [[:properties
                 [:id :name :code :cover_image_url
                  :has_financials
                  [:units [:id]]]]]
               :on-success [::properties-query k params]
               :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::properties-query
 [(path db/path)]
 (fn [{db :db} [_ k params response]]
   {:dispatch [:ui/loading k false]
    :db       (->> (get-in response [:data :properties])
                   (norms/normalize db :properties/norms))}))


(reg-event-fx
 :property/fetch
 [(path db/path)]
 (fn [_ [k property-id on-success]]
   {:dispatch [:ui/loading k true]
    :graphql  {:query
               [[:property {:id property-id}
                 [:id :name :code :cover_image_url :tours
                  [:rates [:rate :term]]
                  [:units [:id :code :name :number
                           [:rates [:id :rate :term]]
                           [:property [:id [:rates [:rate :term]]]]
                           [:occupant [:id :name
                                       [:active_license [:ends]]]]]]]]]
               :on-success [::fetch-property-success k on-success]
               :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::fetch-property-success
 [(path db/path)]
 (fn [{db :db} [_ k on-success response]]
   (let [property (get-in response [:data :property])]
     {:dispatch-n (tb/conj-when [[:ui/loading k false]] on-success)
      :db         (norms/assoc-norm db :properties/norms (:id property) property)})))


;; ==============================================================================
;; list view ====================================================================
;; ==============================================================================


(defmethod routes/dispatches :properties/list [_]
  [[:properties/query]])


(reg-event-fx
 :community.add-financial/show
 [(path db/path)]
 (fn [{db :db} [_ params]]
   {:dispatch-n [[:modal/show :community.add-financial/modal]
                 [:community.add-financial/update [] params]]}))


(reg-event-fx
 :community.add-financial/cancel
 [(path db/path)]
 (fn [{db :db} _]
   {:dispatch [:modal/hide :community.add-financial/modal]
    :db       (update-in db [:form] dissoc :financial)}))


(reg-event-db
 :community.create.form/update
 [(path db/path)]
 (fn [db [_ keys value]]
   (assoc-in db (apply conj [:form :community] keys) value)))


(reg-event-db
 :community.add-financial/update
 [(path db/path)]
 (fn [db [_ keys value]]
   (assoc-in db (apply conj [:form :financial] keys) value)))


(defn- create-bank-account-params [{:keys [account-number routing-number]}]
  {:account_number account-number
   :routing_number routing-number})


(defn- create-financial-info-params
  [{:keys [business-name tax-id first-name last-name ssn dob deposit ops]}]
  {:business_name business-name
   :tax_id        tax-id
   :first_name    first-name
   :last_name     last-name
   :ssn           ssn
   :dob           dob
   :deposit       (create-bank-account-params deposit)
   :ops           (create-bank-account-params ops)})


(reg-event-fx
 :community/add-financial-info!
 [(path db/path)]
 (fn [{db :db} [k id params]]
   {:dispatch [:ui/loading k true]
    :graphql {:mutation
              [[:community_add_financial_info {:id     id
                                               :params (create-financial-info-params params)}
                [:id]]]
              :on-success [::add-financial-info-success k]
              :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::add-financial-info-success
 [(path db/path)]
 (fn [{db :db} [_ k response]]
   (let [community-id (get-in response [:data :community_add_financial_info :id])]
     {:dispatch-n [[:ui/loading k false]
                   [:properties/query]
                   [:modal/hide :community.add-financial/modal]]
      :db         (update-in db [:form] dissoc :financial)})))


(defn- create-address-params [{:keys [address] :as params}]
  (assoc params :address (-> address
                             (assoc :postal_code (:postal-code address))
                             (dissoc :postal-code))))


(defn- create-community-params
  [{:keys [license-prices date] :as params}]
  (let [lps (map #(hash-map :term (first %) :price (second %)) license-prices)]
    (-> params
        (create-address-params)
        (assoc :license_prices lps :available_on date)
        (dissoc :license-prices :date))))


(reg-event-fx
 :community/create!
 [(path db/path)]
 (fn [{db :db} [k]]
   (let [params (create-community-params (get-in db [:form :community]))]
     {:dispatch [:ui/loading k true]
      :graphql  {:mutation   [[:community_create {:params params}
                               [:id]]]
                 :on-success [:communities.create/upload-cover-photo! k]
                 :on-failure [:graphql/failure k]}})))


(defn- files->form-data [files]
  (let [form-data (js/FormData.)]
    (doseq [file-key (.keys js/Object files)]
      (let [file (aget files file-key)]
        (.append form-data "files[]" file (.-name file))))
    form-data))


(reg-event-db
 :communities.create/cover-image-picked
 [(path db/path)]
 (fn [db [k file]]
   (assoc-in db [:new-community :cover-image] (files->form-data file))))


(reg-event-fx
 :communities.create/upload-cover-photo!
 [(path db/path)]
 (fn [{db :db} [_ k response]]
   (let [community-id (get-in response [:data :community_create :id])]
     {:http-xhrio {:uri             (str "/api/communities/" community-id "/cover-photo")
                   :body            (get-in db [:new-community :cover-image])
                   :method          :post
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::community-create-success k community-id]
                   :on-failure      [::community-add-cover-photo-failure k]}})))


(reg-event-fx
 ::community-create-success
 [(path db/path)]
 (fn [{db :db} [_ k community-id response]]
   {:dispatch-n [[:properties/query]
                 [:ui/loading k false]
                 [:community.create.form/clear]
                 [:modal/hide :communities.create/modal]]}))


(reg-event-fx
 ::community-add-cover-photo-failure
 [(path db/path)]
 (fn [{db :db} [_ k error]]
   (timbre/error error)
   (ant/notification-error {:message "Failed to upload photo to this community."})
   {:dispatch-n [[:ui/loading k false]]}))


(reg-event-fx
 :community.create/cancel
 [(path db/path)]
 (fn [{db :db} _]
   {:dispatch-n [[:community.create.form/clear]
                 [:modal/hide :communities.create/modal]]}))


(reg-event-db
 :community.create.form/clear
 [(path db/path)]
 (fn [db _]
   (-> (update-in db [:form] dissoc :community)
       (dissoc :new-community))))


;; ==============================================================================
;; entry view ===================================================================
;; ==============================================================================


(defmethod routes/dispatches :properties [{:keys [params]}]
  [[:property/fetch (tb/str->int (:property-id params))
    [::set-property-rates (tb/str->int (:property-id params))]]
   [:notes/fetch {:refs (tb/str->int (:property-id params))}]])


(defn update-rate [term new-rate rates]
  (map
   #(if (= (:term %) term)
      (assoc % :rate new-rate)
      %)
   rates))


;; property =====================================================================


(reg-event-db
 ::set-property-rates
 [(path db/path)]
 (fn [db [_ property-id]]
   (let [property (norms/get-norm db :properties/norms property-id)]
     (assoc-in db [:property-rates property-id] (:rates property)))))


(reg-event-db
 :property/update-rate
 [(path db/path)]
 (fn [db [_ property-id rate new-rate]]
   (update-in db [:property-rates property-id]
              (partial update-rate (:term rate) new-rate))))


(reg-event-fx
 :property.rates/update!
 [(path db/path)]
 (fn [{db :db} [k property-id]]
   (let [property  (norms/get-norm db :properties/norms property-id)
         new-rates (get-in db [:property-rates property-id])
         to-update (set/difference (set new-rates)
                                   (set (:rates property)))]
     {:dispatch-n [[:ui/loading k true]
                   [::update-property-rate! k property-id to-update]]})))


(reg-event-fx
 ::update-property-rate!
 [(path db/path)]
 (fn [{db :db} [_ k property-id rates]]
   (let [[rate & rates] rates
         on-success     (if-not (empty? rates)
                          [::update-property-rate! k property-id rates]
                          [::set-rate-success k property-id])]
     {:graphql {:mutation
                [[:property_set_rate {:id   property-id
                                      :term (:term rate)
                                      :rate (float (:rate rate))}
                  [:id]]]
                :on-success on-success
                :on-failure [:graphql/failure k]}})))


(reg-event-fx
 :property.tours/toggle!
 [(path db/path)]
 (fn [_ [k property-id]]
   {:dispatch [:ui/loading k true]
    :graphql {:mutation
              [[:property_toggle_touring {:id property-id}
                [:id :tours]]]
              :on-success [::toggle-tours-success k]
              :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::toggle-tours-success
 [(path db/path)]
 (fn [{db :db} [_ k {{{:keys [id tours]} :property_toggle_touring} :data}]]
   (let [property (norms/get-norm db :properties/norms id)]
     {:db       (norms/assoc-norm db :properties/norms id (assoc property :tours tours))
      :dispatch [:ui/loading k false]})))


;; units ========================================================================


(defmethod routes/dispatches :properties.entry.units/entry [{:keys [params]}]
  [[::set-unit-rates
    (tb/str->int (:property-id params))
    (tb/str->int (:unit-id params))]])


(reg-event-db
 ::set-unit-rates
 [(path db/path)]
 (fn [db [_ property-id unit-id]]
   (let [unit (db/unit db property-id unit-id)]
     (assoc-in db [:unit-rates unit-id] (db/unit-rates unit)))))


(reg-event-db
 :property.unit/update-rate
 [(path db/path)]
 (fn [db [_ unit-id rate new-rate]]
   (update-in db [:unit-rates unit-id] (partial update-rate (:term rate) new-rate))))


(reg-event-fx
 :property.unit.rates/update!
 [(path db/path)]
 (fn [{db :db} [k property-id unit-id]]
   (let [unit      (db/unit db property-id unit-id)
         new-rates (get-in db [:unit-rates unit-id])
         to-update (set/difference (set new-rates)
                                   (set (db/unit-rates unit)))]
     {:dispatch-n [[:ui/loading k true]
                   [::update-unit-rate! k property-id unit-id to-update]]})))


(reg-event-fx
 ::update-unit-rate!
 [(path db/path)]
 (fn [{db :db} [_ k property-id unit-id rates]]
   (let [[rate & rates] rates
         on-success     (if-not (empty? rates)
                          [::update-unit-rate! k property-id unit-id rates]
                          [::set-rate-success k property-id])]
     {:graphql {:mutation
                [[:unit_set_rate {:id   unit-id
                                  :term (:term rate)
                                  :rate (float (:rate rate))}
                  [:id]]]
                :on-success on-success
                :on-failure [:graphql/failure k]}})))


(reg-event-fx
 ::set-rate-success
 [(path db/path)]
 (fn [{db :db} [_ k property-id response]]
   ;; TODO: This could be more efficient
   {:dispatch-n [[:ui/loading k false]
                 [:property/fetch property-id]]}))
