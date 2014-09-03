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

(defn remove-tags [customer]
  (dissoc customer :is))

(defn load-customers [request]
  (->> 
   (c/get-all-customers)
   (map (fn [customer] (add-customer-links request)))
   (map (fn [customer] (remove-tags)))))

(defn get-customers [request]
  (let [all-customers (load-customers request)]
    (response (array-map :is ["customer" "list"]
                         :numberOfItems (count all-customers)
                         :items all-customers))))

(defn get-customer [id request]
  (response (array-map :is "customer"
                       :id id
                       :name "XXXXXXXX"
                       :email "xxx@xxx.xxx"
                       :address-line-1 "XXXXXXXX"
                       :town "XXXXX"
                       :postcode "XX0 0XX"
                       :history (local-url request (format "/api/history/%s" id)))))

(defn get-history [id request]
  (response (array-map :is ["event" "list"]
                       :numberOfItems 2
                       :items [{:type "create-customer" :user-id "XXXX" :timestamp "XXXX" :changes {:old "foo" :new "bar"}}
                                 {:type "change-address" :user-id "XXXX" :timestamp "XXXX" :changes {}}])))
