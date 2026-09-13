(ns tui.gateway
  "Hybrid gateway: verifies the short-lived local JWT, injects the Zen
   credential server-side, and proxies everything else to the
   `opencode web` backend.
   Raw secrets never reach logs — only redacted forms. Pure builders are
   unit-tested; the HTTP send fn is injected."
  (:require [buddy.sign.jwt :as jwt]
            [clj-http.client :as http]
            [clojure.string :as str]
            [ring.adapter.jetty :as jetty]
            [tui.zen :as zen]))

(defn sign-session
  "Issue a local session JWT (HS256)."
  [claims secret]
  (jwt/sign claims secret {:alg :hs256}))

(defn verify-session
  "Returns claims map, or nil on any verification failure."
  [token secret]
  (when (and (string? token) (seq token) (string? secret) (seq secret))
    (try (jwt/unsign token secret {:alg :hs256})
         (catch Exception _ nil))))

(defn bearer-token
  "Extract the Bearer token from Ring headers (case-insensitive), or nil."
  [headers]
  (some (fn [[k v]]
          (when (= "authorization" (str/lower-case (str k)))
            (second (re-matches #"(?i)Bearer\s+(.+)" (str v)))))
        headers))

(defn unauthorized []
  {:status 401
   :headers {"Content-Type" "application/json"}
   :body "{\"error\":\"unauthorized\"}"})

(defn wrap-jwt-auth
  "Ring middleware: reject without a valid session JWT, else assoc :identity."
  [handler secret]
  (fn [req]
    (if-let [claims (verify-session (bearer-token (:headers req)) secret)]
      (handler (assoc req :identity claims))
      (unauthorized))))

(defn build-proxy-request
  "Pure: map a Ring request to an outbound backend request.
   :log carries only the redacted credential — never log :headers."
  [{:keys [backend-url token]} {:keys [request-method uri query-string body]}]
  (let [url (str backend-url uri (when query-string (str "?" query-string)))]
    {:method request-method
     :url url
     :headers {"Authorization" (str "Bearer " token)
               "Content-Type" "application/json"}
     :body body
     :log {:method request-method
           :url url
           :credential (zen/redact-key token)}}))

(defn forward!
  "Send a built proxy request. send-fn defaults to clj-http (streaming, so
   SSE from Zen passes through untouched)."
  ([proxy-req] (forward! http/request proxy-req))
  ([send-fn {:keys [method url headers body]}]
   (let [resp (send-fn {:method method :url url :headers headers :body body
                        :as :stream :throw-exceptions false
                        :conn-timeout 5000 :socket-timeout 60000})]
     {:status (:status resp)
      :headers (select-keys (:headers resp) ["content-type"])
      :body (:body resp)})))

(defn no-credential []
  {:status 503
   :headers {"Content-Type" "application/json"}
   :body "{\"error\":\"no-zen-credential\"}"})

(defn proxy-handler
  "Ring handler: JWT gate -> resolve Zen credential -> proxy to backend.
   resolve-cred-fn: (fn [] credential-map) as returned by tui.zen-oauth."
  [{:keys [backend-url secret resolve-cred-fn]}]
  (wrap-jwt-auth
   (fn [req]
     (let [cred (resolve-cred-fn)]
       (if-not (:ok cred)
         (no-credential)
         (forward! (build-proxy-request {:backend-url backend-url
                                         :token (:token cred)}
                                        req)))))
   secret))

(defn serve!
  "Start the gateway on local-port. Returns the Jetty server (join? false).
   Prints only redacted Startup info."
  [{:keys [local-port backend-url secret resolve-cred-fn]}]
  (println (str "tui gateway -> " backend-url " on :" local-port))
  (flush)
  (jetty/run-jetty (proxy-handler {:backend-url backend-url
                                   :secret secret
                                   :resolve-cred-fn resolve-cred-fn})
                   {:port local-port :join? false}))
