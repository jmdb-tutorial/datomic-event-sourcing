(ns datomic-event-sourcing.script
  (:use [clojure.pprint])
  (:require [datomic.api :as d]            
            [datomic-event-sourcing.util :as util]))


(def uri "datomic:mem://customers")

;;(d/delete-database uri)
(d/create-database uri)

(def conn (d/connect uri))


(def schema-tx (read-string (slurp "db/schema.dtm")))
@(d/transact conn schema-tx)

(let [tempid (d/tempid :db.part/user)]
  (def customer-bob
    (util/record-event
     conn
     tempid
     [[:db/add tempid :customer/email "bob@foo.com"]
      [:db/add tempid :customer/name "Bob Bobby"]
      [:db/add tempid :customer/address-line-1 "1 Some Street"]
      [:db/add tempid :customer/address-town "Some Town"]
      [:db/add tempid :customer/address-postcode "RE45 2WE"]]
     "create-customer"
     "johnnyfoo")))

(def customer-bob-id (:db/id customer-bob))

(util/record-event
 conn
 customer-bob-id
 [{:db/id customer-bob-id 
   :customer/address-line-1 "2 Some New Street"
   :customer/address-town "Some New Town"
   :customer/address-postcode "GH45 2SD"}]
  "change-address"
  "jennybar")


(pprint (util/changeset-for customer-bob-id (d/db conn)))

(def bob (d/entity (d/db conn) customer-bob-id))

(:customer/address-line-1 bob)


