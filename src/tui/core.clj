(ns tui.core
  "Terminal-first AI agent harness entry point.
   Inspired by OpenCode architecture. Built ground-up in Clojure.
   Licensed under MIT."
  (:require [tui.auth :as auth]
            [tui.zen :as zen]
            [tui.tools :as tools]
            [tui.stream :as stream]
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
           :allowed-tools ["sh" "git" "cat" "ls"]}})

(defn prune-context
  "Deterministic context budgeting: truncate large outputs to budget."
  [s max-tokens]
  (let [approx-chars (* max-tokens 4)]
    (if (> (count s) approx-chars)
      (str (subs s 0 approx-chars) "\n...[truncated]")
      s)))

(defn -main [& args]
  (println (license-notice))
  (println "TUI agent harness v" version "— type 'exit' to quit.")
  (println "Auth provider:" (get-in (default-config) [:auth :provider]))
  (flush))
