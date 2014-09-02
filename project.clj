(defproject datomic-event-sourcing "0.1.0-SNAPSHOT"
  :description "Code examples showing datomic used for 'event sourcing'"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.datomic/datomic-free "0.8.4218"]                 
                 [cheshire "5.0.0"]                 
                 [compojure "1.1.1"]
                 [ring-cors "0.1.0"]
                 [ring/ring-json "0.1.2"]                             
                 [ring/ring-jetty-adapter "1.1.7"]
                 [endjinn "0.1.0-SNAPSHOT"]]
  :ring {:handler datomic-event-sourcing.server/app}
            :plugins [[lein-ring "0.7.3"]]
            :main datomic-event-sourcing.server
)
 
