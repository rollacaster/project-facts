;; # Project explorer
^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns project-facts.core
  (:require [clj-kondo.core :as clj-kondo]
            [nextjournal.clerk :as clerk]
            [aero.core :as config]))

; Projects are statically analyzed with `clj-kondo`
^{:nextjournal.clerk/visibility #{:hide}}
(def code-analysis
  (:analysis
   (clj-kondo/run!
    {:lint [(:project-path (config/read-config "config.edn"))]
     :config {:output {:analysis true}}})))

^{:nextjournal.clerk/visibility #{:hide}}
(defn make-tree [nodes parent]
  (->> nodes
       (filter (fn [node] (= (:parent node) parent)))
       (reduce (fn [tree {:keys [node]}]
                 (conj
                  tree
                  (let [children (make-tree nodes node)]
                    (cond-> {:node node}
                      (seq children) (assoc :children children)))))
               [])))

(def namespaces (->> (:namespace-usages code-analysis)
                     (remove (fn [{:keys [from to]}] (= from to)))
                     (mapv (fn [{:keys [from to]}]
                             {:parent from
                              :node to}))))

;; ## Namespaces
^{:nextjournal.clerk/visibility #{:hide}}
(clerk/table namespaces)

^{:nextjournal.clerk/visibility #{:fold}}
(def namespace-tree (make-tree
                     (->> (:namespace-usages code-analysis)
                          (remove (fn [{:keys [from to]}] (= from to)))
                          (mapv (fn [{:keys [from to]}]
                                  {:parent from
                                   :node to})))
                     (symbol (:root-ns (config/read-config "config.edn")))))

;; ## Namespace viz
^{:nextjournal.clerk/visibility #{:hide}}
(clerk/html
 [:svg {:width "100%" :height 550}
  (map-indexed
   (fn [idx {:keys [node]}]
     [:text {:x 10 :y (+ 20 (* idx 20))} node])
   namespaces)])
