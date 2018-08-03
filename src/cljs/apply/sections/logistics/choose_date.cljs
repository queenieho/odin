(ns apply.sections.logistics.choose-date
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-sub]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.utils.log :as log]
            [iface.utils.time :as time]
            [toolbelt.core :as tb]
            [iface.components.ptm.ui.form :as form]))


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
  (let [date (or params (step db))]
   {:dispatch [:application/update {:move_in (.toISOString date)}]}))


(defmethod events/gql->rfdb :move_in [k] step)


;; views ========================================================================


(defmethod content/view step
  [_]
  (let [data (subscribe [:step/data])]
    (log/log @data)
    [:div
     [:div.w-60-l.w-100
      [:h1 "First things first:" [:br] "When do you want to move in?"]
      [:p "We can't guarantee that the day you pick will be when you move in,
but we'll do our best to make it work."]]
     [:div.page-content.w-90-l.w-100
      [form/inline-date {:value        @data
                         :on-day-click #(when-not (.. %2 -disabled)
                                          (dispatch [:step.current/next %]))
                         :show-from    (js/Date.)
                         :disabled     {:before (js/Date.)}}]]]))
