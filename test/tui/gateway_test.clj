(ns tui.gateway-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [tui.gateway :as gw]))

(def secret "test-secret-please-rotate")

(deftest jwt-roundtrip-test
  (let [claims {:sub "azh" :exp 9999999999}
        tok (gw/sign-session claims secret)]
    (is (= claims (gw/verify-session tok secret)))))

(deftest jwt-tampered-test
  (let [tok (gw/sign-session {:sub "azh"} secret)]
    (is (nil? (gw/verify-session (str tok "x") secret)))
    (is (nil? (gw/verify-session tok "wrong-secret")))
    (is (nil? (gw/verify-session nil secret)))
    (is (nil? (gw/verify-session "" secret)))))

(deftest bearer-extract-test
  (is (= "abc" (gw/bearer-token {"authorization" "Bearer abc"})))
  (is (= "abc" (gw/bearer-token {"Authorization" "Bearer abc"})))
  (is (nil? (gw/bearer-token {})))
  (is (nil? (gw/bearer-token {"authorization" "Basic xyz"}))))

(deftest auth-middleware-test
  (let [ok (gw/wrap-jwt-auth (fn [req] {:status 200 :body (:sub (:identity req))}) secret)
        tok (gw/sign-session {:sub "azh"} secret)]
    (is (= 200 (:status (ok {:headers {"authorization" (str "Bearer " tok)}}))))
    (is (= "azh" (:body (ok {:headers {"authorization" (str "Bearer " tok)}}))))
    (is (= 401 (:status (ok {:headers {}}))))
    (is (= 401 (:status (ok {:headers {"authorization" "Bearer junk"}}))))))

(deftest proxy-build-test
  (let [built (gw/build-proxy-request
               {:backend-url "http://127.0.0.1:4096" :token "abcd1234567890"}
               {:request-method :post :uri "/session" :query-string "a=1" :body "x"})]
    (is (= "http://127.0.0.1:4096/session?a=1" (:url built)))
    (is (= "Bearer abcd1234567890" (get (:headers built) "Authorization")))
    (testing "log form carries only the redacted credential"
      (is (= "****7890" (get-in built [:log :credential])))
      (is (not (str/includes? (pr-str (:log built)) "abcd1234567890"))))))

(deftest proxy-handler-test
  (let [handler (gw/proxy-handler
                 {:backend-url "http://127.0.0.1:4096"
                  :secret secret
                  :resolve-cred-fn (fn [] {:ok true :scheme :api-key
                                           :token "abcd1234567890"
                                           :redacted "****7890"})})
        tok (gw/sign-session {:sub "azh"} secret)
        sent (atom nil)
        fake-send (fn [req] (reset! sent req) {:status 200 :headers {} :body "ok"})]
    (testing "unauthorized without JWT"
      (is (= 401 (:status (handler {:headers {}})))))
    (testing "503 when no Zen credential"
      (let [h (gw/proxy-handler {:backend-url "http://127.0.0.1:4096"
                                 :secret secret
                                 :resolve-cred-fn (fn [] {:ok false :reason :no-credential})})]
        (is (= 503 (:status (h {:headers {"authorization" (str "Bearer " tok)}}))))))
    (testing "forwards with injected credential"
      (with-redefs [gw/forward! (fn [proxy-req] (fake-send proxy-req) {:status 200 :headers {} :body "ok"})]
        (let [resp (handler {:request-method :post :uri "/x"
                             :headers {"authorization" (str "Bearer " tok)}})]
          (is (= 200 (:status resp)))
          (is (= "Bearer abcd1234567890" (get (:headers @sent) "Authorization"))))))))
