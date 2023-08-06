
/tmp/tomcat-docbase is always created with a Spring Boot JAR (but not a WAR)

Solution: 
create a folder name as public under your project, in the same folder with your JAR.

Reason: 
from the springboot code, there is no configuration for docbase folder. 
but you can create a common root folder in you project folder name as public, static or src/main/webapp, 
then the springboot will never create temp tomcat-docbase folder for you again.


https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/web/servlet/server/DocumentRoot.java
https://stackoverflow.com/questions/43089611/tmp-tomcat-docbase-is-always-created-with-a-spring-boot-jar-but-not-a-war