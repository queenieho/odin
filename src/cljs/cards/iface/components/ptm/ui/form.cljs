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
The `text` form item is used for short text inputs. This input will take up 100% of the width of it's container.
<br>
This component takes a `:value` (`string`) and an `:on-change` (`(function [val])`) props.
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
    [:div {:style {:width "25vw"}}
     [form/text
      {:value     (:text @data)
       :on-change #(swap! data assoc :text (.. % -target -value))}]])
  (r/atom {:text nil})
  {:inspect-data true
   :frame        false
   :header       false})


(defcard-rg number-input
  "
<br>
<hr>
<br>
## Number input
The `number` form item is used for number inputs. This input will take up 100% of the width of it's container.
<br>
<br>
This component takes `:value` (`integer`) and an `:on-change` (`(function [val])`) props.
<br>
Optionally `:step` (`integer`), `:min` (`integer`), and `:max` (`integer`) can be passed too.
<br>
<br>
```clojure
[form/number
 {:value     (:num @data)
  :on-change (fn [val] (swap! data assoc :num (.. val -target -value)))
  :step      10
  :min       5
  :max       65}]
```
<br>
"
  (fn [data _]
    [:div {:style {:width "15vw"}}
     [form/number
      {:value     (:num @data)
       :step      10
       :on-change #(swap! data assoc :num (.. % -target -value))
       :min       5
       :max       65}]])
  (r/atom {:num nil})
  {:inspect-data true
   :frame        false
   :header       false})


(defcard-rg textarea
  "
<br>
<hr>
<br>
## Textarea input
The `textarea` form item is used for long text inputs. This input will take up 100% of the width of it's container and have `:rows` set to `2` by default.
<br>
<br>
This component takes `:value` (`string`) and an `:on-change` (`(function [val])`) props.
<br>
Optionally `:rows` (`integer`) can be passed.
<br>
<br>
```clojure
[form/textarea
 {:value     (:text @data)
  :rows      5
  :on-change (fn [val] (swap! data assoc :text (.. val -target -value)))}]
```
<br>
"
  (fn [data _]
    [:div {:style {:width "35vw"}}
     [form/textarea
      {:value     (:text @data)
       :rows      5
       :on-change #(swap! data assoc :text (.. % -target -value))}]])
  (r/atom {:text nil})
  {:inspect-data true
   :frame        false
   :header       false})


(def options [{:value 1
               :label "ONE"}
              {:value 2
               :label "TWO"}
              {:value 3
               :label "THREE"}])


(defcard-rg select
  "
<br>
<hr>
<br>
## Select / Dropdown
The `select` component is used with `select-option` components as children to create a dropdown. This input will take up 100% of the width of it's container.
<br>
<br>
`select` takes `:value` and an `:on-change` (`(function [val])`) props, and optionally a `:placeholder` (`string`) prop to create a placeholder but unselectable option.
<br>
`select-option` will need a `:value` prop and a `label`.
<br>
<br>
```clojure
[form/select
 {:value       (:option @data)
  :on-change   #(swap! data assoc :option (.. % -target -value))
  :placeholder \"Select an option\"}
 [form/select-option
  {:value 1}
  \"ONE\"]
 [form/select-option
  {:value 2}
  \"TWO\"]
 [form/select-option
  {:value 3}
  \"THREE\"]]
```
<br>
"
  (fn [data _]
    [:div {:style {:width "35vw"}}
     [form/select
      {:value       (:option @data)
       :on-change   #(swap! data assoc :option (.. % -target -value))
       :placeholder "Select an option"}
      (map
       (fn [{:keys [value label]}]
         ^{:key value}
         [form/select-option {:value value} label])
       options)]])
  (r/atom {:option nil})
  {:inspect-data true
   :frame        false
   :header       false})


(defcard-rg checkbox
  "
<br>
<hr>
<br>
## Checkboxes
The `checkbox` component can be used individually or as children in `checkbox-group` for multiple selections.
<br>
<br>
### Single Checkboxes
<br>
`checkbox` can be used individually (ie `Terms and Conditions`). You will need a `:checked` (`boolean`) and an `:on-change` (`(funtion [val])`) prop for this component.
<br>
<br>
```clojure
[form/checkbox
 {:checked   (:terms @data)
  :on-change #(swap! data assoc :terms (.. % -target -checked))}
 \"I have read and agree to the Terms and Conditions.\"]
```
<br>
"
  (fn [data _]
    [form/checkbox
     {:checked   (:terms @data)
      :on-change #(swap! data assoc :terms (.. % -target -checked))}
     "I have read and agree to the Terms and Conditions."])
  (r/atom {:terms true})
  {:inspect-data true
   :frame        false
   :header       false})


(defn- update-selection [coll value checked]
  (cond
    (and checked (nil? (some #(= % value) coll))) (conj coll value)
    (false? checked) (remove #(= % value) coll)
    :else coll))


(defcard-rg checkbox
  "
<br>
### Checkbox group
<br>
`checkbox-group` is used with `checkbox` as children.
You will need a `:checked` (`boolean`) and an `:on-change` (`(funtion [val])`) prop for this component.
<br>
<br>
```clojure
[form/checkbox
 {:checked   (:terms @data)
  :on-change #(swap! data assoc :terms (.. % -target -checked))}
 \"I have read and agree to the Terms and Conditions.\"]
```
<br>
"
  (fn [data _]
    [:div
     [:p "Select animals you like."]
     [form/checkbox-group
      {:value     (:selected @data)
       :on-change #(swap! data update :selected update-selection (.. % -target -value) (.. % -target -checked))}
     [form/checkbox
      {:value :cat}
      "Cat"]
      [form/checkbox
       {:value :dog}
       "Dog"]]])
  (r/atom {:selected []})
  {:inspect-data true
   :frame        false
   :header       false})
