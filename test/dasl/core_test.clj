(ns dasl.core-test
  (:require [dasl.core :as dasl]
            [clojure.test :refer [deftest is]]))


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
  {:student/email [:identity :string]
   :student/first [:string]
   :student/last  [:string]

   :course/id   [:identity :string]
   :course/name [:string]

   :semester/year   [:long]
   :semester/season [:keyword]
   :semester/year+season [:identity :tuple [:semester/year :semester/season]]

   :reg/course   [:ref]
   :reg/semester [:ref]
   :reg/student  [:ref]
   :reg/course+semester+student [:identity :tuple [:reg/course :reg/semester :reg/student]]})



(deftest tuple-schema-example-from-datomic-documentation
  (is (= (dasl/parse datomic-doc-tuple-example-schema-with-dasl)
         datomic-doc-tuple-example-vanilla-schema)))