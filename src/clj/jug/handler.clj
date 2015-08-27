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
            [jug.db :as db]
            [environ.core :refer [env]]))

(def home-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css")
     (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
    [:body
     [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]]
     (include-js "js/app.js")]]))

(defonce users (atom #{}))

(defn broad-cast [msg]
  (doseq [u @users]
    (ohs/send! u msg)))

@users

(broad-cast "Hello from server")

(defn to-json [m]
  (json/generate-string m))


(defn on-new-user [ch]
  (swap! users conj ch)
  (doseq [m (db/list-messages)] 
    (:content m)
    (ohs/send! ch (str m))))

(defn on-message [txt]
  (let [msg (read-string txt)
        msg (db/create-message msg)]
    (broad-cast (str msg))))

(comment
  (doseq [x (range 1000)]
    (broad-cast (str {:id (str "i-" x) :sender "Bot" :content x}))
    (java.lang.Thread/sleep 2)))


(defn chat [req]
  (ohs/with-channel req ch
    (on-new-user ch)
    (ohs/on-receive ch on-message)
    (ohs/on-close ch (fn [_] (swap! users disj ch)))))

(defroutes routes
  (GET "/" [] home-page)
  (GET "/chat" [] #'chat)
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults #'routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))
