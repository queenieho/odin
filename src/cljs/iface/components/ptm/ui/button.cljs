(ns iface.components.ptm.ui.button
  (:require [reagent.core :as r]
            [cljs.spec.alpha :as s]
            [antizer.reagent :as ant]))



(defn- get-button-class [type]
  (get {:primary   ""
        :secondary "button-secondary"
        :text      "button-text"} type))


(s/def ::disabled
  boolean?)

(s/def ::type
  #{:primary :secondary :text :upload})

(defn button [{:keys [disabled type loading on-click class] :as props}]
  (let [props' (r/merge-props {:on-click on-click
                               :class    class
                               :disabled disabled}
                              {:class (str (when (some? type) (get-button-class type))
                                           (when disabled " button-disabled"))})]
    (into [:button.button props'
           (when loading
             [ant/icon {:class "mr2"
                        :type  "loading"}])]
            (r/children (r/current-component)))))

(s/fdef button
        :args (s/cat :props (s/keys :opt-un [::disabled
                                             ::type])))


(defn primary
  [props]
  (into [button props] (r/children (r/current-component ))))


(defn secondary
  [props]
  (into [button (assoc props :type :secondary)] (r/children (r/current-component))))


(defn text
  [props]
  (into [button (assoc props :type :text)] (r/children (r/current-component))))


(defn upload
  [props]
  (into [:div.button-upload props] (r/children (r/current-component))))
