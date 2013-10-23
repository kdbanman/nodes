3D rdf data explorer

##oi, developers:

if you're tempted to use the git hammer, `git add -A`, don't.  i don't want your IDE transients, your filesystem baggage, or anything else i forgot to put in the .gitignore.

there is a shell script called gitty in the main project directory.  run it with `sh gitty "commit message"` from the main project directory.  it will:

- stage only the following:
    - source files
    - resources
    - the readme
    - the todo
    - the bug log
    - the gitignore
    - the gitty script itself
    - all file deletions
- show you what was committed with a `git status` echo

###dependencies

- java 1.7 (verify with `java -version` or look around in your IDE)
- [processing 2.x](https://processing.org/download/) (tested up to 2.0.3)
    - all 15 jars from processing-2.\*/core/library/
- [controlP5 2.0.4](http://code.google.com/p/controlp5/downloads/list)
    - controlP5.jar from controlP5-2.0.4/controlP5/library/
- [peasycam](https://github.com/jeffg2k/peasycam/blob/master/distribution/peasycam.zip?raw=true)
    - peasycam.jar from peasycam/library/
- [apache jena](http://www.apache.org/dist/jena/binaries/) (tested with 2.11.0)
    - all 14 jars from apache-jena-2.11.0/lib/

