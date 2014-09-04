(ns datomic-event-sourcing.script
  (:use [clojure.pprint])
  (:require [datomic.api :as d]            
            [datomic-event-sourcing.util :as util]))


(def uri "datomic:mem://customers")

;;(d/delete-database uri)
(d/create-database uri)

(def conn (d/connect uri))


(def schema-tx (read-string (slurp "db/schema.dtm")))
(pprint @(d/transact conn schema-tx))

(let [tempid (d/tempid :db.part/user)]
  (def customer-01
    (util/record-event
     conn
     tempid
     [{:db/id tempid 
       :customer/email "xxx@xxx.xxx"
       :customer/name "Xxx Xxxx"
       :customer/address-line-1 "0 Xxxx Xxxxx"
       :customer/address-town "Xxxxx"
       :customer/address-postcode "XX00 0XX"}]
     "create-customer"
     "user-01")))

(def customer-01-id (:db/id customer-01))

(d/q '[:find ?c :where [?c :customer/email "xxx@xxx.xxx"]] (d/db conn))

(def customer-01-loaded (d/entity (d/db conn) customer-01-id))

(pprint customer-01-loaded)

(:customer/address-line-1 customer-01-loaded)


(util/record-event
 conn
 customer-01-id
 [{:db/id customer-01-id 
   :customer/address-line-1 "0 NEW Xxxxx"
   :customer/address-town "NEW Xxxxxx"
   :customer/address-postcode "YY00 0YY"}]
  "change-address"
  "user-02")


(pprint (util/changeset-for customer-01-id (d/db conn)))




