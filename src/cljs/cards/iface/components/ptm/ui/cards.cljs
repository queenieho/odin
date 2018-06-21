(ns cards.iface.components.ptm.ui.cards
  (:require-macros [devcards.core :as dc :refer [defcard
                                                 defcard-doc
                                                 defcard-rg]])
  (:require [devcards.core]
            [iface.components.ptm.ui.cards :as cards]
            [reagent.core :as r]))


(defcard-doc
  "
  # Rendering Card Components
  "
  )


(defcard-rg single-cards
  "
## Single Cards
`single` cards can be used as radio buttons or as individual buttons if you wish to set a custom `:on-click` prop to it. If no `:on-click` is set, it will only display data.
<br>
<br>
### Simple Text Cards
```clojure
[cards/single
 {:title    \"This is a title\"
  :on-click #(.log js/console \"I'm being poked!\")}]
```
<br>
"
  (fn [data _]
    [:div.cf
     [cards/single
      {:title    "This is a title"
       :on-click #(.log js/console "I'm being poked!")
       }]])
  (r/atom [])
  {:frame false})


(defcard-rg illo-cards
  "
### Content and Styling Cards
The following content props can be added to a card: `:title`, `:subtitle`, `:description`, `:img`, `:tag`, `:footer`.
<br>
Cards by default are center aligned, but an `:align` prop can be passed to set it to `:center`, or `:left`.
<br>
Cards will automatically be a third of the width of the container they're in, this can be overwritten passing a `:width` prop with a value of `:half`.
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
     [cards/single
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


(defcard-rg h1-cards
  "
### H1 Cards
`single-h1` can be used when there's a need to highlight feature text. A footer can be added to help clarify the selection. H1 Cards can be used like single cards.
<br>
H1 cards can have the following props: `:value`, `:on-click`, `:title`, `:subtitle`, `:footer`, and `:width`.
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
     [cards/single-h1
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
Single Selection Groups are meant to be used like radio buttons. `single` cards with a unique `:value` should be used for this type of groups.
<br>
<br>
```clojure
[cards/group
 {:on-change  #(swap! data assoc :selected %)
  :value      (:selected @data)
  :card-width :half}
 [cards/single
  {:title \"Card 1\"
   :value 1}]
 [cards/single
  {:title \"Card 2\"
   :value 2}]
 [cards/single
  {:title \"Card 3\"
   :value 3}]]
```
"
  (fn [data _]
    [cards/group
     {:on-change  #(swap! data assoc :selected %)
      :value      (:selected @data)
      :card-width :half}
     [cards/single
      {:title "Card 1"
       :value 1}]
     [cards/single
      {:title "Card 2"
       :value 2}]
     [cards/single
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
Multiple Selection Cards are meant to be used like checkboxes. `multiple` cards with a unique `:value` should be used for this type of groups.
<br>
<br>
```clojure
[cards/group
 {:on-change  #(swap! data update :selected update-group-value %)
  :value      (:selected @data)
  :card-width :third
  :show-count true}
 [cards/multiple
  {:title       \"Card 1\"
   :img         \"http://placekitten.com/200/200\"
   :subtitle    \"best option\"
   :description \"buy all the bundles!\"
   :value       1}]
 [cards/multiple
  {:title       \"Card 2\"
   :img         \"http://placekitten.com/200/200\"
   :subtitle    \"best option\"
   :description \"buy all the bundles!\"
   :value       2}]
 [cards/multiple
  {:title       \"Card 3\"
   :img         \"http://placekitten.com/200/200\"
   :subtitle    \"best option\"
   :description \"buy all the bundles!\"
   :value       3}]]
```
"
  (fn [data _]
    [cards/group
     {:on-change  #(swap! data update :selected update-group-value %)
      :value      (:selected @data)
      :card-width :third
      :show-count true}
     [cards/multiple
      {:title       "Card 1"
       :img         "http://placekitten.com/200/200"
       :subtitle    "best option"
       :description "buy all the bundles!"
       :value       1}]
     [cards/multiple
      {:title       "Card 2"
       :img         "http://placekitten.com/200/200"
       :subtitle    "best option"
       :description "buy all the bundles!"
       :value       2}]
     [cards/multiple
      {:title       "Card 3"
       :img         "http://placekitten.com/200/200"
       :subtitle    "best option"
       :description "buy all the bundles!"
       :value       3}]])

  (r/atom {:selected []})
  {:inspect-data true
   :heading      true
   :frame        false})
