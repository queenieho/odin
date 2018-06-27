(ns cards.iface.components.ptm.ui.modal
  (:require-macros [devcards.core :as dc :refer [defcard
                                                 defcard-doc
                                                 defcard-rg]])

  (:require [devcards.core]
            [iface.components.ptm.ui.modal :as modal]
            [iface.components.ptm.ui.button :as button]
            [reagent.core :as r]
            [antizer.reagent :as ant]
            [toolbelt.core :as tb]))


(defcard-doc
  "
# Rendering modals
")


(defn- update-selected [coll v]
  (if (some #(= v %) coll)
    (remove #(= v %) coll)
    (conj coll v)))


(def communities
  [{:value            "414bryant"
    :images           ["http://placekitten.com/1600/1600"
                       "http://placekitten.com/1600/1400"
                       "http://placekitten.com/1600/1500"
                       "http://placekitten.com/1600/1700"]
    :name             "SoMa South Park"
    :price            "From $2000"
    :units-available  18
    :intro            "Join our community in sunny Downtown Oakland, an eclectic and diverse neighborhood steps from the business district, nightlife, shops, and transportation. Just a short bike ride or drive and you can enjoy a picnic at Lake Merritt, a stroll through the Oakland Musuem of California, or kick back to watch a flick on a couch at The New Parkway."
    :building-details "This large historic building has 7 floors, each boasting 2,300 square feet. There are 122 total units. The architecture is blah blah more details about the building."
    :neighborhood     "Downtown Oakland, unlike others, is not just a central business disctrict. It is the cross-section of culture and communities. Lorem ipsum dolor sit amet. Just a stones’ throw away you’ll find your next favorite dive bar, cocktail, or brunch spot. Lorem ipsum dolor sit amet."
    :community-desc   "You’ll be joining a diverse, low-key community who takes pride in representing Oakland and spreading the Town’s unique cultural legacy. We are generous in spirit and welcome people from all walks of life to live with us. You’ll frequently find the common spaces occupied with quiet studiers during the day and people cooking and watching TV at night, with the occasional fun event or craft night."
    :amenities        [{:label "Private, furnished bedroom"
                        :img   "/assets/images/ptm/icons/ic-atom.svg"}
                       {:label "High-speed wifi"
                        :img   "/assets/images/ptm/icons/ic-atom.svg"}
                       {:label "All utilities included"
                        :img   "/assets/images/ptm/icons/ic-atom.svg"}
                       {:label "Bike storage"
                        :img   "/assets/images/ptm/icons/ic-atom.svg"}
                       {:label "Shared media library"
                        :img   "/assets/images/ptm/icons/ic-atom.svg"}]}

   {:value            "2072mission"
    :images           ["http://placekitten.com/1600/1600"
                       "http://placekitten.com/1600/1400"
                       "http://placekitten.com/1600/1500"
                       "http://placekitten.com/1600/1700"]
    :name             "Mission"
    :price            "From $1400"
    :units-available  5
    :intro            "Join our community in The Mission District, an eclectic and diverse neighborhood steps from the business district, nightlife, shops, and transportation. Just a short bike ride or drive and you can enjoy a picnic at Lake Merritt, a stroll through the Oakland Musuem of California, or kick back to watch a flick on a couch at The New Parkway."
    :building-details "This medium building has 2 floors, each boasting 2,300 square feet. There are 122 total units. The architecture is blah blah more details about the building."
    :neighborhood     "Something cool about The Mission having lots of arts and mexican food."
    :community-desc   "We party hard and yoga."
    :amenities        [{:label "Private, furnished bedroom"
                        :img   "/assets/images/ptm/icons/ic-atom.svg"}
                       {:label "High-speed wifi"
                        :img   "/assets/images/ptm/icons/ic-atom.svg"}
                       {:label "All utilities included"
                        :img   "/assets/images/ptm/icons/ic-atom.svg"}
                       {:label "Bike storage"
                        :img   "/assets/images/ptm/icons/ic-atom.svg"}
                       {:label "Dog friendly. Woof!"
                        :img   "/assets/images/ptm/icons/ic-atom.svg"}]}])


(defcard-rg modal
  "
## Community modal
`community` modals are used to show detailed information about a community.
<br>
<br>
Here's a list of the needed props to use this component:
<br>
`:visible` (`true` | `false`) - to indicate visibility of the modal.
<br>
`:on-close` (function) - Function that will be called when the user clicks on the `close` icon.
<br>
`:value` (string | int) - unique value tied to this modal.
<br>
`:images` (collection of image sources) - For carrousel of photos.
<br>
`:name` (string) - Name of community.
<br>
`:price` (string) - Price or price range of community.
<br>
`:units-available` (int) - amount of units available.
<br>
`:intro` (string) - brief introduction to the community.
<br>
`:building-details` (string) - Description of the building.
<br>
`:neighborhood` (string) - Description of the neighborhood.
<br>
`:community-desc` (string) - Description of the community itself. To get a feel of the type of people living there.
<br>
`:amenities` (list of {:label \"Amenity name\", :img \"image source\"}) - For listing the amenities available in this community
<br>
`:on-select` (function [value]) - Function that will be called when a user selects this community.
<br>
`:on-next` (function ) - Function that will be called when a user hits `next`.
<br>
`:next` (community) - Community that will be showed if the `next` button is clicked.
<br>
`:selected` (`true` | `false`) - Indicated if the current community is selected.
<br>
<br>
```clojure
[modal/community
 {:visible (:show-modal @data)
  :on-close #(swap! data update :show-modal not)
  :value            \"414bryant\"
  :images           [\"http://placekitten.com/1600/1600\"
                     \"http://placekitten.com/1600/1400\"
                     \"http://placekitten.com/1600/1500\"
                     \"http://placekitten.com/1600/1700\"]
  :name             \"SoMa South Park\"
  :price            \"From $2000\"
  :units-available  18
  :intro            \"Join our community in sunny Downtown Oakland, an eclectic and diverse neighborhood steps from the business district, nightlife, shops, and transportation. Just a short bike ride or drive and you can enjoy a picnic at Lake Merritt, a stroll through the Oakland Musuem of California, or kick back to watch a flick on a couch at The New Parkway.\"
  :building-details \"This large historic building has 7 floors, each boasting 2,300 square feet. There are 122 total units. The architecture is blah blah more details about the building.\"
  :neighborhood     \"Downtown Oakland, unlike others, is not just a central business disctrict. It is the cross-section of culture and communities. Lorem ipsum dolor sit amet. Just a stones’ throw away you’ll find your next favorite dive bar, cocktail, or brunch spot. Lorem ipsum dolor sit amet.\"
  :community-desc   \"You’ll be joining a diverse, low-key community who takes pride in representing Oakland and spreading the Town’s unique cultural legacy. We are generous in spirit and welcome people from all walks of life to live with us. You’ll frequently find the common spaces occupied with quiet studiers during the day and people cooking and watching TV at night, with the occasional fun event or craft night.\"
  :amenities        [{:label \"Private, furnished bedroom\"
                      :img   \"/assets/images/ptm/icons/ic-atom.svg\"}
                     {:label \"High-speed wifi\"
                      :img   \"/assets/images/ptm/icons/ic-atom.svg\"}
                     {:label \"All utilities included\"
                      :img   \"/assets/images/ptm/icons/ic-atom.svg\"}
                     {:label \"Bike storage\"
                      :img   \"/assets/images/ptm/icons/ic-atom.svg\"}
                     {:label \"Shared media library\"
                      :img   \"/assets/images/ptm/icons/ic-atom.svg\"}]
  :on-select #(swap! data update :selected update-selected %)
  :on-next #(swap! community assoc :info (second communities))
  :next {[information for next community]}
  :selected (some
             #(= % (:value (:info @community)))
             (:selected @data))}]
```
<br>

"
  (fn [data _]
    (let [community (r/atom {:info (first communities)})]
      (fn []
        [:div
         [modal/community
          (tb/assoc-when
           (:info @community)
           :on-select #(swap! data update :selected update-selected %)
           :on-next #(swap! community assoc :info (second communities))
           :next (second communities)
           :selected (some
                      #(= % (:value (:info @community)))
                      (:selected @data))
           :visible (:show-modal @data)
           :on-close #(swap! data update :show-modal not))]
         [button/primary
          {:on-click #(swap! data update :show-modal not)}
          "open modal"]])))
  (r/atom {:show-modal false
           :selected   []})
  {:inspect-data true
   :heading      false
   :frame        false})
