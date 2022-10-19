#!/bin/bash


# TODO see https://github.com/kbss-cvut/s-pipes/issues/122 and sp-csat.sh


docker pull ghcr.io/kbss-cvut/aircraft-maintenance-planning-system/s-pipes-engine:latest
docker run -d -p 8080:8080 ghcr.io/kbss-cvut/aircraft-maintenance-planning-system/s-pipes-engine:latest
