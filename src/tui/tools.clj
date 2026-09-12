(ns tui.tools
  "Deterministic tool gates: allow-list + non-root + schema validation."
  (:require [clojure.string :as str]))

(defn allowed-tool?
  "Pure: is tool name in allow-list?"
  [allowed tool]
  (boolean (some #(= % tool) allowed)))

(defn root?
  "Pure helper: uid 0 means root."
  [uid] (zero? uid))

(defn validate-invocation
  "Pure gate: returns {:ok true} or {:ok false :reason ...}."
  [{:keys [tool uid allowed]}]
  (cond
    (root? uid) {:ok false :reason "refuse-root"}
    (not (allowed-tool? allowed tool)) {:ok false :reason "deny-allow-list"}
    (str/blank? tool) {:ok false :reason "empty-tool"}
    :else {:ok true}))

(defn sanitize-output
  "Strip ANSI + truncate to max-chars for context budgeting."
  [s max-chars]
  (-> s
      (str/replace #"\u001B\[[;0-9]*m" "")
      (#(if (> (count %) max-chars)
          (str (subs % 0 max-chars) "...[truncated]")
          %))))
