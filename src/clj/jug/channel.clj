(ns jug.channel
  (:require 
            [org.httpkit.server :as ohs]
            [cheshire.core :as json]
            [jug.db :as db]
            [org.httpkit.server :as ohs]
            [clojure.stacktrace :as trace]
            [environ.core :refer [env]]))


(def clients (atom #{}))

(def messages (atom []))

(defn on-close [ch] (swap! clients disj ch))

(defn decode [x] (read-string x))

(defn encode [x] (pr-str x))

(defn on-connect [ch]
  (swap! clients conj ch)
  (ohs/send! ch (encode @messages)))

(defn notify [msg]
  (let [data (encode msg)]
    (doseq [ch @clients]
      (println "Send to " ch " " data)
      (try
        (ohs/send! ch data)
        (catch Exception e
          (println (pr-str e)))))))

(defn repl-eval [s]
  (with-out-str
    (try (println (eval
                   (read-string s)))
         (catch Exception e
           (println (trace/print-stack-trace e))))))

(defn process-msg [msg]
  (merge msg
         {:ts (java.util.Date.)
          :result (repl-eval (:expr msg))}))

(defn on-message [data]
  (let [msg (process-msg (decode data))]
    (swap! messages conj msg)
    (notify [:result msg])))

(defn repl [req]
  (ohs/with-channel req ch
    (on-connect ch)
    (ohs/on-close ch (fn [_] (on-close ch)))
    (ohs/on-receive ch (fn [data]
                         (println "receive:" data)
                         (on-message data)))))
