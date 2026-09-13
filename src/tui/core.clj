(ns tui.core
  "Terminal-first AI agent harness entry point.
   Inspired by OpenCode architecture. Built ground-up in Clojure.
   Licensed under MIT."
  (:require [tui.auth :as auth]
            [tui.zen :as zen]
            [tui.zen-oauth :as zen-oauth]
            [tui.gateway :as gateway]
            [tui.tools :as tools]
            [tui.stream :as stream]
            [clojure.edn :as edn]
            [clojure.string :as str])
  (:gen-class))

(def version "0.1.0")

(defn license-notice []
  "tui 0.1.0 — Copyright (C) 2026 Nur Azhar. Licensed under MIT. See LICENSE.")

(defn default-config []
  {:auth {:provider :clerk
          :challenge-url "https://auth.nurazhar.com/verify"
          :local-port 8080
          :session-ttl-hours 8}
   :model {:provider-url "https://opencode.ai/zen/v1"
           :model-name "opencode-zen-free"
           :api-key-env "OPENCODE_ZEN_KEY"}
   :agent {:max-context-tokens 32000
           :tool-timeout-ms 15000
           :allowed-tools ["sh" "git" "cat" "ls"]}
   :gateway {:backend-url "http://127.0.0.1:4096"}})

(defn config-path
  []
  (str (System/getProperty "user.home") "/.config/tui/config.edn"))

(defn load-config
  "Load EDN config merged shallowly over defaults. Missing/unreadable file
   yields defaults — never throws."
  ([] (load-config (config-path)))
  ([path]
   (try (merge (default-config) (edn/read-string (slurp path)))
        (catch Exception _ (default-config)))))

(defn prune-context
  "Deterministic context budgeting: truncate large outputs to budget."
  [s max-tokens]
  (let [approx-chars (* max-tokens 4)]
    (if (> (count s) approx-chars)
      (str (subs s 0 approx-chars) "\n...[truncated]")
      s)))

(defn -main [& args]
  (println (license-notice))
  (if (some #{"serve"} args)
    (let [cfg (load-config)
          secret (or (System/getenv "TUI_JWT_SECRET") "dev-secret-change-me")
          resolve #(zen-oauth/resolve-credential
                    {:token-path (zen-oauth/default-token-path)
                     :api-key-env (get-in cfg [:model :api-key-env])
                     :read-fn slurp
                     :getenv (fn [k] (System/getenv k))
                     :now-ms (System/currentTimeMillis)})]
      (gateway/serve! {:local-port (get-in cfg [:auth :local-port])
                       :backend-url (get-in cfg [:gateway :backend-url])
                       :secret secret
                       :resolve-cred-fn resolve}))
    (do
      (println "TUI agent harness v" version "— type 'exit' to quit.")
      (println "Auth provider:" (get-in (default-config) [:auth :provider]))
      (println "Run with 'serve' to start the gateway (needs TUI_JWT_SECRET).")
      (flush))))
