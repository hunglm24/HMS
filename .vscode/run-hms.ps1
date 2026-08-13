$ErrorActionPreference = 'Stop'

$root = Resolve-Path (Join-Path $PSScriptRoot '..')
$tomcatHome = 'C:\Program Files\Apache Software Foundation\Tomcat 10.1'
$javaHome = 'C:\Program Files\Java\jdk-17.0.18'
$webRoot = Join-Path $root 'web'
$deployRoot = Join-Path $tomcatHome 'webapps\HMS'

& (Join-Path $PSScriptRoot 'build-hms.ps1')

if (Test-Path $deployRoot) {
    Remove-Item -LiteralPath $deployRoot -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $deployRoot | Out-Null
Copy-Item -Path (Join-Path $webRoot '*') -Destination $deployRoot -Recurse -Force

$env:JAVA_HOME = $javaHome
$env:JRE_HOME = $javaHome
$env:CATALINA_HOME = $tomcatHome
$env:CATALINA_BASE = $tomcatHome

$listener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if (-not $listener) {
    Start-Process -FilePath (Join-Path $tomcatHome 'bin\startup.bat') -WindowStyle Hidden
    Start-Sleep -Seconds 8
}

Write-Host "HMS is available at http://localhost:8080/HMS/"
