(ns apply.sections.logistics
  (:require [apply.content :as content]
            [apply.sections.logistics.move-in-date]
            [apply.sections.logistics.choose-date]
            [apply.sections.logistics.outside-application-window]
            [apply.sections.logistics.get-notified]
            [apply.sections.logistics.occupancy]
            [apply.sections.logistics.co-occupant]
            [apply.db :as db]))


#_(defmethod db/section-complete? :logistics
    [db section]
    false)
