(ns odin.routes.api
  (:require [blueprints.models.account :as account]
            [com.walmartlabs.lacinia :refer [execute]]
            [compojure.core :as compojure :refer [defroutes GET POST]]
            [datomic.api :as d]
            [odin.graphql :as graph]
            [odin.graphql.resolvers.utils :as gqlu]
            [ring.util.response :as response]))

;; =============================================================================
;; Helpers
;; =============================================================================


(defn ->conn [req]
  (get-in req [:deps :conn]))


(defn ->db [req]
  (d/db (get-in req [:deps :conn])))


(defn ->stripe [req]
  (get-in req [:deps :stripe]))


(defn- ->requester [req]
  (let [id (get-in req [:identity :db/id])]
    (d/entity (->db req) id)))


;; =============================================================================
;; GraphQL
;; =============================================================================


(defn extract-graphql-expression [request]
  (case (:request-method request)
    :get  [:query (get-in request [:params :query] "")]
    :post [:mutation (get-in request [:params :mutation] "")]))


(defn context [req]
  (gqlu/context
    (->conn req)
    (->requester req)
    (->stripe req)))


(defn graphql-handler [req]
  (let [[op expr] (extract-graphql-expression req)
        result    (execute graph/schema
                           (format "%s %s" (name op) expr)
                           nil
                           (context req))]
    (-> (response/response result)
        (response/content-type "application/transit+json")
        (response/status (if (-> result :errors some?) 400 200)))))


;; =============================================================================
;; Config
;; =============================================================================


(def ^:private admin-config
  {:role :admin
   :features
   {:home        {}
    :profile     {}
    :people      {}
    :metrics     {}
    :communities {}
    :orders      {}
    :services    {}}})


(def ^:private member-config
  {:role :member
   :features
   {:home    {}
    :profile {}}})


(defn make-config [req]
  (let [account (->requester req)]
    (case (account/role account)
      :account.role/admin admin-config
      :account.role/member member-config)))


(defn inject-account [config account]
  (assoc config :account {:id    (:db/id account)
                          :name  (format "%s %s"
                                         (account/first-name account)
                                         (account/last-name account))
                          :email (account/email account)
                          :role  (account/role account)}))


(defn config-handler [req]
  (let [account (->requester req)
        config  (-> (make-config req) (inject-account account))]
    (-> (response/response config)
        (response/content-type "application/transit+json")
        (response/status 200))))


;; =============================================================================
;; Routes
;; =============================================================================


(defroutes routes
  (GET "/config" [] config-handler)

  (GET "/graphql" [] graphql-handler)
  (POST "/graphql" [] graphql-handler))
