(ns apply.sections.logistics.choose-date
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-sub]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.utils.log :as log]
            [iface.utils.time :as time]
            [toolbelt.core :as tb]))


(def step :logistics.move-in-date/choose-date)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  (if (>= (time/days-between (step db)) 45)
    :logistics.move-in-date/outside-application-window
    :logistics/occupancy))


(defmethod db/previous-step step
  [db]
  :logistics/move-in-date)


(defmethod db/step-complete? step
  [db step]
  (some? (step db)))


;; subs =========================================================================


(reg-sub
 :step/data
 (fn [db _]
   (-> (step db))))


;; events =======================================================================


(defmethod events/save-step-fx step
  [db params]
  {:dispatch [::update-application (.toISOString params)]})


(reg-event-fx
 ::update-application
 (fn [{db :db} [_ date]]
   (let [application-id (:application-id db)]
     (log/log "updating application..." application-id date)
     {:graphql {:mutation [[:application_update {:application application-id
                                                 :params      {:move_in date}}
                            [:id :move_in]]]
                :on-success [::update-application-success]
                :on-failure [:graphql/failure]}})))


(reg-event-fx
 ::update-application-success
 (fn [{db :db} [_ response]]
   (let [move-in (get-in response [:data :application_update :move_in])]
     {:db       (assoc db step move-in)
      :dispatch [:step/advance]})))

;; views ========================================================================


(defmethod content/view step
  [_]
  (let [data (subscribe [:step/data])]
    (log/log @data)
    [:div
     [:div.w-60-l.w-100
      [:h1 "Let's get started." [:br] "When do you want to move-in?"]
      [:p "We'll do our best to accommodate your move-in date, but we cannot
    guarantee that the date you choose will be the date that you move in."]]
     [:div.page-content.w-90-l.w-100
      [ant/date-picker
       {:on-change #(dispatch [:step.current/next %])
        :value (if (some? @data)
                 (js/moment @data)
                 (js/moment))}]]]))
