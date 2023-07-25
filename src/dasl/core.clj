(ns dasl.core
  (:require
    [clojure.alpha.spec :as s]))


(defn mapping
  [ks ns-string]
  (->> (mapv #(keyword ns-string (name %)) ks)
       (zipmap ks)))


(def scalars
  #{:long :float :double :bigdec :bigint
    :boolean
    :instant
    :keyword :string :symbol :uuid})


(def ref-type :ref)
(def scalars+ref (conj scalars ref-type))
(def tuple-type :tuple)
(def tuples #{:tuple})

(def value-types (conj scalars ref-type tuple-type))
(def type-mapping (mapping value-types "db.type"))

(def cardinalities #{:one :many})
(def cardinality-mapping (mapping cardinalities "db.cardinality"))

(def uniquenesses #{:unique :identity})


(def uniqueness-mapping
  {:unique   :db.unique/value
   :identity :db.unique/identity})


(s/def ::namespaced-keyword
  (s/and keyword?
         #(string? (namespace %))))


(s/def ::tuple-value-type
  (s/cat :tuple-indicator #{:tuple}
         :tuple-composition (s/coll-of ::namespaced-keyword)))


(s/def ::abbreviated-datomic-attribute-schema
  (s/cat :uniqueness (s/? uniquenesses)
         :cardinality (s/? cardinalities)
         :value (s/alt :single scalars+ref
                       :tuple ::tuple-value-type)
         :doc (s/? string?)))



(comment (clojure.pprint/pprint (s/exercise ::abbreviated-datomic-attribute-schema)))


(comment (s/valid? ::abbreviated-datomic-attribute-schema [:one :tuple [:a/b :c/d]])
         (s/valid? ::abbreviated-datomic-attribute-schema [:identity :one :string "email of the user"])
         (s/valid? ::abbreviated-datomic-attribute-schema [:many :ref "plop"]))


(s/def ::whole
  (s/* (s/cat :k ::namespaced-keyword :v ::abbreviated-datomic-attribute-schema))
  #_(s/map-of ::namespaced-keyword
            ::abbreviated-datomic-attribute-schema))

(comment
  (s/conform ::whole [:a/b :string
                      :c/d :many :ref])

  (s/explain ::whole [:a/b [:string]])

  )

(defn expand
  [m]
  (into []
        (map (fn [{k :k
                   {:keys [uniqueness cardinality value doc]} :v}]
               (let [[value-family v] value]
                 (-> {:db/ident k
                      :db/cardinality (get cardinality-mapping (or cardinality :one))
                      :db/valueType (get type-mapping (case value-family
                                                        :single v
                                                        :tuple :tuple))}
                     (cond->
                       (= value-family :tuple) (assoc :db/tupleAttrs (:tuple-composition v))
                       uniqueness (assoc :db/unique (get uniqueness-mapping uniqueness))
                       doc (assoc :db/doc doc))))))
        (s/conform ::whole m)))


(comment (expand [:a/b :string
                  :c/d :many :ref]))

;; TODO
(defn contract
  [v])
