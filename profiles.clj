{:dev      {:global-vars   {*warn-on-reflection* true}

            :target-path   "target/%s"
            :clean-targets ^{:protect false} [:target-path]

            :plugins       [[org.apache.maven.wagon/wagon-ssh-external "3.5.2"]
                            [org.apache.maven.wagon/wagon-http-lightweight "3.5.2"]]}

 :provided {:dependencies      [[org.clojure/clojure "1.11.1"]]
            :source-paths      #{"src-clj"}
            :java-source-paths #{"src-java"}
            :resource-paths    ["resources"]

            :javac-options     ["-source" "9" "-target" "9" "-g:none"]

            :jar-exclusions    [#"\.java"]}

 :jar      {:aot :all}}
