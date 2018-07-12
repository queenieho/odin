(ns odin.tipe
  (:require [odin.config :as config :refer [config]]
            [odin.util.tipe :as tipe]
            [mount.core :refer [defstate]]
            [taoensso.timbre :as timbre]))


(defstate tipe
  :start (tipe/tipe (config/tipe-api-key config)
                    (config/tipe-secret config)
                    {:include-unpublished? (not (config/production? config))}))
