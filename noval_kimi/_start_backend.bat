@echo off
chcp 65001 >nul
set JAVA_HOME=C:\Users\wcc\.jdks\ms-17.0.16
set PATH=%JAVA_HOME%\bin;%PATH%

echo JAVA_HOME=%JAVA_HOME%
echo Starting backend...

cd /d C:\Users\wcc\Project\noval_kimi
mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=dev"
