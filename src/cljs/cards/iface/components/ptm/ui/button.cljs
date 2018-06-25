(ns cards.iface.components.ptm.ui.button
  (:require-macros [devcards.core :as dc :refer [defcard
                                                 defcard-doc
                                                 defcard-rg]])

  (:require [devcards.core]
            [iface.components.ptm.ui.button :as button]
            [reagent.core :as r]
            [antizer.reagent :as ant]))


(defcard-doc
  "
# Rendering buttons
")


(defcard-rg primary-button
  "
## Primary buttons
<br>
<br>
"
  [:div

   [button/primary
    {:on-click #(.log js/console "click")
     :loading  true}
    " button"]

   ])
