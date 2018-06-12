(ns iface.components.ptm.layout
  (:require [antizer.reagent :as ant]
            [reagent.core :as r]
            [iface.components.ptm.icons :as icons]
            [cljs.spec.alpha :as s]))


(defn- nav-icon
  [{:keys [type active]}]
  (icons/icon {:type  type
               :class (if active
                        "nav-icon active"
                        "nav-icon")}))


(defn- nav-item-class
  [progress]
  (get {:complete "nav-item-complete"
        :active   "nav-in-progress"} progress))


(s/def ::label
  (s/or :string string? :element vector?))

(s/def ::progress
  #{:complete :active})

(s/def ::link
  string?)

(s/def ::icon
  string?)

(defn nav-item
  "A single navigation item to be rendered within `nav` or `nav-footer`."
  [{:keys [progress label link action icon] :or {link ""}}]
  [:li {:class (nav-item-class progress)}
   (when (some? icon)
     (nav-icon {:type   icon
                :active (= progress :active)}))
   [:a.nav-link {:class    (when (some? progress)
                             "active")
                 :href     link
                 :on-click action}
    (r/as-element label)]])

(s/fdef nav-item
        :args (s/cat :props (s/keys :req-un [::label]
                                    :opt-un [::progress
                                             ::link
                                             ::icon])))

(defn nav-footer
  "An optional extra component, rendered at the bottom of the nav component."
  []
  [:div.nav-footer
   (into [:ul.nav-items] (r/children (r/current-component)))] )


(defn nav
  "A component that shows navigation and, when applicable, progress. Always
  renders to the left of the screen."
  []
  (let [this                          (r/current-component)
        {:keys [is-progress? footer]} (r/props this)]
    [:nav.nav-primary
     {:class (when is-progress? "nav-progress")}
     [:div.logo (icons/icon {:type  "logomark"
                             :class "logomark"})]
     (into [:ul.nav-items] (r/children this))
     (when (some? footer)
       (r/as-element footer))]))


(defn- mobile-progress-bar
  [progress]
  [:div.progress-mobile
   [:span.progress-mobile-fill
    {:style {:width (str (or progress 0) "%")}}]])


(defn- footer-small
  [{:keys [destructive primary secondary] :as items}]
  (let [item-class (case (count items)
                     1 "footer-full"
                     2 "footer-half"
                     3 "footer-third"
                     nil)]
    [:div.footer-small
     (when-let [{:keys [label label-small link action]} destructive]
       [:div {:class item-class}
        [:a.footer-link
         {:href     (or link "")
          :on-click action}
         (or label-small label)]])
     (when-let [{:keys [label label-small link action]} secondary]
       [:div {:class item-class}
        [:a.footer-link
         {:href     (or link "")
          :on-click action}
         (or label-small label)]])
     (when-let [{:keys [label label-small link action]} primary]
       [:div {:class item-class}
        [:a.footer-link.active
         {:href     (or link "")
          :on-click action}
         (or label-small label)]])]))


(defn- footer-large
  [{:keys [destructive primary secondary] :as items}]
  [:div.footer-large
   (when-let [{:keys [label link action]} destructive]
     [:div.footer-left
      ;; TODO: come up with a way to do `:a.button` by providing button styling
      [:a {:href (or link "")}
       [:button.button.button-text
        {:on-click action}
        label]]])
   [:div.footer-right
    (when-let [{:keys [label link action]} primary]
      [:a {:href (or link "")}
       [:button.button
        {:on-click action}
        label]])
    (when-let [{:keys [label link action]} secondary]
      [:a {:href (or link "")}
       [:button.button.button-secondary
        {:on-click action}
        label]])]])


(defn footer
  "The footer."
  [footer-items]
  [:footer.footer-wizard
   [footer-large footer-items]
   [footer-small footer-items]])


(defn layout
  "we're not done with it."
  []
  (let [{:keys [nav progress footer pre]} (r/props (r/current-component))]
    [:div
     icons/svg
     (mobile-progress-bar progress)
     (when (some? nav) (r/as-element nav))
     (r/as-element pre)
     (into [:section.main {:class (when-not (some? nav) "main-no-nav center")}]
           (r/children (r/current-component)))
     (when (some? footer) (r/as-element footer))]))
