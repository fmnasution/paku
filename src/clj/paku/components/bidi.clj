(ns paku.components.bidi
  (:require
   [com.stuartsierra.component :as component]
   [bidi.ring :refer [make-handler]]
   [taoensso.encore :as encore]))

(defn- route-configs
  [component]
  (into []
        (comp
         (map val)
         (filter (every-pred :routes :resources))
         (map (fn [{:keys [routes resources middleware]}]
                (let [middleware (:wrapper middleware identity)]
                  [routes (middleware resources)]))))
        component))

(defrecord RingRouter [handler middleware routes resources]
  component/Lifecycle
  (start [{:keys [handler middleware] :as this}]
    (if (some? handler)
      this
      (let [route-configs (route-configs this)
            routes ["" (into [] (map first) route-configs)]
            middleware (:wrapper middleware identity)
            resources (into []
                            (comp
                             (map second)
                             (map middleware))
                            route-configs)
            handler (make-handler routes resources)]
        (assoc this
               :handler handler
               :routes routes
               :resources resources))))
  (stop [{:keys [handler] :as this}]
    (if (nil? handler)
      this
      (assoc this
             :handler nil
             :routes nil
             :resources nil))))

(defn new-ring-router
  []
  (map->RingRouter {}))

(defrecord RouteConfig [middleware routes resources])

(defn new-route-config
  [routes resources]
  (map->RouteConfig {:routes routes
                     :resources resources}))
