(ns jug.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.reader :as reader]))

(defonce state (atom {:messages '({:expr   "(+ 1 1)"
                                  :author "me"
                                  :result "2"})
                      :form     {:author "me"
                                 :expr   "(+1 2)"}}))

(defn submit [e]
  (.preventDefault e)
  (.log  js/console e)
  (swap! state update-in [:messages] conj (:form @state)))

(defn bind [path]
  (fn [evt]
    (let [value (.-value (.-target evt))]
      (swap! state assoc-in path value))))

(defn home-page []
  (let [messages (:messages @state)]
    [:div.container
     [:pre (pr-str @state)]
     [:form {:action ""
             :method ""
             :on-submit submit}
      [:button.btn {:type "submit"} "submit"]
      [:input {:type        :text
               :placeholder "name"
               :value       (get-in @state [:form :author])
               :on-change   (bind [:form :author])}]
      [:input {:type        :text
               :placeholder "expr"
               :value       (get-in @state [:form :expr])
               :on-change   (bind [:form :expr])}]]

     [:div.messages
      (for [[i m] (map-indexed vector messages)]
        [:pre.messages {:key i}
         [:b (:author m)] ":  " (:expr m) " => " (:result m)])]]))

(defn mount-root []
  (reagent/render [home-page]
                  (.getElementById js/document "app")))

(defn init! []
  (mount-root))

