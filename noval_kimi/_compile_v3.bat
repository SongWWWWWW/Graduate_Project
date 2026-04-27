@echo off
set JAVA_HOME=C:\Users\wcc\.jdks\ms-17.0.16
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d C:\Users\wcc\Project\noval_kimi
mvnw.cmd clean compile -DskipTests > C:\Users\wcc\Project\noval_kimi\compile_v3.log 2>&1
