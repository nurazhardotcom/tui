(ns tui.test-runner
  "Runs all tui test namespaces concurrently via futures, preserves determinism."
  (:require [clojure.test :as t]
            tui.core-test tui.auth-test tui.zen-test tui.tools-test tui.stream-test))

(def test-namespaces
  '[tui.core-test tui.auth-test tui.zen-test tui.tools-test tui.stream-test])

(defn -main [& _]
  (println "Running" (count test-namespaces) "test namespaces in parallel (futures)...")
  (let [futures (doall (map (fn [ns-sym]
                              (future
                                (let [{:keys [fail error] :as res} (t/run-tests ns-sym)]
                                  (assoc res :ns ns-sym :failed? (pos? (+ fail error))))))
                            test-namespaces))
        results (mapv deref futures)
        total-fail (reduce + (map :fail results))
        total-error (reduce + (map :error results))
        total-test (reduce + (map :test results))]
    (doseq [{:keys [ns test pass fail error]} results]
      (println (format "%-18s tests=%d pass=%d fail=%d error=%d" ns test pass fail error)))
    (println (format "TOTAL tests=%d fail=%d error=%d" total-test total-fail total-error))
    (if (zero? (+ total-fail total-error))
      (do (println "ATTESTATION: 0 failures, 0 errors — PASS") (System/exit 0))
      (do (println "ATTESTATION: FAIL") (System/exit 1)))))
