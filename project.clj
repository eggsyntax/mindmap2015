(defproject mindmap "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [lein-reload "1.0.0"]
                 [org.clojure/tools.namespace "0.2.9"]
                 [prismatic/schema "0.3.7"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [clojure-lanterna "0.9.4"] ]
  :plugins  [[cider/cider-nrepl "0.8.2"]
             ; lein-test-refresh: https://github.com/jakemcc/lein-test-refresh
             [com.jakemccrary/lein-test-refresh "0.7.0"]

             ]
  :main ^:skip-aot mindmap.core
  :test-refresh {:notify-command ["terminal-notifier" "-title" "Tests" "-message"]
                  ; only notify if there are failures
                 :notify-on-success false}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
