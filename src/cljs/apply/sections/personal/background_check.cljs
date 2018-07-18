(ns apply.sections.personal.background-check
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.card :as card]
            [iface.utils.log :as log]
            [iface.components.ptm.ui.button :as button]
            [reagent.core :as r]))


(def step :personal/background-check)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  (if (= :no (s db))
    :personal.background-check/declined
    :personal.background-check/info))


(defmethod db/next-step step
  [db]
  (if (= :no (step db))
    :personal.background-check/declined
    :personal.background-check/info))


(defmethod db/previous-step step
  [db]
  :personal/phone-number)


(defmethod db/has-back-button? step
  [_]
  true)


(defmethod db/step-complete? step
  [db step]
  false)


;; events =======================================================================


(defmethod events/save-step-fx step
  [db params]
  {:db       (assoc db step params)
   :dispatch [:step/advance]})


;; views ========================================================================


(defn decline-background-check-modal
  [{:keys [visible]}]
  (when @visible
    [:div
     [:div.lightbox-small
      [:div.lightbox-content
       [:h3 "We require background checks for all applicants."]
       [:p "It helps keep our application process, and communities safe. Are you sure you want to decline?"]]
      [:div.lightbox-footer
       [:div.lightbox-footer-left
        [button/text
         {:on-click #(swap! visible not)}
         "Cancel"]]
       [:div.lightbox-footer-right
        [button/primary
         {:on-click #(swap! visible not)}
         "Yes, decline"]]]]
     [:div.scrim
      {:on-click #(swap! visible not)}]]))


(defmethod content/view step
  [_]
  (let [is-showing (r/atom false)]
    [:div
     [:div.w-60-l.w-100
      [:h1 "Do we have your consent to perform a background check?"]
      [:p "We perform background checks to ensure the safety of our community
    members. Your background check is completely confidential, and we'll share
    the results (if any) with you."]]

     [decline-background-check-modal {:visible  is-showing
                                      :on-close #(swap! is-showing not)}]

     [:div.w-80-l.w-100
      [:div.page-content
       [card/single
        {:title    "Yes"
         :on-click #(dispatch [:step.current/next :yes])}]
       [card/single
        {:title    "No"
         :on-click #(swap! is-showing not) #_#(dispatch [:step.current/next :no])}]]]]))
