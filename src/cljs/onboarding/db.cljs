(ns onboarding.db
  (:require [clojure.string :as string]
            [iface.modules.loading :as loading]
            [iface.utils.formatters :as formatters :refer [format]]
            [onboarding.routes :as routes]
            [iface.utils.log :as log]))


(def ^:private nav-items
  [{:section    :member-agreement
    :label      "Member Agreement"
    :icon       "check"
    :first-step :member-agreement/logistics}
   {:section    :helping-hands
    :label      "Helping Hands"
    :icon       "check"
    :first-step :helping-hands/byomf}
   {:section    :security-deposit
    :label      "Security Deposit"
    :icon       "check"
    :first-step :security-deposit/method}])


(def nav-path
  ::nav)


(defn bootstrap [account]
  (merge
   {:lang    :en
    :account account
    nav-path nav-items
    :route   {:page      :home
              :path      [:home]
              :params    {}
              :requester account}}
   loading/db))


;; ==============================================================================
;; navigation ===================================================================
;; ==============================================================================


(def ^:private first-step
  :member-agreement/logistics)


(defn- is-substep? [step]
  (contains? (into #{} (namespace step)) "."))


(defn step->route
  "Produce the route that corresponds to `step`."
  [step]
  (let [ns   (namespace step)
        name (name step)]
    (if (is-substep? step)
      (let [[section step] (string/split ns #"\.")]
        (routes/path-for :section.step/substep
                         :section-id section
                         :step-id step
                         :substep-id name))
      (routes/path-for :section/step
                       :section-id ns
                       :step-id name))))


(defn route->step
  "If the page is a section/step, produce the step that corresponds to this
  `route`, otherwise produce the correct page route."
  [{:keys [page] {:keys [section-id step-id substep-id]} :params, :as route}]
  (cond
    (and (not= page :section/step)
         (not= page :section.step/substep))
    page

    (nil? section-id)
    first-step

    (some? substep-id)
    (keyword (str section-id "." step-id) substep-id)

    :otherwise
    (keyword section-id step-id)))


(defn- step-dispatch
  [db]
  (-> db :route route->step))


(defn- step-data
  [db]
  (let [step (step-dispatch db)]
    (get db step {})))


;; next =========================================================================


(defmulti next-step step-dispatch)


(defmethod next-step :default [db _] first-step)


;; previous =====================================================================


(defmulti previous-step step-dispatch)


(defmethod previous-step :default [db _] first-step)


;; has next button? =============================================================


(defn has-next-button? [db]
  (let [step (step-dispatch db)]
    (boolean
     (#{ ;; add list of steps with a next button
        :member-agreement/logistics
        :member-agreement/thanks
        :helping-hands/bundles
        :helping-hands/request-furniture
        :helping-hands/packages
        :helping-hands/storage
        :helping-hands/dog-walking
        :helping-hands/dog-info
        :security-deposit.transfer/bank-info
        :security.deposit.transfer/verify-account
        ;; TODO make sure this list if complete
        }
      step))))


;; has back button? =============================================================


(defmulti has-back-button? step-dispatch)


(defmethod has-back-button? :default [db] true)


;; section completion ===========================================================


(defmulti section-complete? (fn [db section] section))


;; NOTE: should be default false. Changing it to `true` will toggle complete
;; state on all sections.
(defmethod section-complete? :default [_ _] false)


(defn can-navigate?
  "Is navigation to this `section` allowed?"
  [db section]
  (let [prior (->> (nav-path db) (map :section) (take-while (partial not= section)))]
    (every? (partial section-complete? db) prior)))


;; step completion ==============================================================


(defmulti step-complete? (fn [db step] step))


(defmethod step-complete? :default [_ _] false)
