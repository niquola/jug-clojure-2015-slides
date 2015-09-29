(ns jug.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [org.httpkit.server :as ohs]
            [cheshire.core :as json]
            [clojure.stacktrace :as trace]
            [environ.core :refer [env]]))
(defroutes routes
  (resources "/")
  (not-found "Not Found"))

(def app
  (-> #'routes
      (wrap-defaults site-defaults)
      wrap-exceptions
      wrap-reload))


(defn start []
  (def stop
    (ohs/run-server #'app {:port 3000})))

(comment
  (stop)
  (start)
  (require '[vinyasa.pull :as vp]))
























(comment
      (html
            [:h1 {:style {:color "white"}}"hello"]))