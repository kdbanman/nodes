#nodes
3D rdf data explorer

##the point
this is an application designed with rdf users in mind.
if you've ever copied and pasted a URI from an ugly RDF-XML document, then wondered why such a silly process hasn't been automated, then you know why this application was written.

the basic workflow of nodes is to:

- pull in RDF data that has been [published to the web](http://linkeddatabook.com/book)
- prune irrelevant or uninteresting data
- highlight important or interesting data
- retrieve more data about insufficiently detailed entities or relationships
- (near future) save the collected data in a standard format

there are a few basic UI systems that are in place to help you work that flow:

- a main view window containing 3D rendered nodes and edges representing the imported data whose controls are:
    - right click and drag to rotate the graph
    - scroll wheel to zoom in or out (or click and drag the scroll wheel)
    - click a node or edge to select it
    - click and drag a node or edge to move it (or to move the whole selection)
    - click and drag a rectangle around nodes or edges to select them
    - hold shift to add to the selection
- a Control Panel for
    - importing data about a URI from a direct HTTP request or from a SPARQL endpoint
    - a context-aware selection menu to change the current data selection using graph algorithms or data queries
    - transforming the appearance of selected data (color, size, location, label presence)
    - deleting selected data
- an Information Panel for
    - displaying human-readable text information about the data being interacted with in the main veiw
    - displaying an event log for the application


##oi, developers:

if you're tempted to use the git hammer, `git add -A`, don't.  this project doesn't want your IDE transients, your filesystem baggage, or anything else forgotten by the `.gitignore`.

as an alternative, there is a shell script called `gitty` in the main project directory.  run it with `sh gitty "commit message"` from the main project directory:

- first, it will stage only the following:
    - source files
    - resources
    - the readme
    - the todo
    - the bug log
    - the gitignore
    - the gitty script itself
    - all file deletions
- then, it will show you what will be committed with a `git status` echo
- finally, it will commit said changes to the present branch

###selection modifier and modifier set development

the selection modifier menu dynamically populates with actions to modify the current selection.
each menu item corresponds to a different selection modifier, and the items are inserted into the menu if they are compatible with the current selection.
to create one, write and compile a java class that inherits from one of the abstract classes `Modifier` or `ModifierSet`.
for nodes to see your class, add its name to `resources/modifier_registry` or `resources/modifierset_registry`, respectively.

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

