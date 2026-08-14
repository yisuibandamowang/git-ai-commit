@echo off
set DIR=%~dp0
if "%JAVA_HOME%"=="" set JAVA_HOME=C:\Program Files\Java\jdk-17
"%JAVA_HOME%\bin\java" -Dorg.gradle.appname=gradlew -classpath "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
