(ns odin.aws
  (:require [mount.core :refer [defstate]]
            [odin.config :as config :refer [config]]))


(defstate creds
  :start {:access-key (config/aws-access-key config)
          :secret-key (config/aws-secret-key config)})
