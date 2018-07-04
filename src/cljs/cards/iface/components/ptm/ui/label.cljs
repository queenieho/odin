(ns cards.iface.components.ptm.ui.label
  (:require-macros [devcards.core :as dc :refer [defcard
                                                 defcard-doc
                                                 defcard-rg]])

  (:require [devcards.core]
            [iface.components.ptm.ui.label :as label]
            [reagent.core :as r]
            [antizer.reagent :as ant]))


(defcard-doc
  "
# Rendering Labels
")


(defcard-rg labels
  "
## Labels
`labels` are used to indicate counts (such as # of units selected). They can be used with ints or strings.
<br>
<br>
```clojure
[label/label 26]

[label/label \"some label\"]
```
<br>
"
  (fn []
    [:div
     [:span.mr2
      [label/label 26]]
     [:span.mr2
      [label/label "some label"]]])
  nil
  {:heading false
   :frame   false })
