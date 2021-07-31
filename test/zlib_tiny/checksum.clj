(ns zlib-tiny.checksum
  (:require [clojure.test :refer :all]
            [zlib-tiny.core :refer :all])
  (:import (org.apache.commons.codec.binary Hex)))

(def test-string "test it!")

(deftest checksums
  (testing "CRC32"
    (is (= 3421780262 (crc32 (.getBytes "123456789")))))
  (testing "CRC64"
    (is (= -7395533204333446662 (crc64 (.getBytes "123456789"))))))

(deftest digests
  (testing "MD5"
    (is (= "f4214812f0247f69661fd29e0fca6496" (-> test-string
                                                  str->bytes
                                                  md5
                                                  Hex/encodeHex
                                                  (String.)))))
  (testing "SHA1"
    (is (= "1393ce5dfcf39109a420eb583ecfdeacc28c783a"
           (-> test-string
               str->bytes
               sha-1
               Hex/encodeHex
               (String.)))))
  (testing "SHA256"
    (is (= "9c507d01834b2749d088122a7b3d200957f9b25579b5ce6b490e3b2067ee4f66"
           (-> test-string
               str->bytes
               sha-256
               Hex/encodeHex
               (String.))))))
