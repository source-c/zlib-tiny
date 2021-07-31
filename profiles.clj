{:dev      {:global-vars       {*warn-on-reflection* true}

            :source-paths      #{"src-clj"}
            :java-source-paths #{"src-java"}
            :resource-paths    ["resources"]

            :jar-exclusions    [#"\.java"]

            :target-path       "target/%s"
            :clean-targets     ^{:protect false} [:target-path]

            :dependencies      [[commons-codec "1.15"]]

            :plugins           [[org.apache.maven.wagon/wagon-ssh-external "3.4.3"]
                                [org.apache.maven.wagon/wagon-http-lightweight "3.4.3"]]}

 :provided {:dependencies [[org.clojure/clojure "1.10.3"]]}

 :jar      {:aot :all}}
