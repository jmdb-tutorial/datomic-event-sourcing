(ns datomic-event-sourcing.views
  (:use [ring.util.response]
        [endjinn.web])
  (:require [datomic-event-sourcing.customers :as c]))

(defn index [request]
  (response (array-map :is ["customers" "index"]
                       :customers (local-url request "/api/customers"))))

(defn add-customer-links [request customer]
  (conj {:history (local-url request (format "/api/history/%s" (:id customer)))}
        customer))

(defn inline-representation [request customer]
  (-> customer
      (dissoc :is)
      (conj {:more (local-url request (format "/api/customers/%s" (:id customer)))})))

(defn load-customers [request]
  (->> 
   (c/get-all-customers)
   (map (fn [customer] (add-customer-links request customer)))
   (map (fn [customer] (inline-representation request customer)))))

(defn get-customers [request]
  (let [all-customers (load-customers request)]
    (response (array-map :is ["customer" "list"]
                         :numberOfItems (count all-customers)
                         :items all-customers))))

(defn get-customer [id request]
  (response (->> (c/get-customer-by-id (read-string id))
                 (add-customer-links request))))

(defn get-history [id request]
  (response (array-map :is ["event" "list"]
                       :numberOfItems 2
                       :items [{:type "create-customer" :user-id "XXXX" :timestamp "XXXX" :changes {:old "foo" :new "bar"}}
                                 {:type "change-address" :user-id "XXXX" :timestamp "XXXX" :changes {}}])))
