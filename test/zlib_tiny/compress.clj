(ns zlib-tiny.compress
  (:require [clojure.test :refer :all]
            [zlib-tiny.core :refer :all]))

(def test-string "test it!")

(defn t-inflate #^bytes [string-to-compress compression-level]
  (-> string-to-compress
      (str->bytes)
      (deflate (or compression-level -1))))

(defn t-deflate [b]
  (-> b
      inflate
      force-byte-array
      bytes->str))

(deftest compressors
  (testing "Checking ZLib"
    (is (= test-string (-> test-string
                           str->bytes
                           deflate
                           inflate
                           force-byte-array
                           bytes->str))))

  (testing "Setting compression level"
    (let [with-highest-level (t-inflate test-string 9)
          with-lower-level (t-inflate test-string 0)]
      (is (> (alength with-lower-level)
             (alength with-highest-level))
          "'with-lower-level wasn't smaller than 'with-highest-level")
      (is (= (t-deflate with-lower-level)
             (t-deflate with-highest-level)))))

  (testing "Checking GZip"
    (is (= test-string (-> test-string
                           str->bytes
                           gzip
                           gunzip
                           bytes->str)))))
