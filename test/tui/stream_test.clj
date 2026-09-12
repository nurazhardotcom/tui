(ns tui.stream-test (:require [clojure.test :refer [deftest is testing]] [tui.stream :as s]))

(deftest assemble-test
  (is (= "helloworld" (s/assemble ["hello" "world"])))
  (is (= "helloworld" (s/assemble-parallel ["hello" "world"])))
  (is (= "" (s/assemble []))))

(deftest parallel-equivalence-test
  (let [chunks (mapv str (range 100))]
    (is (= (s/assemble chunks) (s/assemble-parallel chunks)))))

(deftest done-test
  (is (s/done-marker? "data: [DONE]"))
  (is (not (s/done-marker? "data: hello"))))
