(ns iface.components.ptm.layout
  (:require [antizer.reagent :as ant]
            [reagent.core :as r]
            [iface.components.ptm.icons :as icons]
            [cljs.spec.alpha :as s]
            [toolbelt.core :as tb]
            [iface.components.ptm.ui.button :as button]
            [iface.utils.log :as log]))


(defn- nav-icon
  [{:keys [type active disabled link action]}]
  [:a
   {:href     (when-not disabled link)
    :on-click (when-not disabled action)}
   (icons/icon {:type type
                :class (str (if active
                              "nav-icon active"
                              "nav-icon")
                            (when disabled " disabled"))})])


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
  [{:keys [progress disabled label link action icon] :or {link ""}}]
  [:li {:class (nav-item-class progress)}
   (when (some? icon)
     (nav-icon {:type     icon
                :disabled disabled
                :active   (some? progress)
                :link     (when-let [l link] l)
                :action   (when-let [a action] a)}))
   [:a.nav-link
    (tb/assoc-when
     {:class (str
              (when (some? progress)
                "active")
              (when disabled " disabled"))}
     :href     (when-not disabled link)
     :on-click (when-not disabled action))
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
   (when-let [{:keys [label link action disabled loading]} destructive]
     [:div.footer-left
      ;; TODO: come up with a way to do `:a.button` by providing button styling
      [:a {:href (or link "")}
       [button/text
        {:on-click action
         :disabled disabled
         :loading  loading}
        label]]])
   [:div.footer-right
    (when-let [{:keys [label link action disabled loading]} primary]
      [:a {:href (or link "")}
       [button/primary
        {:on-click action
         :disabled disabled
         :loading  loading}
        label]])
    (when-let [{:keys [label link action disabled loading]} secondary]
      [:a {:href (or link "")}
       [button/secondary
        {:on-click action
         :disabled disabled
         :loading  loading}
        label]])]])


(defn footer
  "The footer component, which renders `destructive`, `primary`, and `secondary`
  calls to action."
  [footer-items]
  [:footer.footer-wizard
   [footer-large footer-items]
   [footer-small footer-items]])


(defn layout
  "The main layout component, which defines the nav, footer, and main content for
  the current view."
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


(defn loading-fullpage
  "Loading page component"
  [& {:keys [text loading]
      :or   {text    "Loading..."
             loading false}}]
  [layout
   [:div.w-100.pa7
    {:style {:text-align "center"}}
    (when loading
      [:img.mb3
       {:style {:height "50px"}
        :src   "/assets/images/ptm/loading.gif"}])
    [:h1 text]]])
