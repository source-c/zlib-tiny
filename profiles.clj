{:dev      {:global-vars   {*warn-on-reflection* true}

            :target-path   "target/%s"
            :clean-targets ^{:protect false} [:target-path]

            :plugins       []}

 :provided {:dependencies      [[org.clojure/clojure "1.12.4"]]
            :source-paths      #{"src-clj"}
            :java-source-paths #{"src-java"}
            :resource-paths    ["resources"]

            :javac-options     ["--release" "9" "-g:none"]

            :jar-exclusions    [#"\.java"]}

 :jar      {:aot :all}}
