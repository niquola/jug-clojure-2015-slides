(ns jug.core-ready
    (:require [reagent.core :as reagent :refer [atom]]
              [cljs.reader :as reader]))

(def state (atom {:form {:sender "nicola"
                         :expr "(+ 1 1)"}
                  :messages [{:id 1 :sender "igor" :expr "1" :result "1"}]}))

(defonce socket (atom nil))

(def encode pr-str)
(def decode reader/read-string)

(defn log [lbl x] (.log js/console lbl (pr-str x)))

(defn send [msg]
  (when-let [ws @socket]
    (.send ws (encode msg))))

(defn on-receive [data]
  (let [msg (decode data)]
    (log "Data from ws:" msg)
    (swap! state update-in [:messages] into msg)))

(defn init-socket []
  (let [l (.-location js/window)
        url (str "ws://" (.-hostname l) ":" (.-port l) "/repl")
        ws (js/WebSocket. url)]
    (.log js/console ws)
    (reset! socket ws)
    (aset ws "onmessage" (fn [ev] (on-receive (.-data ev))))
    (aset ws "onclose" init-socket)))

(defn on-submit [pth f]
  (fn [ev] (.preventDefault ev)
    (f (get-in @state pth))))

(defn target-value [ev]
  (-> ev .-target .-value))

(defn bind [pth opts]
  (merge opts {:on-change #(swap! state update-in pth (constantly (target-value %)))
               :value (get-in @state pth)}))

(defn send-message [data]
  (send (merge data {:id (gensym)  :result "???"}))
  (swap! state update-in [:form] merge {:expr ""}))


(defn home-page []
  [:div.container
   [:p "jug-repl"]
   #_[:pre (pr-str @state)]
   [:form {:on-submit (on-submit [:form] send-message)}
    [:input (bind [:form :sender] {:placeholder "name"}) ]
    [:input (bind [:form :expr] {:placeholder "expr" :required true}) ]
    [:button.btn {:type "submit"} "submit"]]
   [:hr]
   [:div.messages
    (for [m (:messages @state)]
      [:pre.messages {:key (:id m)}
       [:b (:sender m)] ":  " (:expr m) " => " (:result m)])]])

(defn mount-root []
  (reagent/render [home-page]
                  (.getElementById js/document "app")))

(defn init! []
  (init-socket)
  (mount-root))
