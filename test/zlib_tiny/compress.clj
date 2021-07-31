(ns zlib-tiny.compress
  (:require [clojure.test :refer :all]
            [zlib-tiny.core :refer :all]))

(def test-string "test it!")

(deftest compressors
  (testing "Checking ZLib"
    (is (= test-string (-> test-string
                           str->bytes
                           deflate
                           inflate
                           force-byte-array
                           bytes->str))))

  (testing "Checking GZip"
    (is (= test-string (-> test-string
                           str->bytes
                           gzip
                           gunzip
                           bytes->str)))))
