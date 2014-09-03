(ns datomic-event-sourcing.customers
  (:require [datomic.api :as d]
            [datomic-event-sourcing.util :as util]))

(def uri "datomic:mem//customer-management")

(defn initialise-customer-db []
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (def schema-tx (read-string (slurp "db/schema.dtm")))
    @(d/transact conn schema-tx)))

(defn create-customer [user-id {:keys [email name address-line-1 town postcode]}]
  (let [conn (d/connect uri)]
    (let [tempid (d/tempid :db.part/user)]      
      (util/record-event
       conn
       tempid
       [{:db/id tempid 
         :customer/email email
         :customer/name name
         :customer/address-line-1 address-line-1
         :customer/address-town town
         :customer/address-postcode postcode}]
       "create-customer"
       user-id))))

(defn change-address [user-id customer-id {:keys [address-line-1 town postcode]}]
  (let [conn (d/connect uri)]
    (let [tempid (d/tempid :db.part/user)]      
      (util/record-event
       conn
       customer-id
       [{:db/id customer-id 
         :customer/address-line-1 address-line-1
         :customer/address-town town
         :customer/address-postcode postcode}]
       "change-address"
       user-id))))

(defn find-customer-by-id [customer-id]
  (println "got to find by id"))

(defn get-customer-history [customer-id]
  (let [conn (d/connect uri)]
    (util/changeset-for customer-id (d/db conn))))
