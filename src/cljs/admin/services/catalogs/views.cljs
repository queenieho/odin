(ns admin.services.catalogs.views
  (:require [admin.content :as content]
            [antizer.reagent :as ant]
            [reagent.core :as r]
            [admin.services.subs]
            [re-frame.core :refer [subscribe dispatch]]
            [admin.routes :as routes]))


;; ==============================================================================
;; subview ======================================================================
;; ==============================================================================

(defn- controls [properties]
  [:div.columns
   [:div.column.is-3
    [ant/select
     {:style       {:width "100%"}
      :placeholder "select a property"}
     (map (fn [{value :value
               name  :name}]
            (r/as-element [ant/select-option
                           {:value value
                            :key   value}
                           name]))
          properties)]]
   [:div.column.is-9.has-text-right
    [ant/button
     {:type :primary
      :icon "plus"}
     "New Service"]
    [ant/button
     {:type :primary
      :icon "plus"}
     "New Catalog"]]])

;; TODO - the out-of-the-box behavior between ant/popconfirm and ant/checkbox
;; doesn't really work for what we want to do. once we're working with data in
;; the re-frame db, i bet we can leverage some subs/dispatches to get the
;; desired behavior - that the value of the checkbox changes only after confirmation
(defn toggle-active-checkbox []
  "Wrap the checkbox for toggling a service's availability with a confirmation popup.
   That's the sort of thing that probably shouldn't change by accident"
  [ant/popconfirm
   {:title "Are you sure you want to toggle this service's availability?"
    :onConfirm #(js/console.log "it is so ordered")
    :onCancel  #(js/console.log "nah i'm good")}
   [ant/checkbox]])

(defn- content [catalogs services]
  [:div.columns
   [:div.column.is-3
    [:h2 "Catalogs"]
    [ant/menu
     {:mode :vertical}
     (map #(r/as-element [ant/menu-item {:key %} %]) catalogs)]]
   [:div.column
    [:h2 "Services"]
    [ant/table
     {:dataSource services
      :bordered   true
      :scroll     {:x 100}
      :columns    [{:title     "Name"
                    :dataIndex "name"
                    :key       "name"}
                   {:title     "Price"
                    :dataIndex "price"
                    :key       "price"
                    :render    #(r/as-element [:span.is-pulled-right "$" %])}
                   {:title  (r/as-element [:span "Active? "[ant/tooltip {:title "Toggle availability of a service at the selected property."} [ant/icon {:type "info-circle-o"}]]])
                    :render #(r/as-element [:span.is-pulled-right [toggle-active-checkbox]])}]}]]])

(defn subview []
  (let [services (subscribe [:services/list]) ;; TODO - the query that populates this property only runs when the services tab is clicked. fix.
        properties [{:name "West SoMa" :value :52gilbert} {:name "The Mothership" :value :944market} {:name "The Pirate Ship" :value :995market} {:name "Steff's Corporate Partnership" :value :611mission}]
        catalogs ["All" "Pets" "Laundry" "Storage" "Furniture"]] ;; TODO - replace hardcoded data
    [:div
     [controls properties]
     [content catalogs @services]]))
