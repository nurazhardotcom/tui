(ns tui.zen-oauth
  "Zen credential provider.
   Primary: OAuth token from `opencode auth login` (token file on disk).
   Fallback: env API key (e.g. OPENCODE_ZEN_KEY from `opencode.ai/auth`).
   File/env access is injected so every branch is unit-testable; no network here."
  (:require [clojure.data.json :as json]
            [tui.zen :as zen]))

(defn default-token-path
  []
  (str (System/getProperty "user.home") "/.local/share/opencode/auth.json"))

(defn pick-token
  "Upstream token-file shape is not contractual; accept the known key variants."
  [m]
  (when (map? m)
    (or (:token m) (:access_token m)
        (get m "token") (get m "access_token"))))

(defn pick-expiry
  [m]
  (when (map? m)
    (or (:expires_at m) (:expiry m) (get m "expires_at") (get m "expiry"))))

(defn expired?
  [expires-at now-ms]
  (boolean (and (number? expires-at) (>= now-ms expires-at))))

(defn resolve-credential
  "Returns {:ok true :scheme :oauth|:api-key :token <raw> :redacted <****last4>}
   or {:ok false :reason :no-credential}.
   read-fn: (fn [path] string|nil). getenv: (fn [k] string|nil). now-ms: epoch ms."
  [{:keys [token-path api-key-env read-fn getenv now-ms]}]
  (let [raw (try (read-fn token-path) (catch Exception _ nil))
        parsed (when raw (try (json/read-str raw :key-fn keyword)
                              (catch Exception _ nil)))
        oauth (pick-token parsed)]
    (cond
      (and oauth (not (expired? (pick-expiry parsed) now-ms)))
      {:ok true :scheme :oauth :token oauth :redacted (zen/redact-key oauth)}

      :else
      (let [k (try (getenv api-key-env) (catch Exception _ nil))]
        (if (and (string? k) (seq k))
          {:ok true :scheme :api-key :token k :redacted (zen/redact-key k)}
          {:ok false :reason :no-credential})))))
