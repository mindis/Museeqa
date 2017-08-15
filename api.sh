while true
do
        echo "Running QA Server"
        nohup java -Xmx10g -classpath src/main/resources/log4j.xml -jar target/Museeqa-1.0-jar-with-dependencies.jar -d1 qald6Train -d2 qald6Test -m1 true -m2 true -w1 30 -e 10 -s 15 -k1 10 -k2 10 -l1 10 -l2 10 -w2 30 -i memory -l $l EN 1,2,3,4 -b true -q true -n local -api true> server.log
        
       
done
