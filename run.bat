@echo off
title Student Attendance Management System
echo ========================================================
echo   Student Attendance Management System - Launching
echo ========================================================
echo.

set "MAVEN_CMD=C:\Users\singh\.maven\apache-maven-3.9.9\bin\mvn.cmd"
if not exist "%MAVEN_CMD%" (
    set "MAVEN_CMD=mvn"
)

echo Starting Spring Boot backend...
echo Web UI will be accessible at: http://localhost:8080
echo.
"%MAVEN_CMD%" spring-boot:run
pause
