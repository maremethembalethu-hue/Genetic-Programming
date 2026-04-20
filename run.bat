@echo off

echo Compiling Java files...
javac EnergyPredictGP.java EnergyPredictSBGP.java Node.java NodePt.java

echo.
echo Select mode:
echo 1 - Standard GP
echo 2 - Structural-Based GP (SBGP)
set /p choice=Enter choice (1 or 2): 

if "%choice%"=="1" goto gp
if "%choice%"=="2" goto sbgp
goto invalid

:gp
echo Running Standard GP (EnergyPredictGP)...
java EnergyPredictGP
goto end

:sbgp
echo Running Structural-Based GP (EnergyPredictSBGP)...
java EnergyPredictSBGP
goto end

:invalid
echo Invalid choice.

:end