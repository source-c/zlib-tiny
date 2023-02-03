(ns zlib-tiny.checksum
  (:require [clojure.test :refer :all]
            [zlib-tiny.core :refer :all]
            [zlib-tiny.utils :refer :all]))

(def test-string "test it!")

(deftest checksums
  (let [input "123456789"
        bs (.getBytes input)]
    (println "Test input string:" input)
    (println "Test input bytes:" (hexlify bs))
    (testing "CRC32"
      (println "CRC32 checks:")
      (is (= 3421780262 (time (crc32 bs))) "CRC32 mismatch"))
    (testing "CRC32C"
      (println "CRC32C checks:")
      (is (= 3808858755 (time (crc32c bs))) "CRC32C mismatch"))
    (testing "Adler32"
      (println "Adler32 checks:")
      (is (= 152961502 (time (adler32 bs))) "Adler32 mismatch"))
    (testing "CRC64"
      (println "CRC64 checks:")
      (is (= -7395533204333446662 (time (crc64 bs))) "CRC64 mismatch"))))

(deftest digests
  (testing "MD5"
    (is (= "f4214812f0247f69661fd29e0fca6496" (-> test-string
                                                  str->bytes
                                                  md5
                                                  hexlify))))
  (testing "SHA1"
    (is (= "1393ce5dfcf39109a420eb583ecfdeacc28c783a"
           (-> test-string
               str->bytes
               sha-1
               hexlify))))
  (testing "SHA256"
    (is (= "9c507d01834b2749d088122a7b3d200957f9b25579b5ce6b490e3b2067ee4f66"
           (-> test-string
               str->bytes
               sha-256
               hexlify))))
  (testing "SHA384"
    (is (= "6e5cc5271b2255f2cf4154c3170c5fb09059c79d28d182ac2caa59bd607ea87c09637d8f2f7b400ac80810f13027716a"
           (-> test-string
               str->bytes
               sha-384
               hexlify))))
  (testing "SHA512"
    (is (= "15353093ef47d2eadefc55d7bc641b6f1150e0b28a609d2368394748091f20b9125e98fe0603b2fbe57f9d65a9b286a8d0dbf70e8f597525051b6f9220e9b61f"
           (-> test-string
               str->bytes
               sha-512
               hexlify)))))
