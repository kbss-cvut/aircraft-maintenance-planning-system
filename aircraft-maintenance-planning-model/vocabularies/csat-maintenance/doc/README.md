# Diagram summary
The diagrams are devided into two main categories **Reference Maintenance planning model** 
and **CSAT specific models** which should be based on the former.   

## Reference Maintenance planning models
### Root maintenance planning model
This model contains the main concepts of maintenance planning. 
* maintenance-planning-model-B.graphml

### Model Alignments
* maintenance-model-aligned-with-romain.graphml

This diagram contains alignment of our ontology with the **Romain** ontology.
This alignment inspired the creation of the models:
* Observation model
* Degradation model

### Degradation Model
A UFO based model of degradation 
  * *degradation-vulnerability.graphml*

### Task order restriction Model 
Tasks are restricted based on situations, e.g. some tasks require that el. power system is on or off and some tasks turn on and off the system.
  * *task-order-restriction.graphml* - model describing restrictions
  * *task-order-restriction-01.png* - example table with specifications of task order restrictions
  * *task-order-restriction-02.png* - example table with specifications of task order restrictions

### Observation Model
* observation-and-finding-pattern.graphml - contains UFO based model of observation (e.g. finding), with examples

## CSAT specific models
*CSAT_data_input.graphml*
* this diagram describes:
  * the input data from CSAT, i.e. the table of 
  worklog sessions and the table of WO/TC task steps.
  * Organizational aspects of CSAT - workshop personal management hierarchy (line manager, team leader), 
    division of personnel according to expertise (scopes) and maintenance task categorization
  * TODO - align with the reference model
  
*maintenance-planning-model-csat.graphml* 
* TODO - merge with *CSAT_data_input.graphml*

# TODOs
* extend the observation pattern with UFO model for:
  * damage affects function
  * requirements and their violation of damaged states