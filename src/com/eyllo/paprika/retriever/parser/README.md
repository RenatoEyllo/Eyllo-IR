ENTITY PARSER
=============

How parser works
----------------
* Any specific parser can be instantiated and encapsulated as an AbstractParser.
* Properties should be set using their constructors.
* The fetching process consists of three phases:
    A. The method fetchEntities is called. This will go for all the number of pages set.
    B. For each fetched page the parseSearchResults method will be called, entities detected and individual sites stored.
    C. After all the pages have been parsed, the completeEntityInfo method will be called and all sites stored for each entity fetched and parsed to complete their data.
* If a local storage system is used, then any of the phases could be started/executed separately.

Implementing a new parser
-------------------------
* The new parser should extend AbstractParser.
* The process it should follow is:
  1. FetchEntities is the method that starts the whole process.
  2. Each fetched site should be parsed into an entity within the parseSearchResults.
  3. All entity information should be completed using the completeEntityInfo method.