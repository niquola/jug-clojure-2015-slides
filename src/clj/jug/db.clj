(ns jug.db
  (:require
   [clojure.java.jdbc :as jdbc]
   [honeysql.core :as sql]))

(def db
  "postgresql://aidbox:aidbox@localhost:5432/test")

(jdbc/query db ["SELECT 1"])

(defn e! [sql] (jdbc/execute! db [sql]))
(defn q [hsql]
  (let [sql (sql/format hsql)]
    (println sql)
    (jdbc/query db sql)))

(defn migrate-up []
  (e!
   "create table messages (
     id serial primary key,
     sender text,
     content text,
     sent_at timestamp default current_timestamp
    )"))

(comment
  (e! "delete from messages")
  (migrate-up))


(defn list-messages []
  (q {:select [:*] :from [:messages] :order-by [[:sent_at]]}))

(defn create-message [m]
  (first 
   (jdbc/insert! db :messages m)))

(create-message {:sender "Nicola" :content "Hello"})

(list-messages)
