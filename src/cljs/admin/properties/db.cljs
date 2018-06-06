(ns admin.properties.db
  (:require [toolbelt.core :as tb]
            [iface.utils.norms :as norms]))


(def path ::properties)


(def financial-form [{:title "Business Information"
                      :fields   [{:label       "Business name"
                                  :placeholder "ex. 00 Name LLC"
                                  :type        :text
                                  :keys        [:business-name]}
                                 {:label       "Tax id"
                                  :placeholder "tax id"
                                  :type        :text
                                  :keys        [:tax-id]}]}
                     {:title  "Account Holder Information"
                      :fields [{:label       "First Name"
                                :placeholder "first name"
                                :type        :text
                                :keys        [:first-name]}
                               {:label       "Last Name"
                                :placeholder "last name"
                                :type        :text
                                :keys        [:last-name]}
                               {:label       "Last 4 SSN"
                                :placeholder "0000"
                                :type        :text
                                :keys        [:ssn]}
                               {:label "DOB"
                                :type  :date
                                :keys  [:dob]}]}
                     {:title  "Deposit Account Information"
                      :fields [{:label       "Account number"
                                :placeholder "000000000000"
                                :type        :text
                                :keys        [:deposit :account-number]}
                               {:label       "Routing number"
                                :placeholder "000000000"
                                :type        :text
                                :keys        [:deposit :routing-number]}]}
                     {:title  "Ops Account Information"
                      :fields [{:label       "Account number"
                                :placeholder "000000000000"
                                :type        :text
                                :keys        [:ops :account-number]}
                               {:label       "Routing number"
                                :placeholder "000000000"
                                :type        :text
                                :keys        [:ops :routing-number]}]}])


(def default-value
  {path {:unit-rates     {}
         :property-rates {}
         :financial-form financial-form}})


(defn unit-rates [unit]
  (let [urates (:rates unit)
        prates (-> unit :property :rates)]
    (map
     (fn [{:keys [term rate] :as prate}]
       (if-let [urate (tb/find-by (comp #{term} :term) urates)]
        urate
        prate))
    prates)))


(defn unit [db property-id unit-id]
  (let [units (:units (norms/get-norm db :properties/norms property-id))]
    (tb/find-by (comp #{unit-id} :id) units)))
