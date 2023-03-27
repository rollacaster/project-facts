(ns clerk-ui
  (:require [nextjournal.clerk.viewer :as viewer]))

(defmacro cljs
  "Evaluate expressions in ClojureScript instead of Clojure. If the result is
   a vector, it is passed to Reagent and interpreted as hiccup."
  [& exprs]
  (let [name (symbol (str "reagent-view-" (hash exprs)))]
    (if (:ns &env)
      ;; in ClojureScript, define a function
      `(defn ~(with-meta name {:export true}) [] ~@exprs)
      ;; in Clojure, return a map with a reference to the fully qualified sym
      {:reagent/var `'~(symbol (str *ns*) (str name))})))

(def reagent-viewer
  (viewer/process-render-fn
   {:pred #(and (map? %) (contains? % :reagent/var))
    :fetch-fn (fn [_ x] x)
    :render-fn '(fn render-var [{var :reagent/var}]
                  (js/console.log "hi" v/html)
                  (let [path (->> (str/split (str var) #"[./]")
                                  (mapv munge))
                        reagent-fn (applied-science.js-interop/get-in js/window path)
                        wrapper (fn [f] (let [result (f)]
                                          (if (vector? result)
                                            result
                                            (v/inspect result))))]
                    (when reagent-fn
                      (v/html [:div.my-1 [wrapper reagent-fn]]))))}))


(defn setup-viewers! []
  (swap! viewer/!viewers update :root #(into [reagent-viewer] %)))
(swap! viewer/!viewers update :root #(into [reagent-viewer] %))
