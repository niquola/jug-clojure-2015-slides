(ns jug.db
  (:require
   [clojure.java.jdbc :as jdbc]
   [honeysql.core :as sql]))

(def db "postgresql://aidbox:aidbox@localhost:5432/test")

(defn q [s]
  (jdbc/query db s))

(defn e! [s]
  (jdbc/execute! db [s]))

(defn hq [hs]
  (q (sql/format hs)))

(defn migrate-up []
  (e! "
   create table repl
   (id serial primary key,
    expr text,
    user_name text,
    created_at timestamp default current_timestamp)
"))

(defn migrate-down! []
  (e! "drop table repl"))

(defn recreate []
  (migrate-down!)
  (migrate-up))

(recreate)


(defn create [msg]
  (first (jdbc/insert! db :repl msg)))

(defn all []
  (hq {:select [:*] :from [:repl]}))


(comment
  (require '[clojure.test :as t])

  (t/deftest simple-test
    (do
      (recreate)
      (create {:user_name "nicola" :expr "(+ 1 1)"})
      (t/is (= 2 (count (all)))))))



