(ns cards.iface.components.ptm.ui.form
  (:require-macros [devcards.core :as dc :refer [defcard
                                                 defcard-doc
                                                 defcard-rg]])

  (:require [devcards.core]
            [iface.components.ptm.ui.form :as form]
            [reagent.core :as r]
            [antizer.reagent :as ant]))


(defcard-doc
  "
# Rendering form items
")


(defcard-rg text-input
  "
## Text input
The `text` form item is used for short text inputs.
<br>
This component takes can take a `:value` (`string`) and an `:on-change` (`(function [val])`) props.
<br>
<br>
```clojure
[form/text
 {:value     (:text @data)
  :on-change (fn [val] (swap! data assoc :text (.. val -target -value)))}]
```
<br>
"
  (fn [data _]
    [form/text
     {:value     (:text @data)
      :on-change #(swap! data assoc :text (.. % -target -value))}])
  (r/atom {:text nil})
  {:inspect-data true
   :frame        false
   :header       false})
