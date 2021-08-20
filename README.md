# Aircraft Maintenance Planning System                                                                                                                                                               
This project contains software modules produced by the Doprava 2020 CSAT project. This includes the software for generating maintenance plans and the aircraft maintenance planning ontology model. We use `PROJECT_ROOT` to reference to the directory, where this README file is located.


## Software

## Model

### Name-ing conventions

#### Files and directories

- RDF data should be saved in *.ttl files
- file containing ontology should be idealy named same as "fragment of url"
    - e.g. http://onto.fel.cvut.cz/ontologies/csat-maintenance in csat-maintenance.ttl

#### Directory structure

- ./bin - for scripts
- ./input - for output of scripts
- ./output - for source files of scripts
- ./doc - documentation of ontologies

#### Prefixes

- prefixes should be globally unique

#### RDF resources 

- resources should be lower-case with words separated by "-"
- IRI fragment should be separated by "/" instead of "#"
- ontology IRI should start with http://onto.fel.cvut.cz/ontologies/
- resource IRI cannot end with "/" or "#" at the end of the IRI (applies also to ontology IRI)


#### OWL resources

- object property should be named with "has-" and "is-" prefix
- datatype property should be called without "has-" and "is-" prefix


### Transformation scripts

Script to transform data into ontologies are possible to edit using [SPipes editor](https://github.com/chlupnoha/s-pipes-editor-ui).
To run it, do the following steps:
1) create a new folder (we will call it `SPE_ROOT`)
2) clone project [SPipes modules](https://blaskmir@kbss.felk.cvut.cz/gitblit/r/s-pipes-modules.git
   ) into `SPE_ROOT` folder 
3) clone project [SPipes editor](https://github.com/chlupnoha/s-pipes-editor-ui) into `SPE_ROOT` folder
4) edit `SPE_ROOT/s-pipes-editor-ui/docker-compose.yml`, so that

    - both SCRIPTPATHS and CONTEXTS_SCRIPTPATHS points to directories `SPE_ROOT/s-pipes-modules` 
      and `PROJECT_ROOT/aircraft-maintenance-planning-model/data`. Note, that the first variable 
      uses ";" as delimiter, while the second one uses ",".
    - RDF4J_PCONFIGURL points to `PROJECT_ROOT/aircraft-maintenance-planning-model/scripts/config.ttl`
    - SCRIPTRULES=`PROJECT_ROOT/aircraft-maintenance-planning-model/scripts/rules`

