(ns apply.sections.payment
  (:require [apply.db :as db]
            [apply.sections.payment.review]
            [apply.sections.payment.complete]))


(defmethod db/section-complete? :payment
  [db _]
  (= :submitted (:application-status db)))
