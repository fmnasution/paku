(defproject paku "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0-alpha4"]
                 [org.clojure/clojurescript "1.10.238"]
                 [com.google.guava/guava "23.6-jre"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [com.stuartsierra/component "0.3.2"]
                 [aleph "0.4.5-alpha5"]
                 [org.clojure/core.async "0.4.474"]
                 [com.taoensso/sente "1.12.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.taoensso/encore "2.94.0"]
                 [bidi "2.1.3"]
                 [datascript "0.16.4"]
                 [metosin/ring-http-response "0.8.2"]
                 [figwheel-sidecar "0.5.14"]
                 [rum "0.11.2"]]
  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources"]
  :profiles {:dev {:source-paths ["src/cljs"]
                   :dependencies [[org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.namespace "0.3.0-alpha4"]]
                   :plugins [[refactor-nrepl "2.4.0-SNAPSHOT"]
                             [cider/cider-nrepl "0.17.0-SNAPSHOT"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}
  :aliases {"dev" ["with-profile"
                   "+dev"
                   "do" "clean," "repl" ":headless"]})
