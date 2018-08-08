(ns apply.sections.personal
  (:require [apply.db :as db]
            [apply.sections.personal.phone-number]
            [apply.sections.personal.background-check]
            [apply.sections.personal.background-check-declined]
            [apply.sections.personal.background-check-info]
            [apply.sections.personal.income]
            [apply.sections.personal.income-cosigner]
            [apply.sections.personal.about]
            [devtools.defaults :as d]
            [iface.utils.log :as log]
            [re-frame.core :refer [subscribe]]))


(defmethod db/section-complete? :personal
  [db section]
  (let [params (subscribe [:route/params])]
    (and (db/step-complete? db :personal/phone-number)
         (db/step-complete? db :personal.background-check/info)
         (db/step-complete? db :personal/income)
         (db/step-complete? db :personal/about)
         (or (= "payment" (:section-id @params))
             (= :submitted (:application-status db))))))
