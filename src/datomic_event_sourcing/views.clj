(ns datomic-event-sourcing.views
  (:use [ring.util.response]
        [endjinn.web]))

(defn index [request]
  (response (array-map :is ["customers" "index"]
                       :customers (local-url request "/customers"))))

(defn get-customers [request]
  (response (array-map :is ["customer" "list"]
                       :numberOfItems 2
                       :items [(local-url request "/customers/1")
                               (local-url request "/customers/2")])))

(defn get-customer [id request]
  (response (array-map :is "customer"
                       :id id
                       :name "XXXXXXXX"
                       :email "xxx@xxx.xxx"
                       :address-line-1 "XXXXXXXX"
                       :town "XXXXX"
                       :postcode "XX0 0XX"
                       :history (local-url request (format "/history/%s" id)))))

(defn get-history [id request]
  (response (array-map :is ["event" "list"]
                       :numberOfItems 2
                       :items [{:type "create-customer" :user-id "XXXX" :timestamp "XXXX" :changes {}}
                                 {:type "change-address" :user-id "XXXX" :timestamp "XXXX" :changes {}}])))
