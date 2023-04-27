#!/bin/bash


#curl -X 'POST' -d 'historyFrom=2023-02-22T18:10:01.759356Z&historyTo=2023-02-24T18:10:02.415856Z'  'https://kbss.felk.cvut.cz/csat/api/update/start'

#curl -X 'POST' -d 'skipImport=true'  'https://kbss.felk.cvut.cz/csat/api/update/start'
curl -X 'POST' -d 'skipImport=true&forceUpdate=true'  'https://kbss.felk.cvut.cz/csat/api/update/start'
