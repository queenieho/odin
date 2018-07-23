(ns member.profile.payments.sources.db)


(def path ::sources)


(def add-path ::add-source)


(def add-payout ::payout-account)


(def default-key-value
  nil)

(def char-limit
  {:routing-number 9
   :account-number 12
   :ssn 9
   :state 2
   :country 2
   :postal-code 5})


(def default-value
  {path       {:sources []
               :current nil
               :autopay {:on false :source nil}
               :loading {:list false}}
   add-path   {:type            :bank
               :card            {}
               :bank            {}
               :microdeposits   {:amount-1 nil :amount-2 nil}
               :available-types [:bank :card]}
   add-payout {:form            {:line1 default-key-value
                                 :line2 default-key-value
                                 :city default-key-value
                                 :state default-key-value
                                 :postal-code default-key-value
                                 :country default-key-value

                                 :dob default-key-value
                                 :ssn default-key-value

                                 :routing-number default-key-value
                                 :account-number default-key-value}
               :form-validation {}}})
