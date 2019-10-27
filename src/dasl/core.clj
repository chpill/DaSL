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

;; TODO this is ugly... is there a way to mechanically derive this spec from the
;; set of scalars + the ref-type values? - chpill 2019/10/27
(s/def ::value-type
  (s/alt :db.type/bigdec   #{:bigdec}
         :db.type/bigint   #{:bigint}
         :db.type/boolean  #{:boolean}
         :db.type/double   #{:double}
         :db.type/float    #{:float}
         :db.type/instant  #{:instant}
         :db.type/keyword  #{:keyword}
         :db.type/long     #{:long}
         :db.type/ref      #{:ref}
         :db.type/string   #{:string}
         :db.type/symbol   #{:symbol}
         :db.type/uuid     #{:uuid}))

(comment (s/conform ::value-type [:string]))


(s/def ::single-value-type
  (s/cat :cardinality-indicator (s/? #{:many})
         :value-type ::value-type))

(s/def ::tuple-value-type
  (s/cat :tuple-indicator #{:tuple}
         :tuple-composition (s/coll-of ::namespaced-keyword)))

(s/def ::abbreviated-datomic-attribute-schema
  (s/cat :uniqueness-indicator (s/? #{:unique :identity})
         :value (s/alt :single ::single-value-type
                       :tuple ::tuple-value-type)
         :doc (s/? string?)))

(comment (s/exercise ::abbreviated-datomic-attribute-schema)
         (s/exercise ::abbreviated-datomic-attribute-schema))

(comment (s/valid? ::abbreviated-datomic-attribute-schema [:tuple [:a/b :c/d]])
         (s/valid? ::abbreviated-datomic-attribute-schema [:string :identity "email of the user"])
         (s/valid? ::abbreviated-datomic-attribute-schema [:many :ref "plop"]))

(s/def ::whole
  (s/map-of ::namespaced-keyword
            ::abbreviated-datomic-attribute-schema))


(defn parse [m]
  (into #{}
        (map (fn [[k {:keys [uniqueness-indicator value doc]}]]
               (let [[value-family v] value]
                 (-> (case value-family
                       :tuple  {:db/valueType :db.type/tuple
                                :db/tupleAttrs  (:tuple-composition v)
                                :db/cardinality :db.cardinality/one}
                       :single {:db/valueType (first (:value-type v))
                                :db/cardinality (if (= (:cardinality-indicator v) :many)
                                                  :db.cardinality/many
                                                  :db.cardinality/one)})
                     (assoc :db/ident k)
                     (cond->
                         uniqueness-indicator (assoc :db/unique
                                                     (case uniqueness-indicator
                                                       :identity :db.unique/identity
                                                       :unique   :db.unique/value))
                         doc (assoc :db/doc doc))))))
        (s/conform ::whole m)))

