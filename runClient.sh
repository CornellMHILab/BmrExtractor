#!/bin/bash

java -Xmx3072m -Xms1024m -Dlog4j.configuration=file:config/log4j.properties -cp "lib/*:target/*" gov.va.vinci.ef.Client -clientConfigFile "config/ClientConfig.groovy" -readerConfigFile "config/readers/SQLDatabaseCollectionReaderConfig.groovy" -listenerConfigFile "config/listeners/SQLDatabaseListenerConfig.groovy"