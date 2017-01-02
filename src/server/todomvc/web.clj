(ns todomvc.web
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :refer (pprint)]
    [clojure.stacktrace :refer (print-stack-trace)]
    [clojure.tools.namespace.repl :refer [disable-reload! refresh clear set-refresh-dirs]]
    [com.stuartsierra.component :as component]
    [taoensso.timbre :refer [info set-level!] :as timbre]
    [todomvc.system :as system]
))

;;SERVER

(def system (atom nil))

(defn init
  "Create a web server from configurations. Use `start` to start it."
  []
  (reset! system (system/make-system)))

(defn start "Start (an already initialized) web server." [] (swap! system component/start))

(defn go "Load the overall web server system and start it." []
  (init)
  (start))

(defn -main
    "Main entry point for the server"
    [& args]
    (do (printf "starting app .......... config=\"%s\" ........." (System/getProperty "config"))  (go)) ) 
