(ns onboarding.sections.member-agreement
  (:require [onboarding.content :as content]
            [onboarding.sections.member-agreement.logistics]
            [onboarding.sections.member-agreement.sign]
            [onboarding.sections.member-agreement.thanks]
            [onboarding.db :as db]))


(defmethod db/section-complete? :member-agreement
  [db section]
  ;; TODO implement
  false)
