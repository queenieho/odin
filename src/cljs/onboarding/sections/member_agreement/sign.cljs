(ns onboarding.sections.member-agreement.sign
  (:require [onboarding.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [onboarding.events :as events]
            [onboarding.db :as db]
            [iface.components.ptm.layout :as layout]
            [iface.components.ptm.ui.button :as button]))


(def step :member-agreement/sign)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  :member-agreement/thanks)


(defmethod db/previous-step step
  [db]
  :member-agreement/logistics)


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
   [layout/header {:title   "Please sign your membership agreement."
                   :subtext "Review the terms and details of your membership
                   agreement and sign and date it at the end."}]
   [:div.page-content.w-90-l.w-100
    [button/primary
     {:on-click #(dispatch [:step/advance])}
     "View & sign agreement"]]])
