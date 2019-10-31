(ns dasl.core
  (:require [clojure.spec-alpha2 :as s]))


(s/def ::namespaced-keyword
  (s/and keyword?
         #(string? (namespace %))))


(def scalars #{:db.type/bigdec
               :db.type/bigint
               :db.type/boolean
               :db.type/double
               :db.type/float
               :db.type/instant
               :db.type/keyword
               :db.type/long
               :db.type/string
               :db.type/symbol
               :db.type/uuid})

(def ref-type :db.type/ref)
(def tuple-type :db.type/tuple)

(def value-types (conj scalars ref-type tuple-type))

;; See http://insideclojure.org/2019/08/10/journal/
;; difference: spec* as been renamed to resolve-spec
(s/register ::value-type
            (s/resolve-spec
             {:clojure.spec/op `s/alt
              :keys (vec value-types)
              :specs (mapv #(-> % name keyword hash-set)
                            value-types)}))

(comment
  ;; To check what it would look like if it was written by hand
  (s/form ::value-type)

  (s/conform ::value-type [:string]))

(s/def ::single-value-type ::value-type)

(s/def ::tuple-value-type
  (s/cat :tuple-indicator #{:tuple}
         :tuple-composition (s/coll-of ::namespaced-keyword)))

(s/def ::abbreviated-datomic-attribute-schema
  (s/cat :uniqueness-indicator (s/? #{:unique :identity})
         :cardinality-indicator #{:one :many}
         :value (s/alt :single ::single-value-type
                       :tuple ::tuple-value-type)
         :doc (s/? string?)))

(comment (s/exercise ::abbreviated-datomic-attribute-schema)
         (s/exercise ::abbreviated-datomic-attribute-schema))

(comment (s/valid? ::abbreviated-datomic-attribute-schema [:one :tuple [:a/b :c/d]])
         (s/valid? ::abbreviated-datomic-attribute-schema [:identity :one :string "email of the user"])
         (s/valid? ::abbreviated-datomic-attribute-schema [:many :ref "plop"]))

(s/def ::whole
  (s/map-of ::namespaced-keyword
            ::abbreviated-datomic-attribute-schema))


(defn expand [m]
  (into #{}
        (map (fn [[k {:keys [uniqueness-indicator cardinality-indicator value doc]}]]
               (let [[value-family v] value]
                 (-> (case value-family
                       :tuple  {:db/valueType :db.type/tuple
                                :db/tupleAttrs  (:tuple-composition v)}
                       :single {:db/valueType (first v)})
                     (assoc :db/ident k)
                     (assoc :db/cardinality (case cardinality-indicator
                                              :one :db.cardinality/one
                                              :many :db.cardinality/many))
                     (cond->
                         uniqueness-indicator (assoc :db/unique
                                                     (case uniqueness-indicator
                                                       :identity :db.unique/identity
                                                       :unique   :db.unique/value))
                         doc (assoc :db/doc doc))))))
        (s/conform ::whole m)))


;; TODO
(defn contract [v])
