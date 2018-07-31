(ns apply.sections.community.term
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-sub]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.utils.log :as log]
            [iface.components.ptm.ui.card :as card]
            [iface.utils.formatters :as format]))


(def step :community/term)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  :personal/phone-number)


(defmethod db/next-step step
  [db]
  :personal/phone-number)


(defmethod db/previous-step step
  [db]
  :community/select)


(defmethod db/has-back-button? step
  [_]
  true)


(defmethod db/step-complete? step
  [db step]
  false)


;; events =======================================================================


(defmethod events/save-step-fx step
  [db term]
  {:dispatch [:application/update {:term term}]})


(defmethod events/gql->rfdb :term [_] step)


;; subs =========================================================================


(reg-sub
 ::term-options
 (fn [db _]
   (->> (:license-options db)
        (sort-by :term >))))


;; views ========================================================================


(defmethod content/view step
  [_]
  (let [options (subscribe [::term-options])]
    [:div
     (log/log @options)
     [:div.w-60-l.w-100
      [:h1 "How long would like to stay with us?"]
      [:p "We know everyone has a different situation,
      so we have membership plans that can either be
      more affordable or more flexible."]]
     [:div.w-80-l.w-100
      [:div.page-content
       (map-indexed
        (fn [idx {:keys [id term]}]
          ^{:key id}
          [card/single-h1
           {:title    (str term)
            :subtitle "months"
            ;; NOTE not completely sure if we want to do this in this fashion
            ;; but it was the in my head what makes sense at the moment
            :footer   (if (zero? idx)
                        "No extra cost"
                        (str "+ " (format/currency (* idx 50)) " per month"))
            :on-click #(dispatch [:step.current/next id])}])
        @options)]]]))
