(ns project-facts.util)

(defn ->maps [tabular-data]
  (map zipmap
       (->> (first tabular-data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
       (map
        (partial map (fn [d] (or (parse-long d) d)))
        (rest tabular-data))))
