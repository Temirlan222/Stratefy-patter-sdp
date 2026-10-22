param([string]$DatabasePath = "data/concert.db")
$ErrorActionPreference = "Stop"
Push-Location $PSScriptRoot
try {
    & mvn -q -DskipTests package
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    $javaCommand = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { 'java' }
    & $javaCommand --enable-native-access=ALL-UNNAMED -jar target/ticket-concert.jar $DatabasePath
    exit $LASTEXITCODE
} finally {
    Pop-Location
}