(ns datomic-event-sourcing.server
  (:use [compojure.core]
        [ring.util.response]
        [ring.middleware.cors])
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]           
            [compojure.route :as route]                        
            [ring.adapter.jetty :as jetty]
            [datomic-event-sourcing.views :as v])
  (:gen-class))

(defroutes app-routes
  (GET "/" request (v/index request))
  (route/resources "/")

  (context "/customers" []
           (defroutes customers-routes
             (GET "/" request (v/get-customers request))))

  (context "/customers/:id" [id]
           (defroutes customer-route
             (GET "/" request (v/get-customer id request))))

  (context "/history/:id" [id]
           (defroutes history-route
             (GET "/" request (v/get-history id request))))

)



(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)     
      ))


(defn -main [& args]
  (if (not (empty? args))
    (jetty/run-jetty app {:port (read-string (first args))})
    (jetty/run-jetty app {:port 8595}))
)
