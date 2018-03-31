(ns paku.components.figwheel
  (:require
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]
   [figwheel-sidecar.repl-api :as figwheel.repl-api]
   [taoensso.timbre :as timbre]
   [taoensso.encore :as encore]))

(defn- delete-files-recursively!
  ([fname silently?]
   (letfn [(delete-f [file]
             (when (.isDirectory file)
               (doseq [child-file (.listFiles file)]
                 (delete-f child-file)))
             (io/delete-file file silently?))]
     (delete-f (io/file fname))))
  ([fname]
   (delete-files-recursively! fname false)))

(defn- js-paths
  [{:keys [build-ids all-builds]}]
  (let [build-ids (set build-ids)]
    (into []
          (comp
           (filter (comp build-ids :id))
           (map :compiler)
           (mapcat (fn [{:keys [output-to output-dir]}]
                     [output-to output-dir]))
           (remove nil?))
          all-builds)))

(defn- delete-compiled-js!
  [{:keys [fresh?] :as config}]
  (when fresh?
    (let [paths (js-paths config)]
      (run! #(delete-files-recursively! % true) paths))))

(defrecord FigwheelServer [all-builds figwheel-options build-ids fresh? started?]
  component/Lifecycle
  (start [{:keys [started?] :as this}]
    (if started?
      this
      (do (delete-compiled-js! this)
          (figwheel.repl-api/start-figwheel!
           (dissoc this :fresh? :started?))
          (assoc this :started? true))))
  (stop [{:keys [started?] :as this}]
    (if-not started?
      this
      (do (delete-compiled-js! this)
          (figwheel.repl-api/stop-figwheel!)
          (assoc this :started? false)))))

(defn new-figwheel-server
  ([all-builds figwheel-options fresh? build-ids]
   (map->FigwheelServer {:all-builds all-builds
                         :figwheel-options figwheel-options
                         :fresh? fresh?
                         :build-ids build-ids
                         :started? false}))
  ([all-builds figwheel-options fresh?]
   (let [build-ids (into [] (keep :id) all-builds)]
     (new-figwheel-server all-builds figwheel-options fresh? build-ids))))

(defn cljs-repl!
  []
  (figwheel.repl-api/cljs-repl))
