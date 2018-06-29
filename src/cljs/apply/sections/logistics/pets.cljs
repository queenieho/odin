(ns apply.sections.logistics.pets
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.utils.log :as log]))


(def step :logistics/pets)


;; db ===========================================================================


;;TODO - selecting other does not go to the 'other' step. figure out why
(defmethod db/next-step step
  [db]
  (if (false? (step db))
    :community/select
    (if (some? (:logistics.pet/dog db))
      :logistics.pets/dog
      :logistics.pets/other)))


(defmethod db/previous-step step
  [db]
  :previous/step)


(defmethod db/has-back-button? step
  [_]
  false)


(defmethod db/step-complete? step
  [db step]
  false)


;; events =======================================================================


(defmethod events/save-step-fx step
  [db pet]
  #_(if (= :none pet)
      {:dispatch [:application/update {:has_pet false}]}
      {:dispatch [:application/update {:has_pet true}]})
  (case pet
    ;; TODO - conditionally *retract* :pet when choosing none
    :none  (dispatch [:application/update {:has_pet false}])
    :dog   (dispatch [:application/update {:has_pet true
                                           :pet     {:type :dog}}])
    :other (dispatch [:application/update {:has_pet true
                                           :pet     {:type :other}}])))


(defmethod events/gql->rfdb :has_pet [k] step)

(defmethod events/gql->rfdb :pet [k v]
  (if (= :dog (:type v))
    :logistics.pet/dog
    :logistics.pet/other))


;; views ========================================================================


(defmethod content/view step
  [_]
  [:div
   [:div.w-60-l.w-100
    [:h1 "Tell us about your fur family."]
    [:p "Most of our communities are dog-friendly, but we unfortunately do not
    allow cats. If you have a dog, please let us know what breed and weight."]]
   [:div.page-content.w-90-l.w-100
    [ant/button
     {:on-click #(dispatch [:step.current/next :dog])}
     "I have a dog"]
    [ant/button
     {:on-click #(dispatch [:step.current/next :none])}
     "No pets"]
    [ant/button
     {:on-click #(dispatch [:step.current/next :other])}
     "Other"]]])
