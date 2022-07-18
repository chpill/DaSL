(ns dasl.core-test
  (:require
    [clojure.test :refer [deftest is]]
    [dasl.core :as dasl]))


(def datomic-doc-tuple-example-vanilla-schema
  "Interesting link to the schema grammar for datomic cloud
   https://docs.datomic.com/cloud/schema/schema-reference.html#orgb6f4748"
  #{{:db/ident :student/first
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one}
    {:db/ident :student/last
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one}
    {:db/ident :student/email
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/unique :db.unique/identity}

    {:db/ident :semester/year
     :db/valueType :db.type/long
     :db/cardinality :db.cardinality/one}
    {:db/ident :semester/season
     :db/valueType :db.type/keyword
     :db/cardinality :db.cardinality/one}
    {:db/ident :semester/year+season
     :db/valueType :db.type/tuple
     :db/tupleAttrs [:semester/year :semester/season]
     :db/cardinality :db.cardinality/one
     :db/unique :db.unique/identity}

    {:db/ident :course/id
     :db/valueType :db.type/string
     :db/unique :db.unique/identity
     :db/cardinality :db.cardinality/one}
    {:db/ident :course/name
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one}

    {:db/ident :reg/course
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/one}
    {:db/ident :reg/semester
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/one}
    {:db/ident :reg/student
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/one}
    {:db/ident :reg/course+semester+student
     :db/valueType :db.type/tuple
     :db/tupleAttrs [:reg/course :reg/semester :reg/student]
     :db/cardinality :db.cardinality/one
     :db/unique :db.unique/identity}})


(def datomic-doc-tuple-example-schema-with-dasl
  {:student/email [:identity :one :string]
   :student/first [:one :string]
   :student/last  [:one :string]

   :course/id   [:identity :one :string]
   :course/name [:one :string]

   :semester/year   [:one :long]
   :semester/season [:one :keyword]
   :semester/year+season [:identity :one :tuple [:semester/year :semester/season]]

   :reg/course   [:one :ref]
   :reg/semester [:one :ref]
   :reg/student  [:one :ref]
   :reg/course+semester+student [:identity :one :tuple [:reg/course :reg/semester :reg/student]]})


(deftest tuple-schema-example-from-datomic-documentation
  (is (= datomic-doc-tuple-example-vanilla-schema
         (dasl/expand datomic-doc-tuple-example-schema-with-dasl))))
