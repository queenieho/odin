(ns odin.graphql.resolvers.utils.autopay
  (:require [teller.customer :as tcustomer]
            [teller.property :as tproperty]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [toolbelt.date :as date]))


(defn autopay-start
  [customer]
  (let [property (tcustomer/property customer)
        tz       (t/time-zone-for-id (tproperty/timezone property))]
    (-> (c/to-date (t/plus (t/now) (t/months 1)))
        (date/beginning-of-month tz))))
