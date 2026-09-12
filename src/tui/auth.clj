(ns tui.auth
  "OIDC / passkey auth boundary. Providers: :clerk :auth0 :keycloak.
   Pure helpers are fully tested; network verification is injected."
  (:require [clojure.string :as str]))

(def supported-providers #{:clerk :auth0 :keycloak})

(defn valid-provider? [p] (contains? supported-providers p))

(defn loopback-url [port] (str "http://localhost:" port "/auth"))

(defn session-expired?
  "Pure: true if now-ms is past issued-at-ms + ttl-hours."
  [issued-at-ms ttl-hours now-ms]
  (> now-ms (+ issued-at-ms (* ttl-hours 3600 1000))))

(defn redact-principal
  "Never log full principals; keep first 3 chars."
  [principal]
  (if (and (string? principal) (> (count principal) 3))
    (str (subs principal 0 3) "***")
    "***"))

(defn build-challenge
  "Pure: build OIDC challenge map for loopback handoff."
  [{:keys [provider local-port challenge-url]} state]
  {:pre [(valid-provider? provider)]}
  {:provider provider
   :redirect (loopback-url local-port)
   :challenge-url challenge-url
   :state state})
