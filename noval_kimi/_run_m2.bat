@echo off
set JAVA_HOME=C:\Users\wcc\.jdks\ms-17.0.16
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d C:\Users\wcc\Project\noval_kimi
mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=dev" > C:\Users\wcc\Project\noval_kimi\run_m2.log 2>&1
