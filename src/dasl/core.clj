(ns dasl.core
  (:require [clojure.spec-alpha2 :as s]))


(defn mapping [ks ns-string]
  (zipmap ks
          (mapv #(->> % name (keyword ns-string))
                ks)))


(def scalars #{:bigdec
               :bigint
               :boolean
               :double
               :float
               :instant
               :keyword
               :long
               :string
               :symbol
               :uuid})
(def ref-type :ref)
(def scalars+ref (conj scalars ref-type))
(def tuple-type :tuple)
(def tuples #{:tuple})

(def value-types (conj scalars ref-type tuple-type))
(def type-mapping (mapping value-types "db.type"))

(def cardinalities #{:one :many})
(def cardinality-mapping (mapping cardinalities "db.cardinality"))

(def uniquenesses #{:unique :identity})
(def uniqueness-mapping (mapping uniquenesses "db.unique"))


(s/def ::namespaced-keyword
  (s/and keyword?
         #(string? (namespace %))))

(s/def ::tuple-value-type
  (s/cat :tuple-indicator #{:tuple}
         :tuple-composition (s/coll-of ::namespaced-keyword)))

(s/def ::abbreviated-datomic-attribute-schema
  (s/cat :uniqueness (s/? uniquenesses)
         :cardinality cardinalities
         :value (s/alt :single scalars+ref
                       :tuple ::tuple-value-type)
         :doc (s/? string?)))

(comment (s/exercise ::abbreviated-datomic-attribute-schema))

(comment (s/valid? ::abbreviated-datomic-attribute-schema [:one :tuple [:a/b :c/d]])
         (s/valid? ::abbreviated-datomic-attribute-schema [:identity :one :string "email of the user"])
         (s/valid? ::abbreviated-datomic-attribute-schema [:many :ref "plop"]))

(s/def ::whole
  (s/map-of ::namespaced-keyword
            ::abbreviated-datomic-attribute-schema))

(defn expand [m]
  (into #{}
        (map (fn [[k {:keys [uniqueness cardinality value doc]}]]
               (let [[value-family v] value]
                 (-> {:db/ident k
                      :db/cardinality (get cardinality-mapping cardinality)
                      :db/valueType (get type-mapping (case value-family
                                                        :single v
                                                        :tuple :tuple))}
                     (cond->
                         (= value-family :tuple) (assoc :db/tupleAttrs (:tuple-composition v))
                         uniqueness (assoc :db/unique (get uniqueness-mapping uniqueness))
                         doc (assoc :db/doc doc))))))
        (s/conform ::whole m)))


;; TODO
(defn contract [v])
