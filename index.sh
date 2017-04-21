mvn clean && mvn install
mvn exec:java -Dexec.mainClass="de.citec.sc.index.IndexCreator" -Dexec.args="../Wikipedia/"
