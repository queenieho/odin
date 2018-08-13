(ns onboarding.sections.member-agreement.logistics
  (:require [onboarding.content :as content]
            [re-frame.core :refer [dispatch
                                   subscribe
                                   reg-event-fx
                                   reg-sub]]
            [onboarding.events :as events]
            [onboarding.db :as db]))


(def step :member-agreement/logistics)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :next/step)


(defmethod db/previous-step step
  [db]
  :previous/step)


(defmethod db/has-back-button? step
  [_]
  false)


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
    [:h1 "Step Header"]
    [:p "Step description"]]
   [:div.page-content.w-90-l.w-100
    [:p "Step content"]]])
