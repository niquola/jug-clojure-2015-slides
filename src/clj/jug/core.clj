(ns jug.core
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [org.httpkit.server :as ohs]
            [jug.styles :as js]
            [cheshire.core :as json]
            [jug.channel :as jc]
            [org.httpkit.server :as ohs]
            [clojure.stacktrace :as trace]
            [environ.core :refer [env]]))

(def home-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css")
     [:style#stylo (js/get-style)]]
    [:body
     [:div#app "..."]
     (include-js "js/app.js")]]))

(defroutes routes
  (GET "/" [] home-page)
  (GET "/repl" [] #'jc/repl)
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults #'routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))

(defn start []
  (def stop
    (ohs/run-server #'app {:port 3000})))

(comment
  (stop)
  (start)
  (require '[vinyasa.pull :as vp]))
