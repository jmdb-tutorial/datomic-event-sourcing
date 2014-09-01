(use '[datomic.api :only [q db] :as d])

(def uri "datomic:mem://accounts")

;; create database
(d/create-database uri)

;; connect to database
(def conn (d/connect uri))

;; parse schema dtm file
(def schema-tx (read-string (slurp "db/schema.dtm")))

;; submit schema transaction
@(d/transact conn schema-tx)

;; parse seed data dtm file
(def data-tx (read-string (slurp "db/accounts.dtm")))

;; submit seed data transaction
@(d/transact conn data-tx)

(defn decorate 
  "Simple function to pull out all the attributes of an entity into a map"
  [id]
  (let [ db (d/db conn)
         e (d/entity db id)]
    (select-keys e (keys e))))

(defn decorate-results 
  "maps through a result set where each item is a single entity and decorates it"
  [r]
  (map #(decorate (first %)) r))

(defn accounts
  "returns all accounts"
  []
  (d/q '[:find ?a :where [?a :account/balance _]] (d/db conn)))

(defn history
  "Returns all transactions"
  ([] (d/q '[:find ?tx :in $ :where [?tx :ot/amount _]] (d/db conn)))
  
  ([acct] (let [rules '[[(party ?t ?a)
                          [?t :ot/from ?a]]
                        [(party ?t ?a)
                         [?t :ot/to ?a ]]]]

                (d/q '[ :find ?t :in $ % ?a :where (party ?t ?a)]
                      (d/db conn) rules acct))))

(defn transfer [ from to amount note]
  (let [txid (datomic.api/tempid :db.part/tx)]
    (d/transact conn [[:transfer from to amount]                      
                      {:db/id txid, :db/doc note :ot/from from :ot/to to :ot/amount amount}])))

(defn credit [ to amount ]
  (d/transact conn [[:credit to amount]]))


(def issuer (ffirst (d/q '[:find ?e :where [?e :account/name "issuer"]] (d/db conn))))
(def bob (ffirst (d/q '[:find ?e :where [?e :account/name "bob"]] (d/db conn))))
(def alice (ffirst (d/q '[:find ?e :where [?e :account/name "alice"]] (d/db conn))))

(transfer issuer alice 77M "Issuance to Alice")
(transfer issuer bob 23M "Issuance to Bob")
(transfer alice bob 7M "Tomatoes")

(prn (decorate-results (accounts)))

(println "All transactions")
(prn (decorate-results (history)))

(println "Issuer's transactions")
(prn (decorate-results (history issuer)))
(prn (decorate issuer))
(println)

(println "Bob's transactions")
(prn (decorate-results (history bob)))
(prn (decorate bob))
(println)

(println "Alice's transactions")
(prn (decorate-results (history alice)))
(prn (decorate alice))
(println)

;; Throws an exception
(transfer alice bob 71M "Tomatoes")
