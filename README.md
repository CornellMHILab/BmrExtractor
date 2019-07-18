## BmrExtractor - Natural language processing (NLP) system for extracting measurements from bone marrow reports

BmrExtractor Pipeline is designed to find blast count, cellularity and fibrosis measurements in bone marrow reports.   

BmrExtractor is based on Leo architecture extending UIMA AS.  For more info on Leo see [ http://department-of-veterans-affairs.github.io/Leo/userguide.html ]. Specifically, BmrExtractor was modeled after a specific instance of Leo, named EFEx, that was developed by the VA researchers to extract left ventricular ejection fraction measurements from Echocardiograms. 

Extraction strategy implemented in BmrExtractor is specific to WCM bone marrow reports and its document format, and may not work well for bone marrow reports generated in clinical settings elsewhere. However, with familiarity in Leo architecture one can modify the pipeline implementation with minimal changes to suit individual needs.

To use BmrExtractor :

  1.  Follow the instructions to install and configure UIMA AS as described on the Nexus page (https://nexus.weill.cornell.edu/display/ARCH/Installation+and+configuration).  
  
  2. Checkout and install Leo BmrExtractor code base (https://svn.med.cornell.edu/dev/researchinformatics/trunk/nlp/uima/uima-as/leo/BmrExtractor/) in a directory of choice.
  
  3. Start UIMA AS Broker.
     
  4. Configure BmrExtractor reader and listeners.
    
    4.1 Three readers are available:
     
       4.1.1 FileCollectionReaderConfig.groovy  -- Enter the path to input directory to read simple text files. The files need to have .txt extention. 
      
       4.1.2 BatchDatabaseCollectionReader.groovy -- Enter the database engine, database name, and input query. Update the batch parameters. If you have only one batch, change the ending index to be less than the batch size. If you are using this reader for batch reads, add sequential numbering column called "RowNo" to your input table. The tags {min} and {max} will be automatically replaced with starting and ending RowNo for each batch until edning RowNo reaches the last endingIndex.
       
       4.1.3 SQLServerPagedDatabaseCollectionReaderConfig.groovy - Enter the database engine, database name, and input query. Make sure the input query ends with "order by" clause. The query will be automatically transformed for SQL Server fetching new batch with offset row number. This approach becomes very slow when the number of records reaches over 2.5M records. MS SQL Server queries become very slow at that point.
      
    4.2 Four listeners
    
       4.2.1 SimpleCsvListenerConfig.groovy - Enter the path to the output directory. A new file will be created with a standard output.
      
       4.2.2 SimpleXmiListenerConfig.groocy - Enter the path to the output directory. A new directory with xmi files will be created.
      
       4.2.3 CsvListenerConfig.groovy - this is an example of a custom CSV listener
      
       4.2.4 DatabaseListenerConfig.groovy - this is an example of a custom database listener.
           
  5. Use runService.sh or runService.bat script to start the service.
  
  6. Modify runClient.sh or runClient.bat script with  the selected readers and listeners and start the client.
  
  
      
  
