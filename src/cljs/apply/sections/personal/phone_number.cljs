(ns apply.sections.personal.phone-number
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch
                                   subscribe
                                   reg-event-fx]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.log :as log]))


(def step :personal/phone-number)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :personal/background-check)


(defmethod db/previous-step step
  [db]
  :community/term)


(defmethod db/has-back-button? step
  [_]
  true)


(defmethod db/step-complete? step
  [db step]
  false)


;; events =======================================================================


(defmethod events/save-step-fx step
  [db _]
  {:graphql {:mutation   [[:update_account {:id   (get-in db [:account :id])
                                            :data {:phone (step db)}}
                           [:phone]]]
             :on-success [::save-phone-success]
             :on-failure [:graphql/failure]}})


(reg-event-fx
 ::save-phone-success
 (fn [{db :db} [_ response]]
   {:db       (assoc db step (get-in response [:data :update_account :phone]))
    :dispatch [:step/advance]}))


(reg-event-fx
 ::update-phone
 (fn [{db :db} [_ phone]]
   {:db (assoc db step phone)}))


;; views ========================================================================


(defmethod content/view step
  [_]
  (let [phone (subscribe [:db/step step])]
    [:div
     [:div.w-60-l.w-100
      [:h1 "What's your phone number?"]
      [:p "We promise we'll keep your phone number private and only contact you
    in case we need to get in touch about something important."]]
     [:div.page-content.w-60-l.w-100
      [:div.w-30-l.w-100
       [form/item
        {:label "Phone Number"}
        [form/text
         {:value     @phone
          :on-change #(dispatch [::update-phone (.. % -target -value)])}]]]]]))
