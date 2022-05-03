(ns project-facts.loc
  (:require [clojure.java.shell :as shell]
            [clojure.string :as str]
            [project-facts.util :refer [->maps]]))

(defn per-language [path]
  (let [line-counts-per-language
        (->> (shell/sh "scc" path)
             :out
             (str/split-lines)
             (partition-by (fn [s] (re-matches #"─*" s)))
             (remove #(->> % (every? (fn [s] (re-matches #"─*" s)))))
             (take 3)
             flatten
             (map (fn [s] (drop-last 1 (str/split s #"\s+"))))
             ->maps)
        total-lines (->> line-counts-per-language
                         (map :Code)
                         (reduce +))]
    (->> line-counts-per-language
         (map
          (fn [{:keys [Code] :as lang-stats}]
            (assoc lang-stats :Percentage
                   (int (* 100 (/ Code (- total-lines Code))))))))))
