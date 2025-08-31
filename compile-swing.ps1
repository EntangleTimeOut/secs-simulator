param(
  [int]$JavaRelease = 17
)

$ErrorActionPreference = 'Stop'

# Paths
$pathSrc = Join-Path $PSScriptRoot 'src\main\java\com'
$pathBin = Join-Path $PSScriptRoot 'bin'
$exportJar = Join-Path $PSScriptRoot 'SwingSecsSimulator.jar'
$mainClass = 'com.shimizukenta.secssimulator.swing.SwingSecsSimulator'

# Clean bin
if (Test-Path $pathBin) {
  Remove-Item -Recurse -Force $pathBin
}
New-Item -ItemType Directory -Path $pathBin | Out-Null

# Build sources list (exclude module-info.java)
$sourcesFile = Join-Path $PSScriptRoot 'sources.txt'
Get-ChildItem -Recurse -Filter *.java $pathSrc |
  ForEach-Object { $_.FullName } |
  Set-Content -Encoding ascii $sourcesFile

# Compile (use cmd to pass @argfile reliably on Windows PowerShell)
$javacCmd = "javac -d `"$pathBin`" -encoding UTF-8 --release $JavaRelease @" + $sourcesFile
cmd /c $javacCmd 2>&1 | Tee-Object -Variable compileOut
$exit = $LASTEXITCODE
if ($exit -ne 0) {
  Write-Error "javac failed with exit code $exit`n$compileOut"
  exit $exit
}

# Package jar
if (Test-Path $exportJar) { Remove-Item -Force $exportJar }
& jar -c -f $exportJar -e $mainClass -C $pathBin .
$exit = $LASTEXITCODE
if ($exit -ne 0) {
  Write-Error "jar failed with exit code $exit"
  exit $exit
}

Write-Host "Built: $exportJar" -ForegroundColor Green
