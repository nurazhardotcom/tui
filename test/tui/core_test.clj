(ns tui.core-test (:require [clojure.test :refer [deftest is testing]] [tui.core :as core]))

(deftest default-config-test
  (testing "sane defaults"
    (let [c (core/default-config)]
      (is (= :clerk (get-in c [:auth :provider])))
      (is (= 8080 (get-in c [:auth :local-port])))
      (is (= 32000 (get-in c [:agent :max-context-tokens]))))))

(deftest prune-context-test
  (testing "short passthrough"
    (is (= "hi" (core/prune-context "hi" 100))))
  (testing "truncates long"
    (let [s (apply str (repeat 1000 "x"))]
      (is (clojure.string/includes? (core/prune-context s 10) "[truncated]")))))

(deftest license-notice-test
  (is (clojure.string/includes? (core/license-notice) "AGPL")))
