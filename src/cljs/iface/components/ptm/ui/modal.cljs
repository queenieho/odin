(ns iface.components.ptm.ui.modal
  (:require [antizer.reagent :as ant]
            [cljs.spec.alpha :as s]
            [iface.components.ptm.ui.carousel :as carousel]
            [reagent.core :as r]
            [devtools.defaults :as d]))


;; modal ========================================================================


(defn modal [{:keys [visible on-close]}]
  (when visible
    [:div
     (into [:div.lightbox
            [:img.lightbox-close
             {:on-click #(on-close)
              :src "/assets/images/ptm/icons/ic-lightbox-close.svg"}]]
           (r/children (r/current-component)))
     [:div.scrim
      {:on-click #(on-close)}]]))


;; community modal ==============================================================


(defn amenity-item [{:keys [label img]}]
  [:div.dt.mb2
   [:div.dtc.v-mid
    [:img {:src img}]]
   [:div.dtc.v-mid.pl2
    label]])


(defn amenities [section items])


(defmulti section (fn [title content] title))


(defmethod section "Amenities" [title content]
  (let [left (take (Math/ceil (/ (count content) 2)) content)
        right (drop (Math/ceil (/ (count content) 2)) content)]
    [:div
     [:h3.mt4 title]
     [:div.cf
      (into [:div.w-50-l.w-100.fl.pr3]
            (map
             (fn [item]
               [amenity-item item])
             left))
      (into [:div.w-50-l.w-100.fl.pl3-l]
            (map
             (fn [item]
               [amenity-item item])
             right))]]))


(defmethod section :default [title content]
  [:div
   [:h3.mt4 title]
   [:p content]])


(defn community-info [{:keys [images name price units-available intro
                              building-details neighborhood community-desc
                              amenities on-select on-next value next selected]
                       :as   props}]
  [:div
   [:div.lightbox-mask
    [carousel/modal images]
   [:div.lightbox-content
    [:h1 name]
    [:h3 price
     [:br]
     (str units-available " units open")]
    [:p.mt4 intro]
    [section "Building Details" building-details]
    [section "Amenities" amenities]
    [section "The Neighborhood" neighborhood]
    [section "Your Community" community-desc]]]
   [:div.lightbox-footer
    [:div.lightbox-footer-left
     (if selected
       [:a.text-link.text-green.fl
        {:on-click (when-let [s on-select]
                     #(s value))}
        [ant/icon {:type "check"}] " Community selected"]
       [:a.text-link.fl
        {:on-click (when-let [s on-select]
                     #(s value))}
        "Select this community"])]
    [:div.lightbox-footer-right
     [:a.text-link.fr.tr
      {:on-click (when-let [n on-next]
                   #(n))}
      (str "Next: " (:name next))]]]])


(defn community [{:keys [visible on-close] :as props}]
  [modal
   {:visible visible
    :on-close #(on-close)}
   [community-info (dissoc props :visible :on-close)]])
