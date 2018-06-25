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


(defcard-rg buttons
  "
## Buttons
<br>
We have designed 3 different `button`s to be used throughout our applications. Select one of these depending on the usecase and design needs.
<br>
<br>
Buttons must have a label and an `:on-click` prop. They can also optionally take a `:disabled` (`true` | `false`) and a `:loading` (`true` | `false`).
<br>
<hr>
### Primary Buttons
`primary` buttons is our default button, used for the main call to action. There is usually only one on any page.
<br>
<br>
```clojure
[button/primary
 {:on-click (fn [] some-function)
  :loading  false
  :disabled false}
 \"button label\"]
```
<br>
"
  (fn []
    [:div
     [:span.mr3
      [button/primary
       {:on-click #(.log js/console "click")}
       "primary"]]
     [:span.mr3
      [button/primary
       {:on-click #(.log js/console "click")
        :loading  true}
       "loading"]]
     [:span.mr3
      [button/primary
       {:on-click #(.log js/console "click")
        :disabled true}
       "disabled"]]])
  nil
  {:heading false
   :frame   false})


(defcard-rg buttons
  "
<br>
<hr>
### Secondary Buttons
`secondary` buttons are used for the secondary call to action. There can be multiple per page. It is usually coupled with a primary button, but can be used alone if a layout needs a subtle main CTA.
<br>
<br>
```clojure
[button/secondary
 {:on-click (fn [] some-function)
  :loading  false
  :disabled false}
 \"button label\"]
```
<br>
"
  (fn []
    [:div
     [:span.mr3
      [button/secondary
       {:on-click #(.log js/console "click")}
       "primary"]]
     [:span.mr3
      [button/secondary
       {:on-click #(.log js/console "click")
        :loading  true}
       "loading"]]
     [:span.mr3
      [button/secondary
       {:on-click #(.log js/console "click")
        :disabled true}
       "disabled"]]])
  nil
  {:heading false
   :frame   false})


(defcard-rg buttons
  "
<br>
<hr>
### Text Buttons
`text` buttons are for use with `primary` or `secondary` buttons. It is frequently used in paired UI controls as the deprecating action, like Back/Next or Cancel/Submit.
<br>
<br>
```clojure
[button/text
 {:on-click (fn [] some-function)
  :loading  false
  :disabled false}
 \"button label\"]
```
<br>
"
  (fn []
    [:div
     [:span.mr3
      [button/text
       {:on-click #(.log js/console "click")}
       "primary"]]
     [:span.mr3
      [button/text
       {:on-click #(.log js/console "click")
        :loading  true}
       "loading"]]
     [:span.mr3
      [button/text
       {:on-click #(.log js/console "click")
        :disabled true}
       "disabled"]]])
  nil
  {:heading false
   :frame   false})


(defcard-rg buttons
  "
<br>
<hr>
## Upload button
<br>
The `upload` button triggers the native file browser on desktop. On mobile the button should allow users to take a photo with their phone's camera.
<br>
<br>
```clojure
[button/upload
 {:on-click (fn [] some-function)
  :loading  false
  :disabled false}
 \"button label\"]
```
<br>
"
  (fn []
    [:div
     [:span.mr3
      [button/upload
       {:on-click #(.log js/console "click")}
       "Upload File"]]])
  nil
  {:heading false
   :frame   false})
