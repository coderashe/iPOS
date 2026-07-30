@echo off
set ASPNETCORE_ENVIRONMENT=Development
echo Starting iPOS API on http://localhost:5000 ...
dotnet bin\Debug\net8.0\iPosApi.dll
pause
