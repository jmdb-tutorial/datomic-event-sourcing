(ns datomic-event-sourcing.database
  (:use [datomic.api :only [q db] :as d]))


(def uri "datomic:mem://customers")

;;(d/delete-database uri)
(d/create-database uri)

(def conn (d/connect uri))


(def schema-tx (read-string (slurp "db/schema.dtm")))
@(d/transact conn schema-tx)

;;(def data-tx (read-string (slurp "db/sample-data.dtm")))
;;@(d/transact conn data-tx)

(defn get-inserted-entity
  [tempid tx-res]
  (d/entity (:db-after tx-res) (d/resolve-tempid (:db-after tx-res) (:tempids tx-res) tempid)))

(defn transact-entity
  [conn tempid tx]
  (get-inserted-entity tempid @(d/transact conn tx)))

(let [user-tempid (d/tempid :db.part/user)]
  (def user
    (transact-entity
     conn
     user-tempid
     [[:db/add user-tempid :customer/email "bob@foo.com"]
      [:db/add user-tempid :customer/name "Bob McBob"]
      [:db/add user-tempid :customer/address-line-1 "1 Some Street"]
      [:db/add user-tempid :customer/address-town "Some Town"]
      [:db/add user-tempid :customer/address-postcode "RE45 2WE"]])))












