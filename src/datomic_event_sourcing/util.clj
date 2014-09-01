(ns datomic-event-sourcing.util
  (:require [datomic.api :as d]))


(defn get-inserted-entity
  [tempid tx-res]
  (d/entity (:db-after tx-res) (d/resolve-tempid (:db-after tx-res) (:tempids tx-res) tempid)))

(defn transact-entity
  [conn tempid tx]
  (get-inserted-entity tempid @(d/transact conn tx meta-data)))

(defn record-event
  [conn tempid tx event-type user-id]
  (let [txid (d/tempid :db.part/tx)]
    (get-inserted-entity tempid 
                         @(d/transact conn 
                                      (conj tx {:db/id txid :x-event/type event-type :x-event/user-id user-id})))))


(defn changeset-for [entity-id db]
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
                     :db/txInstant)
     :event-type (->> (ffirst changes)
                     (d/entity (d/as-of db (ffirst changes)))
                     :x-event/type)
     :user-id (->> (ffirst changes)
                     (d/entity (d/as-of db (ffirst changes)))
                     :x-event/user-id)}))))
