(ns jug.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [cljs.reader :as reader]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History))

;; -------------------------
;; Views

(defonce socket (atom nil))

(def messages (atom []))

(defn next-id [] (str (gensym)))

(defn get-message [msg]
  (swap! messages conj msg))

(defn send-message [txt]
  (.send @socket (str {:sender "Nicola" :content txt})))

(defn log [x]
  (.log js/console (str x)))

(defn home-page []
  [:div.container
   [:div.messages
    (for [m @messages]
      [:div.messages {:key (:id m)} 
       [:b (:sender m)]
       [:p (:content m)]])]
   [:hr]
   [:textarea.form-control
    {:on-key-up (fn [e]
                  (when (= 13 (.-which e))
                    (send-message (-> e .-target .-value))
                    (aset (-> e .-target) "value" "")))}]])

(defn about-page []
  [:div [:h2 "About jug"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))


(defn init-socket []
  (let [l (.-location js/window)
        s (js/WebSocket. (str "ws://" (.-host l) "/chat"))]
    (reset! socket s)
    (.log js/console s)
    (aset s "onmessage" (fn [x] (.log js/console x)
                          (let [msg (reader/read-string (.-data x))]
                            (log msg)
                            (get-message msg))))))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (init-socket)
  (mount-root))
