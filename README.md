# DASL

Toy domain specific language to define datomic schemas -- alpha quality, subject to breaking change.



## Rationale

Datomic schemas are data and machine-oriented. They are not easy to write by hand or to read for a human being.

The idea is to create a DSL which can be expanded to a regular datomic schema transaction.




## TODOS

* Add all datomic schema attributes
* Provide a way to add arbitrary datoms to a schema entity (for example, a deprecated attribute?)
* How do we declare aliases for an existing :db/ident with this?
