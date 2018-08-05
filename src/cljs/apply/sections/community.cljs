(ns apply.sections.community
  (:require [apply.sections.community.select]
            [apply.sections.community.term]
            [apply.db :as db]))


(defmethod db/section-complete? :community
  [db section]
  (and (db/step-complete? db :community/select)
       (db/step-complete? db :community/term)))
