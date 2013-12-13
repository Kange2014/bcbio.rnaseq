(ns bcbio.rnaseq.config
  (:use [bcbio.rnaseq.util])
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]))

(def default-bcbio-system (get-resource "bcbio_system.yaml"))
(def default-bcbio-sample (get-resource "bcbio_sample.yaml"))
(def default-bcbio-project "/v-data/bcbio-nextgen/tests/test_automated_output/upload/project_2013-12-12/project-summary.yaml")

(def cfg-state (atom {}))
(def get-config #(deref cfg-state))
(defn alter-config! [new-cfg]
  (swap! cfg-state (constantly new-cfg)))

(defn- load-yaml [yaml-file]
  (yaml/parse-string (slurp yaml-file)))


(defn setup-config [bcbio-project-file]
  (let [project-config (load-yaml bcbio-project-file)
        system-config (load-yaml (:bcbio_system project-config))]
    (alter-config! (merge system-config project-config))))

(defn metadata-key [key]
  (map key (map :metadata (:samples (get-config)))))

(def get-description
  #(map :description (:samples (get-config))))

(defn program-path [prog]
  "query configuration for program path by keyword"
  (:cmd (prog (:resources (get-config))) (name prog)))


(def upload-dir #(:upload (get-config)))
(def sample-names #(map :description (:samples (get-config))))
(def genome-build #(first (map :genome_build (:samples (get-config)))))
(def ref-fasta #(:sam_ref (first (:samples (get-config)))))

(def gtf-file #(get-in (first (:samples (get-config)))
                       [:genome_resources :rnaseq :transcripts]))
(def library-type #(get-in (first (:samples (get-config)))
                           [:algorithm :strandedness] "unstranded"))
(defn count-files []
  (map #(str (fs/file (upload-dir) % (str % "-ready.counts"))) (sample-names)))

(defn comparison-name [key]
  (clojure.string/join "_vs_" (distinct (metadata-key key))))
(def combined-count-file
  #(str (fs/file (upload-dir) "de" "combined.counts")))

