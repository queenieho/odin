(ns apply.sections.community.term
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]))


(def step :community/term)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :personal/phone-number)


(defmethod db/previous-step step
  [db]
  :community/select)


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


(defmethod content/view step
  [_]
  [:div
   [:div.w-60-l.w-100
    [:h1 "How long will you be staying with us?"]
    [:p "We understand that each individual ahs unique housing needs, which is
    why we offer three membership plans ranging from most affordable to most
    flexible."]]
   [:div.page-content.w-90-l.w-100
    [ant/button
     {:on-click #(dispatch [:step.current/next 12])}
     "12 months"]
    [ant/button
     {:on-click #(dispatch [:step.current/next 6])}
     "6 months"]
    [ant/button
     {:on-click #(dispatch [:step.current/next 3])}
     "3 months"]]])
