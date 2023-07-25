(ns dasl.datomic-test
  (:require  [clojure.test :refer [deftest is use-fixtures]]
             [dasl.core :as dasl]
             [dasl.core-test]
             [datomic.api :as d]
             [datomock.core :as datomock]))



(defn fresh-database []
  (let [db-uri (str "datomic:mem://" (gensym))]
    (d/create-database db-uri)
    (d/connect db-uri)))

(def ^:dynamic *base-conn* nil)
(def ^:dynamic *conn* nil)

(defn create-db-conn-fixture [f]
  (println "creating fixture database connection")
  (binding [*base-conn* (fresh-database)]
    (f)))

(defn fork-db-conn-fixture [f]
  (println "forking database connection")
  (binding [*conn* (datomock/fork-conn *base-conn*)]
    (f)))


;; **NB** try out functions before making them test fixtures. The fixture
;; mechanism seems to be swallowing errors :/
(use-fixtures :once create-db-conn-fixture)
(use-fixtures :each fork-db-conn-fixture)



(deftest datomic-accepts-attributes
  #_@(d/transact *conn*
               dasl.core-test/datomic-doc-tuple-example-vanilla-schema)
  @(d/transact *conn*
               (dasl/expand dasl.core-test/datomic-doc-tuple-example-schema-with-dasl)))



