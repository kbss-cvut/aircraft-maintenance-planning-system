@echo off
REM This script finds the target folder containing the run.bat script. The script searches for the run.bat using in the
REM current and parent folders. The script also supports external folders, i.e. folders which are located outside of the
REM project root. This is enabled by a config.properties file which sets the property
REM scripts.root.dir=...
REM to point to the root of a project.
REM NOTES
REM This script requires java 11 or higher because it runs the non compiled java file.
set java=c:\apps\java\jdk-15\bin\java
for /f "delims=" %%a in ('%java% %~dp0\java\scripts.java') do @set tmp_scripts=%%a
if NOT "%tmp_scripts%"=="%tmp_scripts:__EMPTY__=_%" (GOTO NOT_SCRIPT_DIR) 

set tmp_scripts=%tmp_scripts:~0,-1%
if NOT "%tmp_scripts%"=="%tmp_scripts:__NOOP__=_%" (GOTO ALREADY_SETUP) 

SET "PATH=%tmp_scripts%;%PATH%"
echo you can run scripts from "%tmp_scripts%"
GOTO FINISH

:ALREADY_SETUP
set tmp_scripts=%tmp_scripts:__NOOP__=%
echo no op. already setup. you can run scripts from "%tmp_scripts%"
GOTO FINISH

:NOT_SCRIPT_DIR
echo script dir not found

:FINISH
REM echo FINISH
REM reset the variable
set tmp_scripts=
@echo on