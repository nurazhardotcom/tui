(ns tui.auth-test (:require [clojure.test :refer [deftest is testing]] [tui.auth :as auth]))

(deftest provider-test
  (is (auth/valid-provider? :clerk))
  (is (auth/valid-provider? :keycloak))
  (is (not (auth/valid-provider? :icp-custom))))

(deftest loopback-test
  (is (= "http://localhost:8080/auth" (auth/loopback-url 8080))))

(deftest expiry-test
  (is (auth/session-expired? 0 8 (* 9 3600 1000)))
  (is (not (auth/session-expired? 0 8 (* 7 3600 1000)))))

(deftest redact-test
  (is (= "azh***" (auth/redact-principal "azhar-icp-01")))
  (is (= "***" (auth/redact-principal nil))))

(deftest challenge-test
  (let [c (auth/build-challenge {:provider :clerk :local-port 8080 :challenge-url "https://x/verify"} "s1")]
    (is (= :clerk (:provider c)))
    (is (= "http://localhost:8080/auth" (:redirect c)))))
