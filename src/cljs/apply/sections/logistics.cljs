(ns apply.sections.logistics
  (:require [apply.content :as content]))


(defmethod content/view :logistics/move-in-date
  [_]
  [:div "This is the move-in-date view."])
