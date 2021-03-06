(ns clj-lambda-graphviz-s3.core
  (:require [clojure.java.io :as io]
            [lambada.core :refer [def-lambda-fn]]
            [cheshire.core :as json]
            [taoensso.timbre :as log]
            [environ.core :as environ]
            [clj-lambda-graphviz-s3.http-utils :as http-utils]
            [clj-lambda-graphviz-s3.spec :as spec]
            [clj-lambda-graphviz-s3.graphviz :as graphviz]
            [clj-lambda-graphviz-s3.s3 :as s3]))

(defn lambda-handler [body]
  (if-not (spec/valid-body? body)
    {:error (spec/explain body)}
    (try
      (let [file (graphviz/generate (:options body) (:source body))]
        (s3/put-file file (:bucket body))
        {:result :ok})
      (catch Exception e
        {:error e}))))

(defn lambda-function
  [in out context]
  (log/info "start")
  (let [body (http-utils/parse-json-input-stream in)]
    (log/info "body" body)
    (with-open [w (io/writer out)]
      (json/generate-stream (lambda-handler body) w)
      (log/info "lambda-function - finish"))))

(def-lambda-fn clj-lambda-graphviz.WebHook
  [in out context]
  (lambda-function in out context))
