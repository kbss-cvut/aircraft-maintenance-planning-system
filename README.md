# aircraft-maintenance-planning-system                                                                                                                                                               
This project contains software modules produced by the Doprava 2020 CSAT project. This includes the software for generating maintenance plans and the aircraft maintenance planning ontology model.


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

#### RDF resources

- IRI fragment should be separated by "/" instead of "#"
- resources should be lower-case with words separated by "-"
- prefixes should be globally unique
- ontology IRI should start with http://onto.fel.cvut.cz/ontologies/
