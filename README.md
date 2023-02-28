# Aircraft Maintenance Planning System                                                                                                                                                               
This project contains software modules produced by the Doprava 2020 CSAT project. This includes the software for generating maintenance plans and the aircraft maintenance planning ontology model. We use `PROJECT_ROOT` to reference to the directory, where this README file is located.

The repository contains two main parts -- [Planning Backend](#planning-backend) and [Model](#model).


## Planning Backend

Aircraft Maintenance Planning Backend located in folder [aircraft-maintenance-planning](./aircraft-maintenance-planning) is dockerized Java based application providing REST API for different tasks related to aircraft maintenance. [Plan manager](https://github.com/kbss-cvut/csat-maintenance-planner) is frontend application build over this REST API.

## Model

Aircraft Maintenance Planning Model located in folder [aircraft-maintenance-planning-model](./aircraft-maintenance-planning-model) contains  conceptualization of related concepts that are used within Planning Backend. The conceptualization is implemented using Semantic Web ontologies. The folder contains also scripts to manage those ontologies and data compliant with those ontologies.

-----
This work has been supported by the grant [No. CK01000204 "Improving effectiveness of aircraft maintenance planning and execution"](https://starfos.tacr.cz/en/project/CK01000204) of Technology Agency of the Czech Republic.
