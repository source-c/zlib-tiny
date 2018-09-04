{:dev     {:global-vars         {*warn-on-reflection* true}

           :source-paths        #{"src"}
           :resource-paths      ["resources"]

           :target-path         "target/%s"
           :clean-targets       ^{:protect false} [:target-path]

           :plugins             [[lein-ancient "0.6.15"]
                                 [org.apache.maven.wagon/wagon-ssh-external "3.0.0"]
                                 [org.apache.maven.wagon/wagon-http-lightweight "3.0.0"]]

           :dependencies        [[org.clojure/clojure "1.9.0"]]}

 :uberjar {:aot      :all
           :jvm-opts #=(eval
                         (concat ["-Xmx1G"]
                           (let [version (System/getProperty "java.version")
                                 [major _ _] (clojure.string/split version #"\.")]
                             (if (>= (Integer. major) 9)
                               ["--add-modules" "java.xml.bind"]
                               []))))}}