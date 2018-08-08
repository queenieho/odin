(ns apply.sections.logistics
  (:require [apply.content :as content]
            [apply.sections.logistics.move-in-date]
            [apply.sections.logistics.choose-date]
            [apply.sections.logistics.outside-application-window]
            [apply.sections.logistics.get-notified]
            [apply.sections.logistics.occupancy]
            [apply.sections.logistics.co-occupant]
            [apply.sections.logistics.pets]
            [apply.sections.logistics.dog]
            [apply.sections.logistics.pets-other]
            [apply.db :as db]))


(defmethod db/section-complete? :logistics
    [db section]
  (let [move-in (:logistics/move-in-date db)]
    (and (or (some #(= % move-in) [:asap :flexible])
             (and (= :date move-in) (some? (:logistics.move-in-date/choose-date db))))

         (some? (:logistics/occupancy db))

         (or (false? (:logistics/pets db))
             (and (true? (:logistics/pets db))
                  (or (db/step-complete? :logistics.pets/dog)
                      (db/step-complete? :logistics.pets/other)))))))
