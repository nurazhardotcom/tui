(ns tui.zen
  "OpenCode Zen / OpenAI-compatible payload adapter.
   Pure mapping + key redaction; HTTP kept at boundary."
  (:require [clojure.string :as str]))

(def default-endpoint "https://opencode.ai/zen/v1")

(defn endpoint [base path] (str base path))

(def responses-path "/responses")

(def contributor-model "muse-spark-1.3-contributor-free")

(defn responses-url
  "Full URL for the Responses API (the Zen route serving Muse Spark contributor-free)."
  [base]
  (endpoint base responses-path))

(defn messages->input
  "Normalize internal [{:role :user :content}] messages to Responses input items."
  [messages]
  (mapv (fn [{:keys [role content]}]
          {:role (name role) :content content})
        messages))

(defn responses-payload
  "Map internal messages to the OpenAI Responses API schema.
   `input` is a string or a messages vector (normalized via messages->input)."
  [model input & {:keys [stream tools] :or {stream true}}]
  (cond-> {:model model
           :input (if (string? input) input (messages->input input))
           :stream (boolean stream)}
    tools (assoc :tools tools)))

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
