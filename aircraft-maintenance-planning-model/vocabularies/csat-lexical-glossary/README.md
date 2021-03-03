# CSAT Lexical Glossary

The glossary contains components and failures extracted from general lexical vocabulary created in CSAT before this project. Components and failures were selected from the generic lecical vocabulary manually by domain experts.

## Transformation Instructions

To create a termit vocabulary from csv vocabulary:
	1. preparation - create a folder and copy the files to-termit-skos-vocabulary.sparql and build.bat.
	2. in the open the query file to-termit-skos-vocabulary.sparql and change the query accordingly. Here are some of the possible changes that need to be made:
		- the name of the column for the term label, in the example this column is named AJ
		- ?lang - change the language of the terms and other text literals in the vocabulary. Note, the query does not have different language parameters/variables for the different literals. 
		- ?title and ?description - Change the title and description of the vocabulary
		- ?creator and ?created specify the author and the date of creation
		- ?revision - specifies the version of the vocabulary. 
		- ?status - specifies the status of the vocabulary. - TODO is it necessary?
		- ?prefix and ?namespace - specifies the prefix and the namespace of the vocabulary. - TODO is it necessary?
		- ?termIRI and ?termLang - the term IRI and label as literal with language tag
		- ?termSeparator - specifies the an IRI path component used by termit to separate terms in the vocabulary IRI. Set according to the cofiguration target TermIt installation. Default value is "pojem/".
		
	3. run build.bat
		- input a file - a csv with semi-column ";" separator. The table should have the two columns "termId" containing the id of the term and "label". If a vocabulary does not have term identifier, one can use copy the label column and use it as term identifier.
		- requires tarql [1] and [2] jena command line utilities to be on the path 
		- if necessary change command options and names of input and output files
	3a The output file should be URL decoded because the generated *.ttl file contains IRIs with diacritics tarql will serialize those characters as url encoded. 
	
	4. Upload the vocabulary to termit.
		- login into TermIt installation to which the vocabulary should be uploaded.
		- copy the value of authorization header and set it in some http client, e.g. curl or 
		- create a post request at 
			- path - rest/vocabularies/import 
			- header administration: Bearer qwerq...
			- header Content-Type: mutipart/form-data 
			- body - set body content type to mutipart/form-data 
			- body - add text part with 
				content-type=text/turtle
				field name=file 
				value=copy and paste the content of the output file from step 3 (3a)
				
References:
[1] - Tarql: SPARQL for Tables - http://github.com/tarql/tarql
[2] - Apache Jena - http://jena.apache.org/
 
