(ns migrations.income-files
  (:require [clojure.java.io :as io]
            [datomic.api :as d]
            [amazonica.aws.s3 :as s3]))


(defn income-files [db]
  (d/q '[:find ?i ?a ?app ?p
         :where
         [?i :income-file/path ?p]
         [?i :income-file/account ?a]
         [?a :account/application ?app]]
       db))


(defn- filename [path]
  (last (clojure.string/split path #"/")))


(defn- s3-asset-url [bucket-name creds key]
  (format "https://s3-%s.amazonaws.com/%s/%s" (:endpoint creds) bucket-name key))


(defn upload-to-aws [file-id path application-id]
  (let [bucket-name "starcity-income-verification"
        key         (str "applications/" application-id "/" (filename path))]
    (s3/put-object odin.aws/creds
                   :bucket-name bucket-name
                   :key key
                   :file (io/file path))
    [{:db/id     file-id
      :file/uri  (s3-asset-url bucket-name odin.aws/creds key)
      :file/name (filename path)}
     [:db/add application-id :application/income file-id]]))


(comment

  ;; 1. get existing income files
  ;; 2. read each one into memory
  ;; 3. upload to AWS
  ;; 4. update income-file entity path and set account's
  ;;    application/income-files to reference entity
  ;;    4b) retract account ref from income-file


  (do
    (require '[clojure.java.io :as io])
    (require '[datomic.api :as d])
    (require '[amazonica.aws.s3 :as s3])
    (require '[odin.datomic :refer [conn]]))



  (doseq [[file-id account-id app-id path] (income-files (d/db conn))]
    (let [tx-data (upload-to-aws file-id path app-id)]
      ;; (println (conj tx-data [:db/retract file-id :income-file/account account-id]))
      @(d/transact conn (conj tx-data [:db/retract file-id :income-file/account account-id]))))


  )
