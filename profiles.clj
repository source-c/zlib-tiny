{:dev     {:global-vars    {*warn-on-reflection* true}

           :source-paths   #{"src"}
           :resource-paths ["resources"]

           :target-path    "target/%s"
           :clean-targets  ^{:protect false} [:target-path]

           :plugins        [[org.apache.maven.wagon/wagon-ssh-external "3.4.2"]
                            [org.apache.maven.wagon/wagon-http-lightweight "3.4.2"]]}

 :provided {:dependencies [[org.clojure/clojure "1.10.1"]]}

 :jar     {:aot :all}}
