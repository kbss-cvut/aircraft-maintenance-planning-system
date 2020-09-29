@echo off

call tarql -d=; -q ata-chapter-vs-csat-group.sparql ata-chapters-vs-csat-groups.csv > ata-chapters-vs-csat-groups.ttl

@echo on