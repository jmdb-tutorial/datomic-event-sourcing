(ns datomic-event-sourcing.test-data
  (:use clojure.pprint)
  (:require [datomic-event-sourcing.util :as util]
            [datomic-event-sourcing.customers :as c]))  
 

(defn reset-test-data []
  (c/destroy-customer-db)
  (c/initialise-customer-db)
  (c/create-customer "user-001" {:email "aaa@aaa.aaa" :name "aaaa aaaaa" :address-line-1 "aaaaaaa" :town "aaaaa" :postcode "AA0 0AA"})
  (c/create-customer "user-001" {:email "bbb@bbb.bbb" :name "bbbb bbbbb" :address-line-1 "bbbbbbb" :town "bbbbb" :postcode "BB0 0BB"})
  (c/create-customer "user-001" {:email "ccc@ccc.ccc" :name "cccc ccccc" :address-line-1 "ccccccc" :town "ccccc" :postcode "CC0 0CC"})
  (c/create-customer "user-002" {:email "ddd@ddd.ddd" :name "dddd ddddd" :address-line-1 "ddddddd" :town "ddddd" :postcode "DD0 0DD"})
  (c/create-customer "user-002" {:email "eee@eee.eee" :name "eeee eeeee" :address-line-1 "eeeeeee" :town "eeeee" :postcode "EE0 0EE"})
  (println "Created some test customers"))

