(ns odin.utils.formatters
  (:require [odin.l10n :as l10n]
            [toolbelt.core :as tb]
            [i18n.phonenumbers.PhoneNumberUtil :as pnu]
            [i18n.phonenumbers.PhoneNumberFormat :as pnf]))

;; TIMES
(defn moment [arg] (.moment js/window arg))

(defn date-short
  "Short date (e.g. Jan 1, 2017)"
  [thing]
  (if thing (-> (moment thing)
                (.format (l10n/translate :tongue/format-date-short)))))


(defn date-time
  "Date and time (e.g. January 1, 2017 @ 12:00 pm)"
  [thing]
  (if thing (-> (moment thing)
                (.format (l10n/translate :tongue/format-date-short)))))


(defn date-words
  "Verbose date (e.g. Today at 12:00pm)"
  [thing]
  (if thing (-> (moment thing)
                (.calendar))))

(defn phone-number
  "Uses Google's libphonenumber to format the provided phone number."
  [number]
  (let [pu        (pnu/getInstance)
        num       number
        country   "US"
        parsed    (.parse pu num country)
        formatted (.format pu parsed pnf/NATIONAL)]
    (str formatted)))


(defn email-link
  "Returns an <a> element linking to the specified email address."
  [email]
  [:a
   {:href (str "mailto:" email)}
   email])


; This pipes through Tongue, so it's automatically internationalized
(defn number
  "Accepts a number, and returns a formatted string according to current language. e.g. [12345] -> '12,345'"
  [amount]
  (l10n/translate :tongue/format-number amount))


(defn currency
  "Accepts a number, and returns a formatted currency amount according to current language. e.g. [1999.99] -> '$1,999.99'"
  [amount]
  (str "$ " (number amount)))
