(ns onboarding.sections.security-deposit
  (:require [onboarding.content :as content]
            [onboarding.sections.security-deposit.payment-method]
            [onboarding.db :as db]))


(defmethod db/section-complete? :deposit
  [db section]
  ;; TODO implement
  false)
