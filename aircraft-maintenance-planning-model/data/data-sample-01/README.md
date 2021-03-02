#Description
This pipeline converts data from excel or csv file to RDF.
There are two types of tables which can be converted to RDF using the pipelines in this folder:

* List1 - data used for planning and scheduling. contains task execution log data for maintenance plan executions called workpackage (WP) in CSAT
* List2 - data used for nlp (extraction of components and failures). Contains data about the task execution steps, e.g. step number, work order text, work order action

Input data can be found at:
* [sequence data](https://drive.google.com/drive/folders/1AT5nFmA9UsCfMeuqu7z6rICfchGlhnt3?usp=sharing)
* [work order text data](https://drive.google.com/drive/folders/1PigRhD6mzq8okK8gw2911Ql1e-T65ndJ?usp=sharing)

##Implementation details
This folder contains two similar pipelines each processing one of the two input tables List1 and List2.

In both cases the pipeline consists of two steps performed manually. Related files can be found in the script folder.

1. onto refine script stored in a json file. This file is executed using the import feature of GraphDB, ontorefine.
2. sparql construct query which transforms the data loaded into ontorefine to rdf. 
    1. Note that the service url must be changed to match the service url of the ontorefine project created to load the excel data.

Pipeline files are found in the script folder. 

SPARQL queries for a newer version of graphdb (e.g. graphdb 9.4.1) are found in the 
graphdb-9.4.1 folder. The newer version of graphdb uses a different syntax to bind 
variables to column names.

The naming convention of the script files is:
* name_of_input_file-extension-list_name-script_name.xxx
* for example the following names correspond to the pipeline converting data in List1 to RDF:
    * data_test-xlsx-list1-preprocessing-refine.json
    * data_test-xlsx-list2-to-rdf-graphdb.sparql

# Output
The output of the pipeline if executed should be placed in the target folder.
Currently the output contains:
* schema - folder which contains the columns of the tables in order in which they found in the input data.