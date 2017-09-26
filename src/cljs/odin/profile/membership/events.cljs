(ns odin.profile.membership.events
  (:require [odin.profile.membership.db :as db]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [odin.routes :as routes]
            [toolbelt.core :as tb]))


;; =============================================================================
;; Routing
;; =============================================================================


(defmethod routes/dispatches :profile/membership [route]
  (let [account-id (get-in route [:requester :id])]
    [[:profile/fetch-account account-id]
     [:member.license/fetch account-id]]))


;; =============================================================================
;; Fetch License
;; =============================================================================


;; retrieves the `active_license` for a member.
(reg-event-fx
 :member.license/fetch
 (fn [_ [k account-id]]
   {:dispatch [:loading k true]
    :graphql  {:query      [[:account {:id account-id}
                             [[:deposit [:id :due :amount :amount_remaining :amount_paid :amount_pending]]
                              [:active_license
                               [:id :rate :starts :ends :status :term
                                [:unit [:id :number]]
                                [:property [:id :name :code :cover_image_url]]
                                [:payments [:id :description :type :amount :status
                                            :due :paid_on :pstart :pend]]]]]]]
               :on-success [:member.fetch.license/success k]
               :on-failure [:graphql/failure k]}}))


(reg-event-fx
 :member.fetch.license/success
 [(path db/path)]
 (fn [{:keys [db]} [_ k response]]
   (let [{:keys [active_license deposit]} (get-in response [:data :account])]
     {:db       (assoc db :license active_license :deposit deposit)
      :dispatch [:loading k false]})))


;; =============================================================================
;; Pay Rent Payment
;; =============================================================================


(reg-event-fx
 :member/pay-rent-payment!
 (fn [_ [k payment-id source-id]]
   {:dispatch [:loading k true]
    :graphql  {:mutation   [[:pay_rent_payment {:id     payment-id
                                                :source source-id}
                             [:id]]]
               :on-success [::make-payment-success k payment-id]
               :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::make-payment-success
 (fn [{db :db} [_ k modal-id response]]
   (let [account-id (get-in db [:config :account :id])]
     {:dispatch-n [[:loading k false]
                   [:modal/hide modal-id]
                   [:member.license/fetch account-id]]})))


;; =============================================================================
;; Pay Remainder of Deposit
;; =============================================================================


(reg-event-fx
 :member/pay-deposit!
 (fn [_ [k deposit-id source-id]]
   {:dispatch [:loading k true]
    :graphql  {:mutation   [[:pay_remainder_deposit {:source source-id} [:id]]]
               :on-success [::make-payment-success k deposit-id]
               :on-failure [:graphql/failure k]}}))
