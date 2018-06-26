(ns cards.iface.components.ptm.ui.tag
  (:require-macros [devcards.core :as dc :refer [defcard
                                                 defcard-doc
                                                 defcard-rg]])

  (:require [devcards.core]
            [iface.components.ptm.ui.tag :as tag]
            [reagent.core :as r]
            [antizer.reagent :as ant]))


(defcard-doc
  "
# Rendering Tags
")


(defn- update-group-value [coll v]
  (if (some #(= v %) coll)
    (remove #(= v %) coll)
    (conj coll v)))


(defcard-rg tag
  "
## Tags
`tags` can be selectable, like checkboxes, or deletable.
<br>
<br>
### Selectable Tags
`group-select` with a sequence of `select` tags allows a user to select as many options from a group as they like.
<br>
<br>
`group-select` takes a `:value` and an `:on-change` prop.
<br>
<br>
```clojure
[tag/group-select
 {:value     (:selected @data)
  :on-change #(swap! data update :selected update-group-value %)}
 (map
  (fn [{:keys [value label]}]
    [tag/select
     {:value value}
     label])
  [{:value 1
    :label \"one\"}
   {:value 2
    :label \"two\"}
   {:value 3
    :label \"three\"}])]
```
<br>
"
  (fn [data _]
    (let [options [{:value 1
                    :label "one"}
                   {:value 2
                    :label "two"}
                   {:value 3
                    :label "three"}]]
      [tag/group-select
       {:value     (:selected @data)
        :on-change #(swap! data update :selected update-group-value %)}
       (map
        (fn [{:keys [value label]}]
          [tag/select
           {:value value}
           label])
        options)]))
  (r/atom {:selected []})
  {:inspect-data true
   :heading      false
   :frame        false})


(defn remove-from-coll [coll value]
  (remove #(= (:value %) value) coll))


(defcard-rg tag
  "
<br>
<hr>
<br>
### Deletable Tags
`group-delete` with a sequence of `delete` tags are used to show a collection of selected items and can be easily deletable.
<br>
<br>
`group-select` takes a `:value` and an `:on-change` prop.
<br>
<br>
```clojure
[tag/group-delete
 {:value     (:selected @data)
  :on-change #(swap! data update :selected remove-from-coll %)}
 (map
  (fn [{:keys [value label]}]
    [tag/delete
     {:value value}
     label])
 (:selected @data)]
```
<br>
"
  (fn [data _]
    [tag/group-delete
     {:value     (:selected @data)
      :on-change #(swap! data update :selected remove-from-coll %)}
     (map
      (fn [item]
        [tag/delete
         {:value (:value item)}
         (:label item)])
      (:selected @data))])
  (r/atom {:selected '({:value 1
                        :label "one"}
                       {:value 2
                        :label "two"}
                       {:value 3
                        :label "three"})})
  {:inspect-data true
   :heading      false
   :frame        false})
