(ns status-im.protocol.web3.inbox
  (:require [status-im.protocol.web3.utils :as utils]
            [status-im.native-module.core :as status]
            [status-im.protocol.web3.keys :as keys]
            [taoensso.timbre :as log]))


;; TODO(oskarth): Determine if this is the correct topic or not
;; If it is, use constant in other namespace
(def default-topic "0xaabb11ee")

(def inbox-password "status-offline-inbox")

;; TODO(oskarth): Hardcoded to local enode for preMVP, will be in bootnodes later
(def default-enode "enode://0f51d75c9469de0852571c4618fe151265d4930ea35f968eb1a12e69c12f7cbabed856a12b31268a825ca2c9bafa47ef665b1b17be1ab71de83338c4b7439b24@127.0.0.1:30303")

;; TODO(oskarth): Rewrite callback-heavy code with CSP and/or coeffects
;; NOTE(oskarth): Exact params still being determined
(defn request-messages! [web3 {:keys [enode topic password]
                               :or {enode    default-enode
                                    password inbox-password
                                    topic    default-topic}} callback]
  (status/add-peer
   enode
   (fn [res]
     (log/info "offline inbox: add peer" enode res)
     (keys/get-sym-key web3 password
                       (fn [sym-key-id]
                         (log/info "offline inbox: sym-key-id" sym-key-id)
                         (let [args {:jsonrpc "2.0"
                                     :id      1
                                     :method  "shh_requestMessages"
                                     :params  [{:enode     enode
                                                :topic     topic
                                                :symKeyID  sym-key-id
                                                :from      0
                                                :to        1612505820}]}
                               payload (.stringify js/JSON (clj->js args))]
                           (log/info "offline inbox: request-messages request")
                           (status/call-web3 payload callback)))))))

;; TODO(oskarth): Use web3 binding instead of raw RPC above, pending binding and deps:
;; (.requestMessages (utils/shh web3)
;;                   (clj->js opts)
;;                   callback
;;                   #(log/warn :request-messages-error
;;                             (.stringify js/JSON (clj->js opts)) %))
