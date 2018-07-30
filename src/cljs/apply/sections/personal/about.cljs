(ns apply.sections.personal.about
  (:require [apply.content :as content]
            [apply.db :as db]
            [apply.events :as events]
            [antizer.reagent :as ant]
            [clojure.string :as s]
            [re-frame.core :refer [dispatch subscribe reg-event-fx]]
            [iface.components.ptm.ui.form :as form]
            [iface.utils.log :as log]))


(def step :personal/about)


;; db ===========================================================================


(defmethod db/get-last-saved step
  [db s]
  :payment/review)


(defmethod db/next-step step
  [db]
  :payment/review)


(defmethod db/previous-step step
  [db]
  (if (some? (:personal.income/cosigner db))
    :personal.income/cosigner
    :personal/income))


(defmethod db/has-back-button? step
  [_]
  true)


(defmethod db/step-complete? step
  [db step]
  (not (and (some? (step db)) (not (s/blank? (step db))))))


;; events =======================================================================


(reg-event-fx
 ::update-personal-about
 (fn [{db :db} [_ v]]
   {:db (assoc db step v)}))


(defmethod events/save-step-fx step
  [db params]
  {:dispatch [:application/update {:about (step db)}]})


(defmethod events/gql->rfdb :about [k v] step)


;; views ========================================================================


(defmethod content/view step
  [_]
  (let [about (subscribe [:db/step step])]
    [:div
     [:div.w-75-l.w-100
      [:h1 "Here's a little about us."]
      [:p.mb4 "We believe that community is best created by the connections formed
    between individuals through shared values. Read more about Our Values on our
    website."]
      [:p.mb4 "Starcity is a safe space for individuals to come together and share
    their skills, experiences and perspectives. Our members give to one another
    and to the greater communities in which they live. We hope you're as excited
    about sharing and giving back as we are."]
      [:p.mb4 "Lasting relationships are often built through common interests."]
      [form/textarea
       {:on-change   #(dispatch [::update-personal-about (.. % -target -value)])
        :value       @about
        :placeholder "Help us get to know  you by telling us more about yourself. \n\nWhere are you from? \nWhat do you like to do? \nWhat are your hopes and dreams?"
        :rows        8}]

      ;; NOTE will implement this after MVP for onboarding is ready
      #_[form/select
         {:placeholder "How did you hear about us?"}
         [form/select-option "Website"]
         [form/select-option "Craigslist Ad"]
         [form/select-option "Friend"]]]]))
