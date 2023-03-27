(ns project-facts.cljs-render-fn-file
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [nextjournal.clerk :as clerk]))

(defn render-fn [sym]
  (let [ns (namespace sym)
        file (str "src/project_facts/" (str/replace (munge ns) "." "/") ".cljs")
        source (slurp file)
        source `(do
                  (load-string ~source)
                  (resolve ~(list 'quote sym)))]
    source))

^::clerk/no-cache
(clerk/with-viewer {:render-fn (render-fn 'cljs-render-fn-source/render-fn)
                    :fetch-fn (fn [_ x] x)}
  [{:a 1} ])
