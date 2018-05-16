(ns admin.notes.subs
  (:require [re-frame.core :refer [reg-sub]]
            [toolbelt.core :as tb]))


(reg-sub
 ::notes
 (fn [db _]
   (db)))


(reg-sub
 :note/showing?
 :<- [::notes]
 (fn [notes _]
   notes))
