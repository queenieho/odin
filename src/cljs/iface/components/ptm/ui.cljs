(ns iface.components.ptm.ui
  (:require [antizer.reagent :as ant]
            [reagent.core :as r]
            [cljs.spec.alpha :as s]))



;; button =======================================================================

(defn- get-button-class [type]
  (get {:primary ""
        :secondary   "button-secondary"
        :text        "button-text"} type))


(s/def ::disabled
  boolean?)

(s/def ::type
  #{:primary :secondary :text :upload})

#_(defn button [{:keys [disabled type] :as props}]
  (let [props' (-> props
                   (merge {:class (str
                                   (:class props) " "
                                   (when (some? type) (get-button-class type))
                                   (when disabled " button-disabled"))})
                   (dissoc :disabled :type))]
    (into [:button.button props']
         (r/children (r/current-component)))))


(s/fdef button
        :args (s/cat :props (s/keys :opt-un [::disabled
                                             ::type])))


(defn pill [{:keys [active on-change on-delete selectable]}]
  (into [:div.pill {:class (str
                            (when active "pill-active")
                            (when (and (false? active) selectable) " pill-inactive")
                             )}]
        (r/children (r/current-component))))
