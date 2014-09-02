(ns datomic-event-sourcing.views
  (:use [ring.util.response]
        [endjinn.web]))

(defn index [request]
  (response (array-map :is ["customers" "index"]
                       :customers (local-url request "/api/customers"))))

(defn get-customers [request]
  (response (array-map :is ["customer" "list"]
                       :numberOfItems 6
                       :items [{:id 1 :name "XXX-1" :email "xxx@1.xxx" :address-line-1 "xxxxx 1" :town "xxxxx" :postcode "XX1 1XX" :more (local-url request "/customers/1")}
                               {:id 2 :name "XXX-2" :email "xxx@2.xxx" :address-line-1 "xxxxx 2" :town "xxxxx" :postcode "XX2 2XX" :more (local-url request "/customers/2")}
                               {:id 3 :name "XXX-3" :email "xxx@3.xxx" :address-line-1 "xxxxx 3" :town "xxxxx" :postcode "XX3 3XX" :more (local-url request "/customers/3")}
                               {:id 4 :name "XXX-4" :email "xxx@4.xxx" :address-line-1 "xxxxx 4" :town "xxxxx" :postcode "XX4 4XX" :more (local-url request "/customers/4")}
                               {:id 5 :name "XXX-5" :email "xxx@5.xxx" :address-line-1 "xxxxx 5" :town "xxxxx" :postcode "XX5 5XX" :more (local-url request "/customers/5")}
                               {:id 6 :name "XXX-6" :email "xxx@6.xxx" :address-line-1 "xxxxx 6" :town "xxxxx" :postcode "XX6 6XX" :more (local-url request "/customers/6")}])))

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
