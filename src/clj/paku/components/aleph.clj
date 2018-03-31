(ns paku.components.aleph
  (:require
   [com.stuartsierra.component :as component]
   [aleph.http :refer [start-server]]))

(defrecord WebServer [port handler server]
  component/Lifecycle
  (start [{:keys [port handler server] :as this}]
    (if (some? server)
      this
      (let [ring-handler (:handler handler)
            server (start-server ring-handler {:port port})]
        (assoc this :server server))))
  (stop [{:keys [server] :as this}]
    (if (nil? server)
      this
      (do (.close server)
          (assoc this :server nil)))))

(defn new-web-server
  ([port handler]
   (map->WebServer {:port port
                    :handler handler}))
  ([port]
   (new-web-server port nil)))
