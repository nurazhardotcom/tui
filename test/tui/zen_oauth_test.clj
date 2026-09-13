(ns tui.zen-oauth-test
  (:require [clojure.test :refer [deftest is testing]]
            [tui.zen-oauth :as zo]))

(def now 1700000000000)

(defn base-opts [overrides]
  (merge {:token-path "/fake/auth.json"
          :api-key-env "OPENCODE_ZEN_KEY"
          :read-fn (fn [_] nil)
          :getenv (fn [_] nil)
          :now-ms now}
         overrides))

(deftest oauth-hit-test
  (let [r (zo/resolve-credential
           (base-opts {:read-fn (fn [_] "{\"token\":\"abcd1234567890\",\"expires_at\":9999999999999}")}))]
    (is (:ok r))
    (is (= :oauth (:scheme r)))
    (is (= "****7890" (:redacted r)))))

(deftest oauth-expired-falls-back-test
  (let [r (zo/resolve-credential
           (base-opts {:read-fn (fn [_] "{\"access_token\":\"old-token-1234\",\"expires_at\":1000}")
                       :getenv (fn [k] (when (= k "OPENCODE_ZEN_KEY") "zen-key-9999"))}))]
    (is (:ok r))
    (is (= :api-key (:scheme r)))
    (is (= "****9999" (:redacted r)))))

(deftest api-key-only-test
  (let [r (zo/resolve-credential
           (base-opts {:getenv (fn [_] "env-key-abcd")})) ]
    (is (:ok r))
    (is (= :api-key (:scheme r)))))

(deftest nothing-test
  (let [r (zo/resolve-credential (base-opts {}))]
    (is (false? (:ok r)))
    (is (= :no-credential (:reason r)))))

(deftest corrupt-file-falls-back-test
  (testing "unparseable token file degrades to env fallback, never throws"
    (let [r (zo/resolve-credential
             (base-opts {:read-fn (fn [_] "not-json{{{")
                         :getenv (fn [_] "env-key-abcd")}))]
      (is (:ok r))
      (is (= :api-key (:scheme r))))))
