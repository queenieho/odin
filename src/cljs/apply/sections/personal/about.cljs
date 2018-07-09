(ns apply.sections.personal.about
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
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
  false)


;; events =======================================================================


(defmethod events/save-step-fx step
  [db params]
  {:db       (assoc db step params)
   :dispatch [:step/advance]})


;; views ========================================================================


(defmethod content/view step
  [_]
  [:div
   [:div.w-60-l.w-100
    [:h1 "Here's a little about us."]
    ]
   [:div.page-content.w-60-l.w-100
    [:p.mb4 "We believe that community is best created by the connections formed
    between individuals through shared values. Read more about Our Values on our
    website."]
    [:p.mb4 "Starcity is a safe space for individuals to come together and share
    their skills, experiences and perspectives. Our members give to one another
    and to the greater communities in which they live. We hope you're as excited
    about sharing and giving back as we are."]
    [:p.mb4 "Lasting relationships are often built through common interests."]
    [form/textarea
     {:on-change #(log/log (.. % -target -value))
      :placeholder "Help us get to know  you by telling us more about yourself. \n Where are you from? \n What do you like to do? \n What are your hopes and dreams?"}]

    [form/select
     {:placeholder "How did you hear about us?"}
     [form/select-option "Website"]
     [form/select-option "Craigslist Ad"]
     [form/select-option "Friend"]]]])
