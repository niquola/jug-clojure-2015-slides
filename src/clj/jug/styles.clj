(ns jug.styles
  (:require [garden.core :refer [css]]
            [bansai :as b]
            [jug.channel :as jch]
            [garden.units :as u :refer [px pt em]]
            [garden.stylesheet :refer  [at-media]]))

(def enums {:colors {:green "#62B032"
                     :white "#fff"
                     :blue "#5980D8"}})

(defn bg [s clr]
  (merge s
         {:background-color (get-in enums [:colors clr])}))

(defn color [s clr]
  (merge s
         {:color (get-in enums [:colors clr])}))

(def padded {:padding (px 5)})
(def marged {:margin (px 5)})
(def x-padded {:padding (px 10)})
(def x-marged {:margin-top (px 20)})
(def block {:display "block"})
(def full-width {:width "100%"})
(def bordered {:border "1px solid #ddd"})
(def no-shadow {:box-shadow "none"})
(def no-radius {:border-radius "0 !important"})

(def menu [:.menu [x-padded [bg :blue] [color :white]]])

(def form [:.expressions [x-marged]
           [:input [{:position "relative" :top (px 1)}]]
           [:.form-control [no-radius block padded full-width]]
           [:.btn [no-radius full-width]]])

(def style [:.wrap [] menu form])

(defn get-style [] (css (b/dsl->garden [:body [] style])))

(defn push-changes []
  (jch/notify [:css (get-style)]))

(push-changes)
