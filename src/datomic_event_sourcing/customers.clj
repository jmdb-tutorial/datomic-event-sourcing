(ns datomic-event-sourcing.customers
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic-event-sourcing.util :as util]))

(def uri "datomic:mem://customer-management")

(defn destroy-customer-db []
  (if (not (d/create-database uri))
    (d/delete-database uri)))

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

(defn change-email [user-id customer-id {:keys [email]}]
  (let [conn (d/connect uri)]
    (let [tempid (d/tempid :db.part/user)]      
      (util/record-event
       conn
       customer-id
       [{:db/id customer-id 
         :customer/email email}]
       "change-email"
       user-id))))

(defn map-customer [entity]
  (array-map :is ["customer"]
             :id (:db/id entity)
             :name (:customer/name entity)
             :email (:customer/email entity)
             :address-line-1 (:customer/address-line-1 entity)
             :town (:customer/address-town entity)
             :postcode (:customer/address-postcode entity)))


(defn process-changes-map-entry [changes-map fieldname]
  {:field (last (clojure.string/split (name fieldname) #"/"))
   :mutation (fieldname changes-map)})

(defn process-changes-map [changes-map]
  (sort-by :field (map (partial process-changes-map-entry changes-map) (keys changes-map))))

(defn map-history [changes-col]
   (map (fn [item]
          (array-map :type (:event-type item)
                            :user-id (:user-id item)
                            :timestamp (:timestamp item)
                            :changes (process-changes-map (:changes item))))
        changes-col))

(defn get-all-customers []
  (let [conn (d/connect uri)]
    (map map-customer (sort-by :customer/name (util/decorate-results (d/q '[:find ?c :where [?c :customer/email _]] (d/db conn)) conn)))))

(defn get-customer-by-id [customer-id]
  (let [conn (d/connect uri)]
    (map-customer (d/entity (d/db conn) customer-id))))

(defn get-customer-history [customer-id]
  (let [conn (d/connect uri)]
    (map-history (util/changeset-for customer-id (d/db conn)))))


