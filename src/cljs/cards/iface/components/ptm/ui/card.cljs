(ns cards.iface.components.ptm.ui.card
  (:require-macros [devcards.core :as dc :refer [defcard
                                                 defcard-doc
                                                 defcard-rg]])
  (:require [devcards.core]
            [iface.components.ptm.ui.card :as card]
            [reagent.core :as r]))


(defcard-doc
  "
  # Rendering Card Components
  "
  )


(defcard-rg single-cards
  "
## Single Card
`single` card can be used as radio buttons or as individual buttons if you wish to set a custom `:on-click` prop to it. If no `:on-click` is set, it will only display data.
<br>
<br>
### Simple Text Card
```clojure
[card/single
 {:title    \"This is a title\"
  :on-click #(.log js/console \"I'm being poked!\")}]
```
<br>
"
  (fn [data _]
    [:div.cf
     [card/single
      {:title    "This is a title"
       :on-click #(.log js/console "I'm being poked!")}]])
  (r/atom [])
  {:frame false})


(defcard-rg illo-card
  "
### Content and Styling Card
The following content props can be added to a card: `:title`, `:subtitle`, `:description`, `:img`, `:tag`, `:footer`.
<br>
Card by default are center aligned, but an `:align` prop can be passed to set it to `:center`, or `:left`.
<br>
Card will automatically be a third of the width of the container they're in, this can be overwritten passing a `:width` prop with a value of `:half`.
<br>
<br>
```clojure
[card/single
 {:title       \"Kitten!\"
  :subtitle    \"Such floof!\"
  :description \"This is a description being used\"
  :img         \"http://placekitten.com/90/90\"
  :tag         \"Cats\"
  :footer      \"Paws\"
  :width       :half
  :disabled    true
  :align       :left}]
```
<br>
"
  (fn [_ _]
    [:div.cf
     [card/single
      {:title       "Kitten!"
       :subtitle    "Such floof!"
       :description "This is a description being used"
       :img         "http://placekitten.com/300/300"
       :tag         "Cats"
       :footer      "Paws"
       :width       :half
       :disabled    true
       :align       :left}]

     ])
  nil
  {:heading false
   :frame   false})


(defcard-rg h1-card
  "
### H1 Card
`single-h1` can be used when there's a need to highlight feature text. A footer can be added to help clarify the selection. H1 Card can be used like single card.
<br>
H1 card can have the following props: `:value`, `:on-click`, `:title`, `:subtitle`, `:footer`, and `:width`.
<br>
<br>
```clojure
[card/single-h1
 {:title    \"6\"
  :subtitle \"months\"
  :footer   \"+ $50/month\"}]
```
<br>
"
  (fn [_ _]
    [:div.cf
     [card/single-h1
      {:value    6
       :title    "6"
       :subtitle "months"
       :footer   "+ $50/month"}]])
  nil
  {:heading false
   :frame   false})


(defn- update-group-value [coll v]
  (if (some #(= v %) coll)
    (remove #(= v %) coll)
    (conj coll v)))


(defcard-rg groups-card-single
  "
<hr>
<br>
## Card Groups
Can be used as radio buttons or as checkboxes depending on the `:on-change` prop that is passed to it.
<br>
Card groups can also receive a `:card-width` (`:third` | `:half`) prop that will determine the width of each card contained in the group.
<br>
<br>
<br>
### Single Selection Card Groups
Single Selection Groups are meant to be used like radio buttons. `single` card with a unique `:value` should be used for this type of groups.
<br>
<br>
```clojure
[card/group
 {:on-change  #(swap! data assoc :selected %)
  :value      (:selected @data)
  :card-width :half}
 [card/single
  {:title \"Card 1\"
   :value 1}]
 [card/single
  {:title \"Card 2\"
   :value 2}]
 [card/single
  {:title \"Card 3\"
   :value 3}]]
```
"
  (fn [data _]
    [card/group
     {:on-change  #(swap! data assoc :selected %)
      :value      (:selected @data)
      :card-width :half}
     [card/single
      {:title "Card 1"
       :value 1}]
     [card/single
      {:title "Card 2"
       :value 2}]
     [card/single
      {:title "Card 3"
       :value 3}]])

  (r/atom {:selected nil})
  {:inspect-data true
   :heading      true
   :frame        false})


(defcard-rg groups-card-multiple
  "
<br>
### Multiple Selection Card Groups
Multiple Selection Card are meant to be used like checkboxes. `multiple` card with a unique `:value` should be used for this type of groups.
<br>
<br>
```clojure
[card/group
 {:on-change  #(swap! data update :selected update-group-value %)
  :value      (:selected @data)
  :card-width :third
  :show-count true}
 [card/multiple
  {:title       \"Card 1\"
   :img         \"http://placekitten.com/200/200\"
   :subtitle    \"best option\"
   :description \"buy all the bundles!\"
   :value       1}]
 [card/multiple
  {:title       \"Card 2\"
   :img         \"http://placekitten.com/200/200\"
   :subtitle    \"best option\"
   :description \"buy all the bundles!\"
   :value       2}]
 [card/multiple
  {:title       \"Card 3\"
   :img         \"http://placekitten.com/200/200\"
   :subtitle    \"best option\"
   :description \"buy all the bundles!\"
   :value       3}]]
```
"
  (fn [data _]
    [card/group
     {:on-change  #(swap! data update :selected update-group-value %)
      :value      (:selected @data)
      :card-width :third
      :show-count true}
     [card/multiple
      {:title       "Card 1"
       :img         "http://placekitten.com/200/200"
       :subtitle    "best option"
       :description "buy all the bundles!"
       :value       1}]
     [card/multiple
      {:title       "Card 2"
       :img         "http://placekitten.com/200/200"
       :subtitle    "best option"
       :description "buy all the bundles!"
       :value       2}]
     [card/multiple
      {:title       "Card 3"
       :img         "http://placekitten.com/200/200"
       :subtitle    "best option"
       :description "buy all the bundles!"
       :value       3}]])

  (r/atom {:selected []})
  {:inspect-data true
   :heading      true
   :frame        false})


(defcard-rg groups-card-carousel
  "
<br>
### Carousel Card Groups
Carousel Card are meant to be used like checkboxes. `carousel-card` card with a unique `:value` should be used for this type of groups. An `:images` prop with an array or list of image sources will be required for the carrousel.
<br>
<br>
```clojure
[card/group
 {:on-change  #(swap! data update :selected update-group-value %)
  :value      (:selected @data)
  :card-width :third
  :show-count true}
 [card/carousel-card
  {:title       \"Card 1\"
   :tag         \"Most popular\"
   :subtitle    \"best option\"
   :description \"buy all the bundles!\"
   :images      [\"http://placekitten.com/600/600\"
                 \"http://placekitten.com/600/400\"
                 \"http://placekitten.com/600/500\"
                 \"http://placekitten.com/600/700\"]
   :value       1}]
 [card/carousel-card
  {:title       \"Card 2\"
   :subtitle    \"best option\"
   :description \"buy all the bundles!\"
   :images      [\"http://placekitten.com/600/600\"
                 \"http://placekitten.com/600/400\"
                 \"http://placekitten.com/600/500\"
                 \"http://placekitten.com/600/700\"]
   :value       2}]
 [card/carousel-card
  {:title       \"Card 3\"
   :subtitle    \"best option\"
   :description \"buy all the bundles!\"
   :images      [\"http://placekitten.com/600/600\"
                 \"http://placekitten.com/600/400\"
                 \"http://placekitten.com/600/500\"
                 \"http://placekitten.com/600/700\"]
   :value       3}]]
```
"
  (fn [data _]
    [card/group
     {:on-change  #(swap! data update :selected update-group-value %)
      :value      (:selected @data)
      :card-width :third
      :show-count true}
     [card/carousel-card
      {:title       "Card 1"
       :tag         "Most popular"
       :subtitle    "best option"
       :description "buy all the bundles!"
       :images      ["http://placekitten.com/600/600"
                     "http://placekitten.com/600/400"
                     "http://placekitten.com/600/500"
                     "http://placekitten.com/600/700"]
       :value       1}]
     [card/carousel-card
      {:title       "Card 2"
       :subtitle    "best option"
       :description "buy all the bundles!"
       :images      ["http://placekitten.com/600/600"
                     "http://placekitten.com/600/400"
                     "http://placekitten.com/600/500"
                     "http://placekitten.com/600/700"]
       :value       2}]
     [card/carousel-card
      {:title       "Card 3"
       :subtitle    "best option"
       :description "buy all the bundles!"
       :images      ["http://placekitten.com/600/600"
                     "http://placekitten.com/600/400"
                     "http://placekitten.com/600/500"
                     "http://placekitten.com/600/700"]
       :value       3}]])

  (r/atom {:selected []})
  {:inspect-data true
   :heading      true
   :frame        false})


(defn summary-items []
  (let [items [{:label    "Move-in Date"
                :value    "ASAP"
                :edit     true
                :on-click #(.log js/console "move-in")}
               {:label    "Adult occupants"
                :value    "1"
                :edit     true
                :on-click #(.log js/console "occupants")}
               {:label    "Term length"
                :value    "12 months"
                :edit     true
                :on-click #(.log js/console "term")}
               {:label    "Dog"
                :value    "Yes"
                :edit     true
                :on-click #(.log js/console "dog")}]]
    [card/logistics-summary {:title "Logistics"
                             :items items}]))


(defcard-rg summary-card
  "
<br>
## Summary Card
<br>
### Logistics Summary Card
`logistics-summary` is meant to show a summary of logistics answers of the member application. This component takes `2` props: a `:title` and a collection of `:items`, which will be maps in the following form: <br>
```clojure
{:label    String - required
 :value    String - required
 :edit     Boolean
 :on-click (function)}
```
<br>
"
  (fn [data _]
    [summary-items])

  (r/atom {:selected []})
  {:heading true
   :frame   false})


(defcard-doc
  " #### Logistics Summary Card Sample Code"
  (dc/mkdn-pprint-source summary-items))


(defn- community-selection-sample []
  (let [items [{:label   "Suite Fee"
                :tooltip "This is a tooltip about Suite Fees. We need this to explain the very complicated price breakdown we have."
                :price   1600}
               {:label "Membership Fee"
                :price 300}
               {:label "Pet Fee"
                :price 75}
               {:label   "Suite Features"
                :tooltip "This is a tooltip about Suite Features. Because some people won't get why a range."
                :price   0
                :max     100}]
        total {:label   "Total"
               :tooltip "This is the sum of all items."
               :price   1975
               :max     2090}]
    [card/community-selection {:community  "Tenderloin"
                               :units      5
                               :on-click   #(.log js/console "click")
                               :line-items items
                               :total      total}]))


(defcard-rg summary-card
  "
<br>
### Community Selection Summary Card
Each `community-selection` card displays the number of preferred units in a chosen community and it's respective cost breakdown. It takes 5 props `:community` (`String`), `:units` (`Int`), `:line-items` (`list of cost breakdown`), and `total` (`map of total cost`).
<br>
<br>
Each `line-item` and the `total` should follow the following shape:
<br>
```clojure
{:label   String for breakdown - Required
 :price   Integer - Required
 :tooltip String for tooltip text - Required
 :max     Integer - Optional. Will be the max price if using a price range}
```
<br>
There should be a row container for every 2 `community-selection` cards.
<br>
<br>
"
  (fn [data _]
    [:div.w-100.cf
     [community-selection-sample]])

  (r/atom {:selected []})
  {:heading      true
   :frame        false})


(defcard-doc
  " #### Community selection summary card code sample"
  (dc/mkdn-pprint-source community-selection-sample))


(defn- coapplicant-community-selection-sample []
  (let [items [{:label   "Suite Fee"
                :tooltip "This is a tooltip about Suite Fees. We need this to explain the very complicated price breakdown we have."
                :price   1600}
               {:label "Membership Fee"
                :price 300}
               {:label "Pet Fee"
                :price 75}
               {:label   "Suite Features"
                :tooltip "This is a tooltip about Suite Features. Because some people won't get why a range."
                :price   0
                :max     100}]
        total {:label   "Total"
               :tooltip "This is the sum of all items."
               :price   1975
               :max     2090}]
    [card/coapplicant-community-selection {:community  "Tenderloin"
                                           :units      5
                                           :line-items items
                                           :total      total}]))


(defcard-rg coapplicant-summary-card
  "
<br>
## This docstring is not the right one, update when this component actually works
### Community Selection Summary Card
Each `community-selection` card displays the number of preferred units in a chosen community and it's respective cost breakdown. It takes 5 props `:community` (`String`), `:units` (`Int`), `:line-items` (`list of cost breakdown`), and `total` (`map of total cost`).
<br>
<br>
Each `line-item` and the `total` should follow the following shape:
<br>
```clojure
{:label   String for breakdown - Required
 :price   Integer - Required
 :tooltip String for tooltip text - Required
 :max     Integer - Optional. Will be the max price if using a price range}
```
<br>
"
  (fn [data _]
    [:div.w-100.cf
     [coapplicant-community-selection-sample]])

  (r/atom {:selected []})
  {:heading      true
   :frame        false})


(defcard-doc
  " #### Community selection summary card code sample"
  (dc/mkdn-pprint-source coapplicant-community-selection-sample))
