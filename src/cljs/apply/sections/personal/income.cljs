(ns apply.sections.personal.income
  (:require [ajax.core :as ajax]
            [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-sub]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.log :as log]
            [iface.utils.file :as ufile]
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
  {:dispatch [::upload-income-verification!]})


(defmethod events/gql->rfdb :income [k v] step)


;; upload file ==========================


(reg-event-fx
 ::upload-income-verification!
 (fn [{db :db} _]
   (let [application-id (:application-id db)]
     {:http-xhrio {:uri             (str "/api/applications/" application-id "/verify-income")
                   :body            (get-in db [:income-files :files])
                   :method          :post
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::upload-income-verification-success]
                   :on-failure      [::upload-income-verification-failure]}})))


(reg-event-fx
 ::upload-income-verification-success
 (fn [{db :db} [k]]
   {:graphql {:query [[:account {:id (get-in db [:account :id])}
                       [[:application events/application-attrs]]]]
              :on-success [::income-verification-success]
              :on-failure [:graphql/failure k]}}))


(reg-event-fx
 ::upload-income-verification-failure
 (fn [{db :db} [_ error]]
   (timbre/error error)
   (ant/notification-error {:message "Failed to upload your income verification files."})))


(reg-event-fx
 ::income-verification-success
 (fn [{db :db} [_ response]]
   {:db       (-> (assoc db step (get-in response [:data :account :application :income]))
                  (dissoc :income-files))
    :dispatch [:step/advance]}))


;; file selected ========================


(reg-event-fx
 ::income-verification-selected
 (fn [{db :db} [_ files count]]
   {:db (-> (assoc-in db [:income-files :files] (ufile/files->form-data files))
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


;; NOTE what do we want to show in this step if there's already uploaded images?
;; also, do we want to delete files from the application?
;; I think this works well as it is right now
;; but for a second pass at the application we should think about this
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
