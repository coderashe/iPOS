@echo off
set ASPNETCORE_ENVIRONMENT=Development
set ASPNETCORE_URLS=http://0.0.0.0:5000
echo Building...
dotnet build iPosApi.csproj -c Debug -v q
echo Starting iPOS API on http://0.0.0.0:5000 ...
dotnet bin\Debug\net8.0\iPosApi.dll
pause
