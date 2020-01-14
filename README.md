# Engine

# PartA

INTRODUCTION
------------
This is part A of our SearchEngine Project.
It includes the following process:
- Reading files from a given corpus, segmenting them into documents
- Parsing the corpus in batches of 6000 documents, one by one. A hashmap of <String, Term> pairs is created in this phase.
The parsing could be executed with or without stemming
- Indexing the terms of each batch: creating 800 posting files and writing intormation about the terms into them.

INFO 
----
project name : EngineA 
java version 1.8.0

OPERATIONS 
---------
1. Run the project jar file 
2. Select a corpus path in the first text area by pressing browse
3. Select a posting files path 
4. Click start to run processing the corpus 

POST PROCESSING 
----------------
1. Reset button : clicking this button will delete all content in the selected posting files path
2. Load dictionary : will load the term dictionary to memory
3. Show dictionary : shows all the unique terms in the corpus with this total tf


# Part B

INTRODUCTION
------------
This is part B of our Search Engine Project.
It includes the following process:
- Loading the posting files and dictionaries generated in part A
The dictionaries are restored and loaded into memory.
- After entering a query: 
1. The query is parsed.
2. Its term objects are reconstructed from the dictionary 
3. If semantics is enabled the query terms are sent to the semantics model.
4. The query terms and semantics terms (if enabled) are sent to the ranker who finds the 50 most relevant documents.
 
INFO 
----
Project name : EngineA
java version 1.8.0

OPERATIONS 
---------
1. Run the project bat file 
2. Select an index path in the second text area by pressing browse. This path should include the following:
-Index folder including all posting files, dictionary file, docs folder including the docs file, stop_words file.
3. If stemming option was enabled in part A please check the "Enable stemming" checkbox.
4. Click on Load dictionary and wait until the Show dictionary open in enabled.
5. Select a query file in the Query File text area by pressing browse OR type a query .
6. If you want to expand the query using semantics model check the Semantics model checkbox or Semantics model API checkbox. 
7. Select Run query and wait until the Query is done alert pops.

POST PROCESSING 
----------------
1. Reset button : clicking this button will delete all content in the selected posting files path
2. Select Show Results if you want to show the query results. 
You can view each document's top 5 entities by by marking a specific row.
3. Select Save Results to save the query results into a txt file.
