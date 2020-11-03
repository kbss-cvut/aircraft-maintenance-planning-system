@echo off
call jena-cmd
call tarql -e utf-8 -d ; to-termit-skos-vocabulary.sparql %1  > slovnik-tmp.ttl
call riot --formatted=turtle slovnik-tmp.ttl > slovnik.ttl
del slovnik-tmp.ttl
@echo on