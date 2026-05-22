@echo off
setlocal

set "BASEDIR=%~dp0"
set "MVN=%BASEDIR%.maven\apache-maven-3.9.9\bin\mvn.cmd"
set "JDK_HOME=%BASEDIR%.jdk\jdk-21.0.11+10"

if exist "%JDK_HOME%\bin\java.exe" (
  set "JAVA_HOME=%JDK_HOME%"
  set "PATH=%JAVA_HOME%\bin;%PATH%"
)

if not exist "%MVN%" (
  echo Bundled Maven not found at: %MVN%
  echo Extract .maven\maven.zip or install Maven globally.
  exit /b 1
)

call "%MVN%" %*
exit /b %ERRORLEVEL%