(ns onboarding.subs
  (:require [onboarding.db :as db]
            [re-frame.core :refer [reg-sub]]))


;; ==============================================================================
;; helper =======================================================================
;; ==============================================================================


(reg-sub
 :db
 (fn [db _]
   db))


(reg-sub
 :db/step
 (fn [db [_ step]]
   (step db)))


;; ==============================================================================
;; user =========================================================================
;; ==============================================================================


(reg-sub
 :user
 (fn [db _]
   (:account db)))


;; ==============================================================================
;; route ========================================================================
;; ==============================================================================


(reg-sub
 :route/current
 (fn [db _]
   (:route db)))


(reg-sub
 :route/path
 :<- [:route/current]
 (fn [{path :path} _]
   path))


(reg-sub
 :route/params
 :<- [:route/current]
 (fn [{params :params} _]
   params))


(reg-sub
 :route/root
 :<- [:route/path]
 (fn [path _]
   (first path)))


;; ==============================================================================
;; navigation ===================================================================
;; ==============================================================================


(reg-sub
 :nav/items
 (fn [db _]
   (get db db/nav-path)))


(reg-sub
 :nav.item/enabled?
 (fn [db [_ nav-item]]
   (and (db/can-navigate? db (:section nav-item))
        (= :in_progress (:application-status db)))))


(reg-sub
 :nav.item/complete?
 (fn [db [_ nav-item]]
   (db/section-complete? db (:section nav-item))))


;; ==============================================================================
;; steps ========================================================================
;; ==============================================================================


(reg-sub
 :step/current
 :<- [:route/current]
 (fn [route _]
   (db/route->step route)))


(reg-sub
 :step/complete?
 :<- [:db]
 :<- [:step/current]
 (fn [[db step] _]
   (db/step-complete? db step)))


(reg-sub
 :ui.step.current/has-back?
 (fn [db _]
   (db/has-back-button? db)))


(reg-sub
 :ui.step.current/has-next?
 (fn [db _]
   (db/has-next-button? db)))


(reg-sub
 :step.current/next
 (fn [db _]
   (db/next-step db)))


(reg-sub
 :step.current/previous
 (fn [db _]
   (db/previous-step db)))


(defn- lbl [s]
  (str "next: " s))


;; TODO finish this list
(reg-sub
 :step.current.next/label
 :<- [:step/current]
 (fn [step _]
   (case step
     :member-agreement/logistics      (lbl "Membership Agreement")
     :member-agreement/thanks         (lbl "Services")
     :helping-hands/bundles           (lbl "Furniture")
     :helping-hands/request-furniture (lbl "Package Delivery")
     :helping-hands/packages          (lbl "Storage")
     ;; if there's a dog "Dog walking"
     :helping-hands/storage           (lbl "Dog Walking")
     :helping-hands/dog-walking       (lbl "Pet Info")
     :helping-hands/pet-info          (lbl "Security Deposit")
     ;; TODO lay out the rest of the buttons for security deposit after
     ;; reorganizing designs
     :security-deposit/payment-method (lbl "")
     "next")))
