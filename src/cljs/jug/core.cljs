(ns jug.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.reader :as reader]))

(defn home-page []
  [:h1 "hello"])

(defn mount-root []
  (reagent/render [home-page]
                  (.getElementById js/document "app")))

(defn init! []
  (mount-root))

