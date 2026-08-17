$ErrorActionPreference = 'Stop'

$root = Resolve-Path (Join-Path $PSScriptRoot '..')
$jdkHome = 'C:\Program Files\Java\jdk-17.0.18'
$tomcatHome = 'C:\Program Files\Apache Software Foundation\Tomcat 10.1'
$mysqlJar = Join-Path $root 'web\WEB-INF\lib\mysql-connector-j-26.7.0.jar'
$classesDir = Join-Path $root 'web\WEB-INF\classes'
$sourceRoot = Join-Path $root 'src\java'

if (-not (Test-Path $mysqlJar)) {
    throw "Missing MySQL driver: $mysqlJar"
}

New-Item -ItemType Directory -Force -Path $classesDir | Out-Null

$tomcatJarNames = @(
    'annotations-api.jar',
    'catalina-ant.jar',
    'catalina-ha.jar',
    'catalina-ssi.jar',
    'catalina-storeconfig.jar',
    'catalina-tribes.jar',
    'catalina.jar',
    'ecj-4.27.jar',
    'el-api.jar',
    'jakartaee-migration-1.0.9-shaded.jar',
    'jasper-el.jar',
    'jasper.jar',
    'jaspic-api.jar',
    'jsp-api.jar',
    'servlet-api.jar',
    'tomcat-api.jar',
    'tomcat-coyote-ffm.jar',
    'tomcat-coyote.jar',
    'tomcat-dbcp.jar',
    'tomcat-i18n-cs.jar',
    'tomcat-i18n-de.jar',
    'tomcat-i18n-es.jar',
    'tomcat-i18n-fr.jar',
    'tomcat-i18n-ja.jar',
    'tomcat-i18n-ko.jar',
    'tomcat-i18n-pt-BR.jar',
    'tomcat-i18n-ru.jar',
    'tomcat-i18n-zh-CN.jar',
    'tomcat-jdbc.jar',
    'tomcat-jni.jar',
    'tomcat-util-scan.jar',
    'tomcat-util.jar',
    'tomcat-websocket.jar',
    'websocket-api.jar',
    'websocket-client-api.jar'
)

$tomcatJars = $tomcatJarNames | ForEach-Object { Join-Path $tomcatHome "lib\$_" }
$tomcatClasspath = ($tomcatJars + $mysqlJar) -join ';'
$sources = Get-ChildItem -Path $sourceRoot -Recurse -Filter *.java | ForEach-Object { $_.FullName }

& (Join-Path $jdkHome 'bin\javac.exe') `
    -encoding UTF-8 `
    -cp $tomcatClasspath `
    -d $classesDir `
    @sources

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
