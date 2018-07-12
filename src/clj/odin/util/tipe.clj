(ns odin.util.tipe
  (:require [cheshire.core :as json]
            [org.httpkit.client :as http]
            [odin.util.validation :as validation]
            [taoensso.timbre :as timbre]))

(def base-uri
  "The base URI of Tipe."
  "http://api.tipe.io/api/v1/")


(defn wrap-validate [validate-document]
  (fn [document]
    (let [vresult (validate-document document)]
      (when-not (validation/valid? vresult)
        {:errors (first vresult)}))))


(defn- headers
  [tipe]
  {"Content-Type"  "application/json"
   "Authorization" (:api-key tipe)
   "Tipe-Id"       (:secret-key tipe)})


(defn- payload
  [response]
  (json/parse-string (:body response) true))


(defn- log-invalid-docs [validate docs]
  (let [validated (map validate docs)]
    (doseq [result (filter (comp some? :errors) validated)]
      (timbre/error ::invalid-document (vec (:errors result))))))


(defn parse-document
  [{blocks :blocks :as document}]
  (when (seq blocks)
    (merge
     document
     (reduce (fn [acc {:keys [apiId value]}]
               (assoc acc (keyword apiId) value))
             {}
             blocks))))


(defn- parse-documents
  [tipe documents]
  (let [documents (if (:include-unpublished? tipe)
                    documents
                    (filter :published documents))]
    (mapv parse-document documents)))


(defn fetch-folder
  "Synchronously fetch an entire Tipe folder by its `folder-id`."
  [tipe folder-id & {:keys [validate]
                     :or   {validate (constantly nil)}}]
  (let [docs (->> @(http/get (str base-uri "folder/" folder-id)
                             {:headers (headers tipe)})
                  payload
                  :documents
                  (parse-documents tipe))]
    (log-invalid-docs validate docs)
    (filter (comp nil? validate) docs)))


(defn fetch-document
  "Synchronously fetch a single Tipe document by its `document-id`."
  [tipe document-id & {:keys [validate]
                       :or   {validate (constantly nil)}}]
  (let [doc (-> @(http/get (str base-uri "document/" document-id)
                           {:headers (headers tipe)})
                payload
                parse-document)]
    (log-invalid-docs validate (vector doc))
    (nil? (validate doc))))


(defn tipe
  "Construct a connection to Tipe given an `api-key` and a `secret-key`."
  ([api-key secret-key]
   (tipe api-key secret-key {}))
  ([api-key secret-key {:keys [include-unpublished?]
                        :or   {include-unpublished? true}}]
   {:api-key              api-key
    :secret-key           secret-key
    :include-unpublished? include-unpublished?}))
