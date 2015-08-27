(ns jug.server
  (:require [jug.handler :refer [app]]
            [environ.core :refer [env]]
            [org.httpkit.server :as ohs]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn start []
  (def stop
    (ohs/run-server #'app {:port 3000})))

(comment
  (stop)
  (start)

  (require '[vinyasa.pull :as vp])
 )
