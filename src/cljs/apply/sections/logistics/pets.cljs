(ns apply.sections.logistics.pets
  (:require [apply.content :as content]
            [antizer.reagent :as ant]
            [re-frame.core :refer [dispatch subscribe]]
            [apply.events :as events]
            [apply.db :as db]
            [iface.utils.log :as log]
            [iface.components.ptm.ui.card :as card]))


(def step :logistics/pets)


;; db ===========================================================================


(defmethod db/next-step step
  [db]
  (if (false? (step db))
    :community/select
    (if (= :dog (get-in db [:logistics.pets/dog :type]))
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
  (some? (step db)))


;; events =======================================================================


(defmethod events/save-step-fx step
  [db pet]
  (case pet
    ;; TODO - conditionally *retract* :pet when choosing none
    :none  (dispatch [:application/update {:has_pet false}])
    :dog   (dispatch [:application/update {:has_pet true
                                           :pet     {:type :dog}}])
    :other (dispatch [:application/update {:has_pet true
                                           :pet     {:type :other}}])))


(defmethod events/gql->rfdb :has_pet [k] step)

(defmethod events/gql->rfdb :pet [k v]
  (log/log "is this a dog?" (:type v))
  (log/log "does that equal :dog?" (= :dog (:type v)))
  (if (= :dog (:type v))
    :logistics.pets/dog
    :logistics.pets/other))


;; views ========================================================================


(defmethod content/view step
  [_]
  [:div
   [:div.w-60-l.w-100
    [:h1 "Tell us about your fur family."]
    [:p "Most of our communities are dog-friendly, but we unfortunately don't
    allow cats. If you have a dog, please tell us a little bit about them."]]
   [:div.w-80-l.w-100
    [:div.page-content
    [card/single
     {:title    "I have a dog"
      :img      "/assets/images/ptm/icons/sketch-dog.svg"
      :on-click #(dispatch [:step.current/next :dog])}]
    [card/single
     {:title    "No pets"
      :img      "/assets/images/ptm/icons/sketch-no-pet.svg"
      :on-click #(dispatch [:step.current/next :none])}]
    [card/single
     {:title    "Other"
      :img      "/assets/images/ptm/icons/sketch-hedgehog.svg"
      :on-click #(dispatch [:step.current/next :other])}]]]])
