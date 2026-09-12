(ns tui.tools-test (:require [clojure.test :refer [deftest is testing]] [tui.tools :as tools]))

(deftest allow-test
  (is (tools/allowed-tool? ["sh" "git"] "git"))
  (is (not (tools/allowed-tool? ["sh"] "sudo"))))

(deftest gate-test
  (is (= {:ok true} (tools/validate-invocation {:tool "git" :uid 1000 :allowed ["git"]})))
  (is (= "refuse-root" (:reason (tools/validate-invocation {:tool "git" :uid 0 :allowed ["git"]}))))
  (is (= "deny-allow-list" (:reason (tools/validate-invocation {:tool "rm" :uid 1000 :allowed ["git"]})))))

(deftest sanitize-test
  (is (= "hi" (tools/sanitize-output "\u001B[31mhi\u001B[0m" 100)))
  (is (clojure.string/includes? (tools/sanitize-output "abcdefgh" 4) "[truncated]")))
