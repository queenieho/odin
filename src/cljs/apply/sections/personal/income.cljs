(ns apply.sections.personal.income
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.log :as log]))


(def step :personal/income)


;; db ===========================================================================


(defmethod db/next-step step
  [db s]
  (if (= :cosigner (db s))
    :personal.income/cosigner
    :personal/about))


(defmethod db/next-step step
  [db]
  (if (= :cosigner (db step))
    :personal.income/cosigner
    :personal/about))


(defmethod db/previous-step step
  [db]
  :personal.background-check/info)


(defmethod db/has-back-button? step
  [_]
  true)


(defmethod db/step-complete? step
  [db step]
  false)


;; events =======================================================================


(defmethod events/save-step-fx step
  [db params]
  {:db       (assoc db step params)
   :dispatch [:step/advance]})


(defn- files->form-data [files]
  (let [form-data (js/FormData.)]
    (doseq [file-key (.keys js/Object files)]
      (let [file (aget files file-key)]
        (.append form-data "files[]" file (.-name file))))
    form-data))


(reg-event-fx
 ::income-verification-selected
 (fn [{db :db} [_ files]]
   (assoc db :income-files (files->form-data files))))


;; views ========================================================================


(defn bullet-item
  [valid label]
  (let [icon-props {:type  (if valid "check" "close")
                    :class (if valid "text-green pr2" "text-red pr2")
                    :style {:font-size "2rem"}}]
    [:div.pv2
     {:display "table"}
     [:span {:style {:display        "table-cell"
                     :vertical-align "middle"}}
      [ant/icon icon-props]]
     [:span {:style {:display        "table-cell"
                     :vertical-align "middle"}}
      [:p label]]]))


(defmethod content/view step
  [_]
  [:div
   [:div.w-60-l.w-100
    [:h1 "Please verify your income."]
    [:p "To qualify to live in Starcity, your gross income must be at least 2.5x the cost of rent. Please submit acceptable forms of verification."]]
   [:div.w-60-l.w-100
    [:div.page-content
     [:div.w-50-l.w-100.fl.pl4-l.pl2
      [bullet-item true "Most recent pay stub"]
      [bullet-item true "Last three months' bank statements"]
      [bullet-item true "Offer letter"]]
     [:div.w-50-l.w-100.fl.pr4-l.pl2
      [bullet-item false "Stock portfolio"]
      [bullet-item false "Photo of your crypto wallet"]
      [bullet-item false "Photo of your actual wallet"]]

     [:input
      {:type     "file"
       :multiple true
       :name     "income"
       :id       "income"
       :on-change #(log/log (.. % -currentTarget -files)
                            (.. % -currentTarget -files -length))}]
     [:label.button-upload
      {:for "income"}
      "Upload files"]

     #_[:p.mt3.mb3 "Are you taking a picture with your phone? Get an SMS link to finish this part of the application on your phone."]

     #_[:span {:on-click #(dispatch [:step.current/next :cosigner])}
        [form/checkbox {} "I am applying with a cosigner (i)"]]]]])
