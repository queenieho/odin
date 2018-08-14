(ns onboarding.sections.helping-hands
  (:require [onboarding.content :as content]
            [onboarding.sections.helping-hands.bundles]
            [onboarding.sections.helping-hands.byomf]
            [onboarding.sections.helping-hands.dog-info]
            [onboarding.sections.helping-hands.dog-walking]
            [onboarding.sections.helping-hands.packages]
            [onboarding.sections.helping-hands.request-furniture]
            [onboarding.sections.helping-hands.storage]
            [onboarding.db :as db]))


(defmethod db/section-complete? :helping-hands
  [db section]
  ;; TODO implement
  false)
