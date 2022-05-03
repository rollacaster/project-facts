;; # Project explorer
^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns project-facts.core
  (:require
   [aero.core :as config]
   [arrowic.core :as graph]
   [clj-kondo.core :as clj-kondo]
   [nextjournal.clerk :as clerk]))

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
(def ns-graph
  (graph/with-graph (graph/create-graph)
    (let [vertices (->> namespaces
                        (reduce
                         (fn [vs {:keys [node]}]
                           (assoc vs (str node) (graph/insert-vertex! (str node))))
                         {}))]
      (doseq [{:keys [node parent]} namespaces]
        (graph/insert-edge! (get vertices (str parent))
                            (get vertices (str node)))))))
;; ## Namespace viz
(clerk/html
 (graph/as-svg ns-graph))

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
