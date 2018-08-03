(ns apply.applications.views
  (:require [antizer.reagent :as ant]
            [apply.content :as content]
            [apply.db :as db]
            [apply.routes :as routes]
            [apply.sections.payment]
            [clojure.string :as s]
            [iface.components.ptm.layout :as layout]
            [iface.components.ptm.ui.label :as label]
            [iface.components.ptm.ui.tag :as tag]
            [iface.components.ptm.ui.button :as button]
            [iface.utils.formatters :as format]
            [iface.utils.log :as log]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]))


(defn- summary-item
  "Summary information item"
  [{:keys [label value edit on-click]}]
  [:div.w-50-l.w-100.fl.ph4.pv1
   [:h4.w-40.mv1.fl label]
   [:p.w-60.fl.tr.mv0 value
    (when edit
      [:img.icon-edit {:src      "/assets/images/ptm/icons/ic-edit.svg"
                       :on-click (when-let [c on-click]
                                   #(c))}])]])


(defn- summary-row
  "Row of 2 summary items"
  [items]
  [:div.w-100.cf
   (map-indexed
    (fn [i item]
      ^{:key i}
      [summary-item item])
    items)])


(defn- toggle [v]
  (if v
    false
    true))


(defn- line-label [label tooltip]
  (let [show (r/atom false)]
    (fn [label]
      [:h4.w-60.mv1.fl label
       (when tooltip
         [:a {:onMouseOver #(swap! show toggle)
              :onMouseOut  #(swap! show toggle)}
          [ant/tooltip {:title     tooltip
                        :placement "right"
                        :visible   @show}
           [:img.icon-small {:src "/assets/images/ptm/icons/ic-help-tooltip.svg"}]]])])))


(defn- line-item [type label tooltip cost & rest]
  (let [cstr  (format/currency cost)
        price (if (not-empty (remove nil? rest))
                (str cstr " - " (format/currency (first rest)))
                cstr)]
    [:div.cf.ph4
     [line-label label tooltip]
     (if (= type :line)
       [:p.w-40.fl.tr.mv0 price]
       [:h3.w-40.fl.tr.mt1.mb3 price])]))


(defn- communities-section [{:keys [community line-items total image]}]
  [:div
   {:style {:overflow   "auto"
            :border-top "1px solid #e6e6e6"}}
   [:div.w-25-l.w-100.fl.pv0.card-top
    [:h2.ma0 community]]
   ;; body
   [:div.w-75-l.w-100.fl.pv0
    [:div.w-50-l.w-100.fl.mv3
     [:div.ph4
      [:h3.mv2 "Cost Breakdown"]]
     ;; line items
     (map-indexed
      (fn [i {:keys [label tooltip price max]}]
        ^{:key i}
        [line-item :line label tooltip price max])
      line-items)
     (when-let [{:keys [label tooltip price max]} total]
       [:div
        [:hr.ph4]
        [line-item :total label tooltip price max]])]
    [:div.w-50-l.w-100.fl.pv0
     {:style {:background-image    (str "url('" image "')")
              :background-size     "cover"
              :background-position "center"
              :min-height          "200px"}}
     ]]])


(defn- review-card []
  (let [logistics   (subscribe [:review/logistics])
        personal    (subscribe [:review/personal])
        communities (subscribe [:review/communities])
        status      (subscribe [:application-status])]
    [:div.w-100.pr4-l.pr0
     (log/log @status)
     [:div.card.cf
      ;; header
      [:div.ph4.pv3
       {:style {:overflow "auto"}}
       (when (= :in_progress @status)
         [:div.fl
          [button/text
           {:on-click #(dispatch [:app.init/route-to-last-saved])}
           "Continue application"]])
       [:div.fr
        [label/label @status]]]
      [:div
       {:style {:overflow "auto"
                :border-top "1px solid #e6e6e6"}}
       [:div.w-25-l.w-100.fl.pv0.card-top
        [:h2.ma0 "Logistics"]]
       ;; body
       [:div.w-75-l.w-100.fl.pv3
        (map-indexed
         (fn [i row-items]
           ^{:key i}
           [summary-row row-items])
         (partition 2 2 nil @logistics))]]

      ;; Communities
      (when-not (nil? @communities)
        (map
         (fn [c]
           ^{:key (:id c)}
           [communities-section c])
         @communities))

      [:div
       {:style {:overflow   "auto"
                :border-top "1px solid #e6e6e6"}}
       [:div.w-25-l.w-100.fl.pv0.card-top
        [:h2.ma0 "Personal Information"]]
       ;; body
       [:div.w-75-l.w-100.fl.pv3
        (map-indexed
         (fn [i row-items]
           ^{:key i}
           [summary-row row-items])
         (partition 2 2 nil @personal))]]]]))


(defn- applications-layout []
  (let [user      (subscribe [:user])]
    [:div
     (log/log "logistics")
     [:div.w-100
      [:h1 "Good afternoon, " (first (s/split (:name @user) #" "))]
      [:p "Here is your current application."]]
     [:div.w-100
      [:div.page-content
       [review-card]]]]))


(defmethod content/view :applications []
  [layout/layout
   {:nav [layout/nav {:footer [layout/nav-footer
                               [layout/nav-item {:label "Log Out"
                                                 :link  (routes/path-for :logout)
                                                 :icon  "logout"}]]}
          [layout/nav-item {:label    "Application"
                            :disabled false}]]}
   [applications-layout]])
