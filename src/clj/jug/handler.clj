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
     (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
    [:body
     [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]]
     (include-js "js/app.js")]]))

(defn repl-eval [s]
  (with-out-str
    (try (println (eval
                   (read-string s)))
         (catch Exception e
           (println (trace/print-stack-trace e))))))


(def clients (atom #{}))

(def messages (atom []))


(defn on-close [ch]
  (swap! clients disj ch))

(defn decode [x] (read-string x))

(defn encode [x] (pr-str x))

(defn on-connect [ch]
  (swap! clients conj ch)
  (ohs/send! ch (encode @messages)))

(defn broad-cast [msg]
  (let [data (encode msg)]
    (doseq [ch @clients]
      (println "Send to " ch " " data)
      (try
        (ohs/send! ch data)
        (catch Exception e
          (println (pr-str e)))))))

(defn process-msg [msg]
  (assoc msg :result (repl-eval (:expr msg))))

(defn on-message [data]
  (let [msg (process-msg (decode data))]
    (swap! messages conj msg)
    (broad-cast [msg])))

(defn repl [req]
  (ohs/with-channel req ch
    (on-connect ch)
    (ohs/on-close ch (fn [_] (on-close ch)))
    (ohs/on-receive ch (fn [data]
                         (println "receive:" data)
                         (on-message data)))))

(defroutes routes
  (GET "/" [] home-page)
  (GET "/repl" [] #'repl)
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
