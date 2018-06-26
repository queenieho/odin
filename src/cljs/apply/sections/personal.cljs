(ns apply.sections.personal
  (:require [apply.db :as db]
            [apply.sections.personal.phone-number]
            [apply.sections.personal.background-check]
            [apply.sections.personal.background-check-declined]
            [apply.sections.personal.background-check-info]
            [apply.sections.personal.income]
            [apply.sections.personal.income-cosigner]
            [apply.sections.personal.about]))

(defmethod db/section-complete? :personal [_ _] false)
