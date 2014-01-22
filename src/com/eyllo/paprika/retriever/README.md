ENTITY RETRIEVER
================

How retriever works
-------------------
* The entity retriever is the one in charge of running the parser as many times as required (even indefinitely).
* It can also run the geo-validation process for entity's addresses.
* The following properties should be filled or left blank to use default values within each parser:
# Parser name.
retriever.parser.name=sptrans
# Maximum number of pages to be visited.
retriever.parser.maxpagenum=10000
# Maximum number of entities to be retrieved.
retriever.parser.maxnument=10000
# Path where output file will be writen.
retriever.parser.outpath=
# Url to be visited to get entities.
retriever.parser.fetchurl=http://api.olhovivo.sptrans.com.br/v0
# If local storage will be used.
retriever.parser.localsearch=true
# Request politeness between server requests (mili seconds).
retriever.parser.reqpoliteness=5000
# Number of runs Entity retriever will perform.
retriever.runs.number=1000
# Time between retriever runs (mili seconds).
retriever.runs.interleave=900
# Helps us decide where to store retrieved entities.
retriever.backend.entities=file
