# Running scripts
## Running scripts in the projects
The java project contains scripts which are used for example to transform data into RDF. 
These scripts can be executed on the commandline using the run.bat command line script. For example:

> run cz.cvut.kbss.amaplas.tmpscript

will call the tmpscript class without any arguments. Note that the run command must be on the path.

To prepare scripts for running :
* package the jar application using maven - this will create the run.bat script in the target folder.

To make the run.bat script executable from any location:
* Add the run.bat to the path variable
* Alternatively, if java 11 or higher is installed you can scripts-init.bat (see the bin folder) to the path
    * the script will work in project sub folders.
    * to make the project run in external folders add a config.properties file which sets the property
      >scripts.root.dir=...
      
      to point to the root of a project.
    

# TODO
* create run.sh for linux 
* make script-init.bat run with any java, requires compiling the scripts.java file. 
