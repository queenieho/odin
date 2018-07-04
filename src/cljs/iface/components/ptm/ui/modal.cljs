(ns iface.components.ptm.ui.modal
  (:require [antizer.reagent :as ant]
            [cljs.spec.alpha :as s]
            [iface.components.ptm.ui.carousel :as carousel]
            [reagent.core :as r]
            [devtools.defaults :as d]))


;; specs ========================================================================


(s/def ::on-close
  fn?)

(s/def ::visible
  boolean?)

(s/def ::images
  some?)

(s/def ::name
  string?)

(s/def ::price
  number?)

(s/def ::units-available
  int?)

(s/def ::intro
  string?)

(s/def ::building-desc
  string?)

(s/def ::neighborhood
  string?)

(s/def ::community-desc
  string?)

(s/def ::amenities
  coll?)

(s/def ::on-select
  fn?)

(s/def ::on-next
  fn?)

(s/def ::value
  some?)

;; still chewing on this too
(s/def ::next
  map?)

(s/def ::selected
  boolean?)


;; modal ========================================================================


(defn modal
  "Renders a blank modal. Can take children to be rendered in it.
  Can be extended to have more features once we understand it's usage better."
  [{:keys [visible on-close]}]
  (when visible
    [:div
     (into [:div.lightbox
            [:img.lightbox-close
             {:on-click #(on-close)
              :src "/assets/images/ptm/icons/ic-lightbox-close.svg"}]]
           (r/children (r/current-component)))
     [:div.scrim
      {:on-click #(on-close)}]]))

(s/fdef modal
  :args (s/cat :props (s/keys :req-un [::visible
                                       ::on-close])))


;; community modal ==============================================================


(defn- amenity-item [{:keys [label img]}]
  [:div.dt.mb2
   [:div.dtc.v-mid
    [:img {:src img}]]
   [:div.dtc.v-mid.pl2
    label]])


(defn- amenities [section items]
  (into [:div.w-50-l.w-100.fl
         {:class (if (= section :left)
                   "pr3"
                   "pl3-l")}
         (map-indexed
          #(with-meta [amenity-item %2] {:key %1})
          items)]))


(defmulti section (fn [title content] title))


(defmethod section "Amenities" [title content]
  (let [left (take (Math/ceil (/ (count content) 2)) content)
        right (drop (Math/ceil (/ (count content) 2)) content)]
    [:div
     [:h3.mt4 title]
     [:div.cf
      [amenities :left left]
      [amenities :right right]]]))


(defmethod section :default [title content]
  [:div
   [:h3.mt4 title]
   [:p content]])


(defn community-info
  "Renders detailed information of a community."
  [{:keys [images name price units-available intro building-desc neighborhood
           community-desc amenities on-select on-next value next selected]
    :as   props}]
  [:div
   [:div.lightbox-mask
    [carousel/modal images]
    [:div.lightbox-content
     [:h1 name]
     [:h3 (str "From $" price)
      [:br]
      (str units-available " units open")]
     [:p.mt4 intro]
     (when-let [b building-desc] [section "Building Details" b])
     (when-let [a amenities] [section "Amenities" a])
     (when-let [n neighborhood] [section "The Neighborhood" n])
     (when-let [c community-desc] [section "Your Community" c])]]
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
    (when-let [n next]
      [:div.lightbox-footer-right
       [:a.text-link.fr.tr
        {:on-click (when-let [n on-next]
                     #(n))}
        (str "Next: " (:name n))]])]])

(s/fdef community-info
  :args (s/cat :props (s/keys :req-un [::images
                                       ::name
                                       ::price
                                       ::units-available
                                       ::intro
                                       ::value]
                              :opt-un [::building-desc
                                       ::amenities
                                       ::neighborhood
                                       ::community-desc
                                       ::selected
                                       ::on-select
                                       ::next
                                       ::on-next])))


(defn community
  "Renders a modal containing a community's detailed information."
  [{:keys [visible on-close] :as props
    :or   {visible true}}]
  [modal
   {:visible  visible
    :on-close (when-let [c on-close] #(c))}
   [community-info (dissoc props :visible :on-close)]])

(s/fdef community
  :args (s/cat :props (s/keys :req-un [::images
                                       ::name
                                       ::price
                                       ::units-available
                                       ::intro
                                       ::value]
                              :opt-un [::visible
                                       ::on-close
                                       ::building-desc
                                       ::amenities
                                       ::neighborhood
                                       ::community-desc
                                       ::selected
                                       ::on-select
                                       ::next
                                       ::on-next])))
