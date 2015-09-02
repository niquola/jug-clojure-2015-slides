(ns jug.web
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [org.httpkit.server :as ohs]
            [jug.db :as db]
            [cheshire.core :as json]))

(defn layout [cnt]
  (html [:html
         [:head]
         [:body cnt]]))

(defn link-to [href lbl]
  [:a {:href href} lbl])

(defn server [req]
  {:body (layout [:div.container
                  [:h3 "Hello"]
                  [:pre (pr-str (db/all))]
                  (link-to "/repl" "repl")])
   :status 200})


(comment
  (def stop
    (ohs/run-server #'server {:port 3000}))

  (stop)
  )
