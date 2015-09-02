(ns jug.db
  (:require
   [clojure.java.jdbc :as jdbc]
   [honeysql.core :as sql]))

(def db "postgresql://aidbox:aidbox@localhost:5432/test")

