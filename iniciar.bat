@echo off
rem Doble clic: compila y abre el emulador simple.
chcp 65001 >nul
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\*.java || (pause & exit /b 1)
java -cp out Main
pause
