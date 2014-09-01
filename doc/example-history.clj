(do
  (require '[datomic.api :as d])
  (require '[dbs-are-fn-datomic-examples.util :as util])
  (def datomic-url "datomic:mem://history-of-an-entity")
  (d/create-database datomic-url)
  (def datomic-conn (d/connect datomic-url)))


(d/transact
 datomic-conn
 [{:db/id #db/id[:db.part/db]
   :db.install/_attribute :db.part/db
   :db/unique :db.unique/value
   :db/ident :user/email
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one}

  {:db/id #db/id[:db.part/db]
   :db.install/_attribute :db.part/db
   :db/ident :user/name
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one}

  {:db/id #db/id[:db.part/db]
   :db.install/_attribute :db.part/db
   :db/ident :user/age
   :db/valueType :db.type/long
   :db/cardinality :db.cardinality/one}

  {:db/id #db/id[:db.part/db]
   :db.install/_attribute :db.part/db
   :db/ident :user/address
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one}])

(let [user-tempid (d/tempid :db.part/user)]
  (def user
    (util/transact-entity
     datomic-conn
     user-tempid
     [[:db/add user-tempid :user/email "quentin@test.com"]
      [:db/add user-tempid :user/name "Quentin Test"]])))


(util/transact-entity
 datomic-conn
 (:db/id user)
 [[:db/add (:db/id user) :user/name "Quentin F. Test"]])

(util/transact-entity
 datomic-conn
 (:db/id user)
 [[:db/add (:db/id user) :user/age 27]
  [:db/add (:db/id user) :user/address "Some Road 155, Norway"]])

(util/transact-entity
 datomic-conn
 (:db/id user)
 [[:db/add (:db/id user) :user/email "quentin@mycompany.com"]])

(def db (d/db datomic-conn))

;; We're getting a sequence of maps contaning the full before/after of entities

(->>
 ;; This query finds all transactions that touched a particular entity
 (d/q
  '[:find ?tx
    :in $ ?e
    :where
    [?e _ _ ?tx]]
  (d/history db)
  (:db/id user))
 ;; The transactions are themselves represented as entities. We get the
 ;; full tx entities from the tx entity IDs we got in the query.
 (map #(d/entity (d/db datomic-conn) (first %)))
 ;; The transaction entity has a txInstant attribute, which is a timestmap
 ;; as of the transaction write.
 (sort-by :db/txInstant)
 ;; as-of yields the database as of a t. We pass in the transaction t for
 ;; after, and (dec transaction-t) for before. The list of t's might have
 ;; gaps, but if you specify a t that doesn't exist, Datomic will round down.
 (map
  (fn [tx]
    {:before (d/entity (d/as-of db (dec (d/tx->t (:db/id tx)))) (:db/id user))
     :after (d/entity (d/as-of db (:db/id tx)) (:db/id user))})))

;; The next step would be to diff the before/after entities. But instead we show an alternate
;; method leveraging the query engine and the history database layout eavt.
(->>
 ;; This query finds all tuples of the tx and the actual attribute that
 ;; changed for a specific entity.
 (d/q
  '[:find ?tx ?a
    :in $ ?e
    :where
    [?e ?a _ ?tx]]
  (d/history db)
  (:db/id user))
 ;; We group the tuples by tx - a tx single can and will contain multiple
 ;; attribute changes.
 (group-by (fn [[tx attr]] tx))
 ;; We only want the actual changes
 (vals)
 ;; Sort with oldest first
 (sort-by (fn [[tx attr]] tx))
 ;; Generate our full changeset value
 (map
  (fn [changes]
    {:changes (into
               {}
               (map
                (fn [[tx attr]]
                  (let [tx-before-db (d/as-of db (dec (d/tx->t tx)))
                        tx-after-db (d/as-of db tx)
                        tx-e (d/entity tx-after-db tx)
                        attr-e-before (d/entity tx-before-db attr)
                        attr-e-after (d/entity tx-after-db attr)]
                    [(:db/ident attr-e-after)
                     {:old (get
                            (d/entity tx-before-db (:db/id user))
                            (:db/ident attr-e-before))
                      :new (get
                            (d/entity tx-after-db (:db/id user))
                            (:db/ident attr-e-after))}]))
                changes))
     :timestamp (->> (ffirst changes)
                     (d/entity (d/as-of db (ffirst changes)))
                     :db/txInstant)})))



