(ns tui.zen-test (:require [clojure.test :refer [deftest is testing]] [tui.zen :as zen]))

(deftest payload-test
  (let [p (zen/chat-payload "m" [{:role :user :content "hi"}])]
    (is (= "m" (:model p)))
    (is (= [{:role "user" :content "hi"}] (:messages p)))
    (is (true? (:stream p)))))

(deftest redact-key-test
  (is (= "****7890" (zen/redact-key "abcd1234567890")))
  (is (= "****" (zen/redact-key nil))))

(deftest header-test
  (let [h (zen/auth-header "k")]
    (is (= "Bearer k" (get h "Authorization")))))

(deftest endpoint-test
  (is (= "https://opencode.ai/zen/v1/chat" (zen/endpoint zen/default-endpoint "/chat"))))
