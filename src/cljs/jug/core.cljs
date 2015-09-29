(ns jug.core
    (:require [reagent.core :as reagent :refer [atom]]
              [jug.utils :as u]
              [cljs.reader :as reader]))

(defonce state (atom {:form {:sender "nicola"
                         :expr "(+ 1 1)"}
                  :messages [{:id 1 :ts (js/Date.) :sender "igor" :expr "1" :result "1"}]}))

(defonce socket (atom nil))

(def encode pr-str)
(def decode reader/read-string)

(defn log [lbl & x] (.log js/console lbl (pr-str x)))

(defn send [msg]
  (when-let [ws @socket]
    (.send ws (encode msg))))

(defn reload-css [css]
  (println "Reload css" css)
  (aset (.getElementById js/document "stylo") "innerHTML" css))

(defn on-receive [data]
  (let [[ev msg] (decode data)]
    (log "Data from ws:" ev msg)
    (cond
      (= :css ev)    (reload-css msg)
      (= :result ev) (swap! state update-in [:messages] conj msg))))

(defn init-socket []
  (let [l (.-location js/window)
        url (str "ws://" (.-hostname l) ":" (.-port l) "/repl")
        ws (js/WebSocket. url)]
    (.log js/console ws)
    (reset! socket ws)
    (aset ws "onmessage" (fn [ev] (on-receive (.-data ev))))
    (aset ws "onclose" init-socket)))


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
  (send (merge data {:id (gensym)  :result "???"}))
  (swap! state update-in [:form] merge {:expr ""}))


(defn home-page []
  [:div.wrap
   [:div.menu "jug-repl"]
   [:div.container
    [:form.expressions {:on-submit (on-submit [:form] send-message)}
     [:input.form-control (bind [:form :sender] {:placeholder "name"}) ]
     [:textarea.form-control.input (bind [:form :expr] {:placeholder "expr" :required true}) ]
     [:button.btn.btn-success {:type "submit"} "submit"]]
    [:hr]
    [:div.messages
     (for [m (:messages @state)]
       [:pre.messages {:key (:id m)}
        [:b (:sender m)] " " (u/format-date :SHORT_TIME (:ts m)) ":  " (:expr m) " => " (:result m)])]]])

(defn mount-root []
  (reagent/render [home-page]
                  (.getElementById js/document "app")))

(defn init! []
  (init-socket)
  (mount-root))
