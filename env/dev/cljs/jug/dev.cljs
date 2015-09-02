(ns ^:figwheel-no-load jug.dev
  (:require [jug.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(def ws
  (let [l (.-location js/window)]
    (str "ws://" (.-hostname l) ":3001/figwheel-ws")))

(.log js/console ws)

(figwheel/watch-and-reload
  :websocket-url ws 
  :jsload-callback core/mount-root)

(core/init!)
