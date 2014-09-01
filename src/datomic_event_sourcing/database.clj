(ns datomic-event-sourcing.database
  (:require [datomic.api :as d]))


(def uri "datomic:mem://customers")

;;(d/delete-database uri)
(d/create-database uri)

(def conn (d/connect uri))


(def schema-tx (read-string (slurp "db/schema.dtm")))
@(d/transact conn schema-tx)

;;(def data-tx (read-string (slurp "db/sample-data.dtm")))
;;@(d/transact conn data-tx)

(defn get-inserted-entity
  [tempid tx-res]
  (d/entity (:db-after tx-res) (d/resolve-tempid (:db-after tx-res) (:tempids tx-res) tempid)))

(defn transact-entity
  [conn tempid tx]
  (get-inserted-entity tempid @(d/transact conn tx)))

(let [tempid (d/tempid :db.part/user)]
  (def customer
    (transact-entity
     conn
     tempid
     [[:db/add tempid :customer/email "bob@foo.com"]
      [:db/add tempid :customer/name "Bob McBob"]
      [:db/add tempid :customer/address-line-1 "1 Some Street"]
      [:db/add tempid :customer/address-town "Some Town"]
      [:db/add tempid :customer/address-postcode "RE45 2WE"]])))

(def entity-id (:db/id customer))

(transact-entity
 conn
 entity-id
 [[:db/add entity-id :customer/address-line-1 "2 Some New Street"]
  [:db/add entity-id :customer/address-town "Some New Town"]
  [:db/add entity-id :customer/address-postcode "GH45 2SD"]])

(def db (d/db conn))


(->>
 ;; This query finds all tuples of the tx and the actual attribute that
 ;; changed for a specific entity.
 (d/q
  '[:find ?tx ?a
    :in $ ?e
    :where
    [?e ?a _ ?tx]]
  (d/history db)
  entity-id)
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
                            (d/entity tx-before-db entity-id)
                            (:db/ident attr-e-before))
                      :new (get
                            (d/entity tx-after-db entity-id)
                            (:db/ident attr-e-after))}]))
                changes))
     :timestamp (->> (ffirst changes)
                     (d/entity (d/as-of db (ffirst changes)))
                     :db/txInstant)})))
