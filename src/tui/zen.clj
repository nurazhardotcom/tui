(ns tui.zen
  "OpenCode Zen / OpenAI-compatible payload adapter.
   Pure mapping + key redaction; HTTP kept at boundary."
  (:require [clojure.string :as str]))

(def default-endpoint "https://opencode.ai/zen/v1")

(defn chat-payload
  "Map internal messages to OpenAI chat schema."
  [model messages & {:keys [stream tools] :or {stream true}}]
  (cond-> {:model model
           :messages (mapv (fn [{:keys [role content]}]
                             {:role (name role) :content content})
                           messages)
           :stream (boolean stream)}
    tools (assoc :tools tools)))

(defn redact-key
  "Redact API key for logs/telemetry. Keeps last 4 only."
  [k]
  (if (and (string? k) (> (count k) 4))
    (str "****" (subs k (- (count k) 4)))
    "****"))

(defn auth-header [api-key]
  {"Authorization" (str "Bearer " api-key)
   "Content-Type" "application/json"})

(defn endpoint [base path] (str base path))
