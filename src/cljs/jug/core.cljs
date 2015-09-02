(ns jug.core
    (:require [reagent.core :as reagent :refer [atom]]
              [cljs.reader :as reader]))

(def state (atom {:form {:name "nicola"
                         :expr "(+ 1 1)"}
                  :messages [{:id 1 :sender "igor" :expr "1" :result "1"}]}))

(defn mk-setter [pth]
  (fn [v] (swap! state update-in pth v)))

(defn on-submit [pth f]
  (fn [ev] (.preventDefault ev)
    (f (get-in @state pth))))

(defn target-value [ev]
  (-> ev .-target .-value))

(defn bind [pth opts]
  (merge opts {:on-change #(swap! state update-in pth (constantly (target-value %)))
               :value (get-in @state pth)}))

(defn send-message [data]
  (->> [(merge data {:id (gensym) :sender "???" :result "???"})]
       (swap! state update-in [:messages] into))
  (swap! state update-in [:form] merge {:expr ""}))


(defn home-page []
  [:div.container
   [:p "jug-repl"]
   #_[:pre (pr-str @state)]
   [:form {:on-submit (on-submit [:form] send-message)}
    [:input (bind [:form :name] {:placeholder "name"}) ]
    [:input (bind [:form :expr] {:placeholder "expr"}) ]
    [:button.btn {:type "submit"} "submit"]]
   [:hr]
   [:div.messages
    (for [m (:messages @state)]
      [:div.messages {:key (:id m)}
       [:pre.col-md-8 [:b (:sender m)] (:expr m)]
       [:pre.col-md-4.res (:result m)]])]])

(defn mount-root []
  (reagent/render [home-page]
                  (.getElementById js/document "app")))

(defn init! []
  (mount-root))
