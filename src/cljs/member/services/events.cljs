(ns member.services.events
  (:require [akiroz.re-frame.storage :refer [reg-co-fx!]]
            [member.routes :as routes]
            [member.services.db :as db]
            [re-frame.core :refer [reg-event-fx reg-event-db path inject-cofx]]
            [toolbelt.core :as tb]))


(reg-co-fx! db/path
            {:fx   :cart
             :cofx :cart})


(reg-event-fx
 ::load-cart
 [(inject-cofx :cart) (path db/path)]
 (fn [{:keys [cart db]} _]
   {:db (assoc db :cart (or cart []))}))


(reg-event-fx
 ::save-cart
 [(path db/path)]
 (fn [{:keys [db]} [_ new-cart]]
   {:db   (assoc db :cart new-cart)
    :cart new-cart}))


(reg-event-fx
 ::clear-cart
 (fn [_ _]
   {:dispatch [::save-cart []]}))


(defmethod routes/dispatches :services/book
  [{:keys [params page] :as route}]
  (if (empty? params)
    [[:services/set-default-route route]]
    [[:services.catalogs/fetch (db/parse-query-params page params)]
     [::load-cart]]))


(defmethod routes/dispatches :services/manage
  [_]
  [[::load-cart]])


(defmethod routes/dispatches :services/cart
  [{:keys [requester] :as route}]
  [[:payment-sources/fetch (:id requester)]
   [::load-cart]])


(reg-event-fx
 :services/set-default-route
 [(path db/path)]
 (fn [{db :db} [_ {page :page}]]
   {:route (db/params->route page (:params db))}))


(reg-event-fx
 :services.catalogs/fetch
 [(path db/path)]
 (fn [{db :db} [k query-params]]
   {:db       (assoc db :params query-params)
    :dispatch [:ui/loading k true]
    :graphql  {:query
               [[:catalogs
                 [:id :name :code
                  [:properties [:id :name]]
                  [:items [:id]]]]]
               :on-success [::fetch-catalogs-success k]
               :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::fetch-catalogs-success
 [(path db/path)]
 (fn [{db :db} [_ k response]]
   (.log js/console response)
   {:dispatch [:ui/loading k false]}))


(reg-event-fx
 :services.section/select
 [(path db/path)]
 (fn [_ [_ section]]
   (let [page (keyword (str "services/" section))]
     {:route (routes/path-for page)})))


(reg-event-db
 :services.add-service.form/update
 [(path db/path)]
 (fn [db [_ key value]]
   (update db :form-data
           (fn [fields]
             (map
              (fn [field]
                (if (= (:key field) key)
                  (assoc field :value value)
                  field))
              fields)))))


(reg-event-db
 :services.add-service.form/reset
 [(path db/path)]
 (fn [db _]
   (dissoc db :form-data)))


(reg-event-fx
 :services.add-service/show
 [(path db/path)]
 (fn [{db :db} [_ {:keys [service fields]}]]
   {:dispatch [:modal/show db/modal]
    :db       (assoc db :adding service :form-data fields)}))


(reg-event-fx
 :services.add-service/close
 [(path db/path)]
 (fn [{db :db} _]
   {:dispatch-n [[:services.add-service.form/reset]
                 [:modal/hide db/modal]]}))


(reg-event-fx
 :services.add-service/add
 [(path db/path) ]
 (fn [{db :db} _]
   (let [{:keys [id title description price]} (:adding db)
         adding                               {:id          (db/rand-id) ;; gen unique id so we can remove items by item-id
                                               :service-id  id           ;; not sure if needed, but its here
                                               :title       title
                                               :description description
                                               :price       price
                                               :fields      (:form-data db)}
         new-cart                             (conj (:cart db) adding)]
     {:dispatch-n [[:services.add-service/close]
                   [::save-cart new-cart]]})))


(reg-event-fx
 :services.cart/submit
 [(path db/path)]
 (fn [{db :db} _]
   {:dispatch [::clear-cart]}))


;; this removes all of the services with the same service id... we need to only remove the selected item
(reg-event-fx
 :services.cart.item/remove
 [(path db/path)]
 (fn [{db :db} [_ id]]
   (let [new-cart (remove #(= id (:id %)) (:cart db))]
     {:dispatch [::save-cart new-cart]})))


;; this is the same as services.add-service/show, should probably rename that to make it generic
(reg-event-fx
 :services.cart.item/edit
 [(path db/path)]
 (fn [{db :db} [_ service fields]]
   {:dispatch [:modal/show db/modal]
    :db       (assoc db :adding service :form-data fields)}))


(reg-event-fx
 :services.cart.item/save-edit
 [(path db/path)]
 (fn [{db :db} _]
   (let [new-fields (:form-data db)
         new-cart   (map
                     (fn [item]
                       (if (= (:id item) (:id (:adding db)))
                         (assoc item :fields new-fields)
                         item))
                     (:cart db))]
     {:dispatch-n [[:services.add-service/close]
                   [::save-cart new-cart]]})))



;; =============================================================================
;; Add Card
;; =============================================================================

;; Cards have no `submit` event, as this is handled by the Stripe JS API.
;; We skip immediately to `success`, where we've
;; received a token for the new card from Stripe.

(reg-event-fx
 :services.cart.add.card/save-stripe-token!
 (fn [_ [k token]]
   {:dispatch [:ui/loading k true]
    :graphql  {:mutation   [[:add_payment_source {:token token} [:id]]]
               :on-success [::services-create-card-source-success k]
               :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::services-create-card-source-success
 (fn [{:keys [db]} [_ k response]]
   {:dispatch-n [[:ui/loading k false]
                 [:modal/hide :payment.source/add]]
    :route      (routes/path-for :services/cart)}))
