# Engine - Part A

INTRODUCTION
------------
This is part A of our Search Engine Project.
It includes the following process:
- Reading files from a given corpus, segmenting them into documents
Parsing the corpus in batches of 6000 documents, one by one. A hashmap of <String, Term> pairs is
  created in this phase.
  The parsing could be executed with or without stemming
- Indexing the terms of each batch: creating 800 posting files and writing information about the
terms into them. In addition, we create a file containing information about all the parsed documents and a united dictionary for the entire corpus.


INFO 
----
Project name : EngineA
java version 1.8.0

OPERATIONS 
---------
1. Run the project jar file 
2. Select a corpus path in the first text area by pressing browse
3. Select a posting files path 
4. Click start to run processing the corpus 
5. Click Ok on the alert that pops up

POST PROCESSING 
----------------
1. Reset button : clicking this button will delete all content in the selected posting files path
2. Load dictionary : will load the term dictionary to memory
3. Show dictionary : shows all the unique terms in the corpus with this total tf