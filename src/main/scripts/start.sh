#!/bin/sh

java -Xms32m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=heapdump.hprof -Djava.util.logging.config.file=conf/logging.properties -jar knowledgebase-@project.version@.jar