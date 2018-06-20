(ns cards.iface.components.ptm.ui.cards
  (:require-macros [devcards.core :as dc :refer [defcard
                                                 defcard-doc
                                                 defcard-rg]])
  (:require [devcards.core]
            [iface.components.ptm.ui.cards :as cards]
            [reagent.core :as r]))


(defcard-doc
  "
  ## Rendering Reagent Components
  Rendering Path to Membership Cards
  "
  )


(defcard-rg card-single
  "
## Single Selection Card
Single Selection Cards are meant to be used like radio buttons.
They allow the user to make one selection out of a group
<br>
"
  (fn [data _]
    (.log js/console @data)
    (let [value 1]
      [cards/single
       {:title   "Card 1"
        :value   value
        :on-click #(doall
                    (swap! data assoc :selected value)
                    (.log js/console @data))}]))
  (r/atom {:selected nil})
  {:inspect-data true})
