(ns datomic-event-sourcing.views
  (:use [ring.util.response]
        [endjinn.web])
  (:require [datomic-event-sourcing.customers :as c]))

(defn index [request]
  (response (array-map :is ["customers" "index"]
                       :customers (local-url request "/api/customers"))))

(defn add-customer-links [request customer]
  (assoc customer :history (local-url request (format "/api/history/%s" (:id customer)))))

(defn inline-representation [request customer]
  (-> customer
      (dissoc :is)
      (assoc :more (local-url request (format "/api/customers/%s" (:id customer))))))

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
  (let [customer-history (c/get-customer-history (read-string id))]
    (response (array-map :is ["event" "list"]
                         :numberOfItems (count customer-history)
                         :items customer-history))))

