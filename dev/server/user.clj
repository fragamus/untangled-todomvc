(ns user
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :refer (pprint)]
    [clojure.stacktrace :refer (print-stack-trace)]
    [clojure.tools.namespace.repl :refer [disable-reload! refresh clear set-refresh-dirs]]
    [com.stuartsierra.component :as component]
    [figwheel-sidecar.repl-api :as ra]
    [taoensso.timbre :refer [info set-level!] :as timbre]
    [todomvc.system :as system]
    [figwheel-sidecar.system :as sys]
    [watch :refer [start-watching stop-watching reset-fn]]
    [juxt.dirwatch :as dw]))

(def figwheel-config (sys/fetch-config))
(def figwheel (atom nil))

(defn start-figwheel
  "Start Figwheel on the given builds, or defaults to build-ids in `figwheel-config`."
  ([]
   (let [props (System/getProperties)
         all-builds (->> figwheel-config :data :all-builds (mapv :id))]
     (start-figwheel (keys (select-keys props all-builds)))))
  ([build-ids]
   (let [default-build-ids (-> figwheel-config :data :build-ids)
         build-ids (if (empty? build-ids) default-build-ids build-ids)
         preferred-config (assoc-in figwheel-config [:data :build-ids] build-ids)]
     (reset! figwheel (component/system-map :figwheel-system (sys/figwheel-system preferred-config)))
     (println "STARTING FIGWHEEL ON BUILDS: " build-ids)
     (swap! figwheel component/start)
     (sys/cljs-repl (:figwheel-system @figwheel)))))

;;SERVER

(set-refresh-dirs "src/server" "specs/server" "dev/server")

(def system (atom nil))

(set-level! :info)

(defn init
  "Create a web server from configurations. Use `start` to start it."
  []
  (reset! system (system/make-system)))

(defn start "Start (an already initialized) web server." [] (swap! system component/start))

(defn stop "Stop the running web server. Is a no-op if the server is already stopped" []
  (when @system
    (swap! system component/stop)
    (reset! system nil)))

(defn go "Load the overall web server system and start it." []
  (init)
  (start))

(defn reset
  "Stop the web server, refresh all namespace source code from disk, then restart the web server."
  []
  (stop)
  (refresh :after 'user/go))

(reset! watch/reset-fn reset)
