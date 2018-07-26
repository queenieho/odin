(ns apply.sections.personal.income
  (:require [ajax.core :as ajax]
            [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-sub]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.log :as log]
            [taoensso.timbre :as timbre]))


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
  (let [file-count (get-in db [:income-files :count])]
    (not (and (some? file-count) (not (zero? file-count))))))


;; events =======================================================================


(defmethod events/save-step-fx step
  [db params]
  {
   ;; :db       (assoc db step params)
   :dispatch [::upload-income-verification!] #_[:step/advance]})


(reg-event-fx
 ::upload-income-verification!
 (fn [{db :db} _]
   (let [account-id     (get-in db [:account :id])
         application-id (:application-id db)]
     (log/log "next" (get-in db [:income-files :files]))
     {:http-xhrio {:uri             (str "/api/communities/" application-id "/cover-photo") #_(str "/api/applications/" account-id "/" application-id "/income-verification")
                   :body            (get-in db [:income-files :files])
                   :method          :post
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::upload-income-verification-success account-id]
                   :on-failure      [::upload-income-verification-failure]}})))


(reg-event-fx
 ::upload-income-verification-success
 (fn [{db :db} [_ k account-id]]
   (log/log "success!!")
   {:graphql {:query [[:account {:id account-id}
                       [:application events/application-attrs]]]
              :on-success [::income-verification-success]
              :on-failure [::income-verification-failure]}}))


(reg-event-fx
 ::upload-income-verification-failure
 (fn [{db :db} [_ error]]
   (timbre/error error)
   (log/log "upload failed" error)))


(reg-event-fx
 ::income-verification-success
 (fn [{db :db} [_ response]]
   (log/log "we got it!!!" response)
   {:db       (assoc db step response)
    :dispatch [:step/advance]}))


(reg-event-fx
 ::income-verification-failure
 (fn [{db :db} _]
   (log/log "i have no idea what im doing")))


(defn- files->form-data [files]
  (let [form-data (js/FormData.)]
    (doseq [file-key (.keys js/Object files)]
      (let [file (aget files file-key)]
        (.append form-data "files[]" file (.-name file))))
    form-data))


(reg-event-fx
 ::income-verification-selected
 (fn [{db :db} [_ files count]]
   {:db (-> (assoc-in db [:income-files :files] (files->form-data files))
            (assoc-in [:income-files :count] count))}))


;; subs =========================================================================


(reg-sub
 ::income-files
 (fn [db _]
   (:income-files db)))


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
  (let [income-files (subscribe [::income-files])
        count        (:count @income-files)]
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

       [:div
        [:input
         {:type      "file"
          :multiple  true
          :name      "income"
          :id        "income"
          :on-change #(dispatch [::income-verification-selected (.. % -currentTarget -files) (.. % -currentTarget -files -length)])}]
        [:label.button-upload
         {:for "income"}
         (if (and (some? count) (not (zero? count)))
           (str count " files selected")
           "Upload files")]]

       ;; NOTE commented out because we don't have this capability yet
       #_[:p.mt3.mb3 "Are you taking a picture with your phone? Get an SMS link to finish this part of the application on your phone."]

       ;; NOTE commented out until we add cosigner to the flow
       #_[:span {:on-click #(dispatch [:step.current/next :cosigner])}
          [form/checkbox {} "I am applying with a cosigner (i)"]]]]]))
