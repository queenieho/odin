(ns cards.iface.components.ptm.ui.form
  (:require-macros [devcards.core :as dc :refer [defcard
                                                 defcard-doc
                                                 defcard-rg]])

  (:require [antizer.reagent :as ant]
            [devcards.core]
            [iface.components.ptm.ui.form :as form]
            [reagent.core :as r]))


(defcard-doc
  "
# Rendering form items
")


(defn- text-input-sample [data]
  [form/text
   {:value     (:text @data)
    :on-change #(swap! data assoc :text (.. % -target -value))}])

(defcard-rg text-input
  "
## Text input
The `text` form item is used for short text inputs. This input will take up 100% of the width of it's container.
<br>
This component takes a `:value` (`string`) and an `:on-change` (`(function [val])`) props.
<br>
<br>
"
  (fn [data _]
    [:div {:style {:width "25vw"}}
     [text-input-sample data]])
  (r/atom {:text nil})
  {:inspect-data true
   :frame        false
   :header       false})


(defcard-doc
  "
#### Text Input Sample Code"
  (dc/mkdn-pprint-source text-input-sample))


(defn- number-input-sample [data]
  [form/number
   {:value     (:num @data)
    :step      10
    :on-change #(swap! data assoc :num (.. % -target -value))
    :min       5
    :max       65}])


(defcard-rg number-input
  "
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
"
  (fn [data _]
    [:div {:style {:width "15vw"}}
     [number-input-sample data]])
  (r/atom {:num nil})
  {:inspect-data true
   :frame        false
   :header       false})


(defcard-doc
  "
#### Number Input Sample Code"
  (dc/mkdn-pprint-source number-input-sample))


(defn- textarea-input-sample [data]
  [form/textarea
   {:value     (:text @data)
    :rows      5
    :on-change #(swap! data assoc :text (.. % -target -value))}])

(defcard-rg textarea
  "
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
"
  (fn [data _]
    [:div {:style {:width "35vw"}}
     [textarea-input-sample data]])
  (r/atom {:text nil})
  {:inspect-data true
   :frame        false
   :header       false})


(defcard-doc
  "
#### Textarea Input Sample Code"
  (dc/mkdn-pprint-source textarea-input-sample))


;; select input =========================


(defn- select-input-sample [data]
  (let [options [{:value 1
                  :label "ONE"}
                 {:value 2
                  :label "TWO"}
                 {:value 3
                  :label "THREE"}]]
    [form/select
     {:value       (:option @data)
      :on-change   #(swap! data assoc :option (.. % -target -value))
      :placeholder "Select an option"}
     (map
      (fn [{:keys [value label]}]
        ^{:key value}
        [form/select-option {:value value} label])
      options)]))


(defcard-rg select
  "
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
"
  (fn [data _]
    [:div {:style {:width "35vw"}}
     [select-input-sample data]])
  (r/atom {:option nil})
  {:inspect-data true
   :frame        false
   :header       false})


(defcard-doc
  "
#### Select / Dropdown Input Sample Code"
  (dc/mkdn-pprint-source select-input-sample))


;; checkbox =============================


(defn- checkbox-input-sample [data]
  [form/checkbox
   {:value     "terms-and-conditions"
    :checked   (:terms @data)
    :on-change #(swap! data assoc :terms (.. % -target -checked))}
   "I have read and agree to the Terms and Conditions."])


(defcard-rg checkbox
  "
<hr>
<br>
## Checkboxes
The `checkbox` component can be used individually or as children in `checkbox-group` for multiple selections.
<br>
<br>
### Single Checkboxes
`checkbox` can be used individually (ie `Terms and Conditions`). You will need a `:checked` (`boolean`) and an `:on-change` (`(funtion [val])`) prop for this component.
<br>
<br>
"
  (fn [data _]
    [checkbox-input-sample data])
  (r/atom {:terms true})
  {:inspect-data true
   :frame        false
   :header       false})


(defcard-doc
  "
#### Single Checkbox Input Sample Code"
  (dc/mkdn-pprint-source checkbox-input-sample))


;; checkbox group =======================


(defn- update-selection [coll value checked]
  (cond
    (and checked (nil? (some #(= % value) coll))) (conj coll value)
    (false? checked) (remove #(= % value) coll)
    :else coll))


(defn- checkbox-group-sample [data]
  [:div
   [:p "Select all the animals you like."]
   [form/checkbox-group
    {:value     (:selected @data)
     :on-change #(swap! data update :selected update-selection (.. % -target -value) (.. % -target -checked))}
    [form/checkbox
     {:value "cat"}
     "Cat"]
    [form/checkbox
     {:value "dog"}
     "Dog"]]])


(defcard-rg checkbox
  "
<hr>
<br>
### Checkbox group
<br>
`checkbox-group` is used with `checkbox` as children.
A `checkbox-group` takes a `:value` (`list`) and an `:on-change` (`(funtion [val])`), while `checkbox` only needs a `:value` prop.
<br>
<br>
"
  (fn [data _]
    [checkbox-group-sample data])
  (r/atom {:selected ["cat"]})
  {:inspect-data true
   :frame        false
   :header       false})


(defcard-doc
  "
#### Checkbox Group Sample Code"
  (dc/mkdn-pprint-source checkbox-group-sample))


(defn- radio-group-component [data]
  (let [options [{:value "paris"
                  :label "Paris"}
                 {:value "ny"
                  :label "New York"}
                 {:value "la"
                  :label "Los Angeles"}]]
    [form/radio-group
     {:value     (:selected @data)
      :on-change #(swap! data assoc :selected (.. % -target -value))}
     (map
      (fn [{:keys [value label]}]
        [form/radio-option
         {:value value}
         label])
      options)]))


(defcard-rg radio-group
  "
<hr>
<br>
## Radio Group
A `radio-group` can be created with multiple `radio-option`s as children.
A `radio-group` takes a `:value` (`list`) and an `:on-change` (`(funtion [val])`), while `radio-option` only needs a `:value` prop.
<br>
<br>
"
  (fn [data _]
    [radio-group-component data])
  (r/atom {:selected "la"})
  {:inspect-data true
   :frame        false
   :header       false})


(defcard-doc
  "
#### Radio Group Sample Code"
  (dc/mkdn-pprint-source radio-group-component))


(defn- date-input-component [data]
  [form/inline-date {:value         (:selected @data)
                     :on-day-click  #(when-not (.. %2 -disabled)
                                       (if (.. %2 -selected)
                                         (swap! data dissoc :selected)
                                         (swap! data assoc :selected %)))
                     :disabled      {:before   (js/Date.)
                                     :after    #inst "2018-10-20"
                                     :weekdays [0 6]}
                     :show-from     (js/Date.)
                     :initial-month #inst "2018-09-02"
                     :change-month  true
                     :fixed-height  true
                     :today-btn     true}])


(defcard-rg date-input
  "
<hr>
<br>
## Inline date picker
`inline-date` allows a user to pick a date out of a calendar.
<br>
<br>
Props used for this component:
<br>
`:value` stores the value of the selected date. It can be a single `instant` or a `list` of `instant`s. <br>
`:on-day-click` event handler when a user clicks on a date in the following shape `(fn [val {:disabled _ :selected _}])`.<br>
`:disabled` map to indicate disabled dates with the following keys `:before [Instant]`, `:after [Instant]`, `:weekdays (list of weekday indexes)`.<br>
`:show-from` takes an `Instant` to indicate which month to show a caledar from. If not specified the user will be able to go back indefinitely.<br>
`:initial-month` takes an `Instant` that represents the month that is rendered initially. Defaults to current date.<br>
`:change-month` defaults to `true`. Allows user to cycle through different months in the calendar.<br>
`:fixed-height` defaults to `true`. Keeps the calendar UI showing 6 weeks at a time. Out of month days are shown as disabled.<br>
`:today-btn` defaults to `false`. When `true` shows a `Today` button that brings user back to the current date.<br>
<br>
<br>
"
  (fn [data _]
    [date-input-component data])
  (r/atom {:selected nil})
  {:inspect-data true
   :frame        false
   :header       false})


(defcard-doc
  "
#### Date Input Sample Code"
  (dc/mkdn-pprint-source date-input-component))
