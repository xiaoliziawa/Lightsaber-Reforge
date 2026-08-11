param(
    [string]$GradleHome = 'D:\GradleHome',
    [string]$JavaHome = 'D:\GradleHome\jdks\eclipse_adoptium-25-amd64-windows.2'
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$properties = @{}
Get-Content -LiteralPath (Join-Path $projectRoot 'gradle.properties') | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*)=(.*)$') {
        $properties[$matches[1].Trim()] = $matches[2].Trim()
    }
}

$neoVersion = $properties['neo_version']
$minecraftJar = Join-Path $projectRoot "build\moddev\artifacts\minecraft-patched-$neoVersion.jar"
$javac = Join-Path $JavaHome 'bin\javac.exe'
$cacheRoot = Join-Path $GradleHome 'caches\modules-2\files-2.1'

if (-not (Test-Path -LiteralPath $javac)) {
    throw "Java 25 javac was not found at $javac"
}
if (-not (Test-Path -LiteralPath $minecraftJar)) {
    throw "Patched Minecraft jar was not found at $minecraftJar"
}
if (-not (Test-Path -LiteralPath $cacheRoot)) {
    throw "Gradle dependency cache was not found at $cacheRoot"
}

$neoForgeJar = Get-ChildItem -Path (Join-Path $cacheRoot "net.neoforged\neoforge\$neoVersion") -Recurse -File -Filter "neoforge-$neoVersion-universal.jar" |
    Select-Object -First 1 -ExpandProperty FullName
if (-not $neoForgeJar) {
    throw "NeoForge $neoVersion universal jar was not found under $cacheRoot"
}

$allDependencyJars = Get-ChildItem -Path $cacheRoot -Recurse -File -Filter '*.jar' |
    Where-Object {
        $jarPath = $_.FullName
        if ($_.Name -match '(-sources|-javadoc|-installer|-userdev)\.jar$' -or
            $jarPath -match '\\natives-[^\\]+\.jar$') {
            return $false
        }

        try {
            $archive = [IO.Compression.ZipFile]::OpenRead($jarPath)
            $archive.Dispose()
            return $true
        }
        catch {
            Write-Warning "Skipping invalid dependency jar: $jarPath"
            return $false
        }
    }

$preferredPatterns = @(
    "\\net\.neoforged\\neoforge\\$([regex]::Escape($neoVersion))\\",
    "\\mezz\.jei\\.*\\$([regex]::Escape($properties['jei_version']))\\",
    "\\curse\.maven\\dynamic-lights-227874\\$([regex]::Escape($properties['dynamic_lights_file_id']))\\",
    "\\curse\.maven\\kubejs-238086\\$([regex]::Escape($properties['kubejs_file_id']))\\",
    "\\curse\.maven\\rhino-416294\\$([regex]::Escape($properties['rhino_file_id']))\\",
    "\\curse\.maven\\lootr-361276\\$([regex]::Escape($properties['lootr_file_id']))\\"
)

$preferredJars = $allDependencyJars | Where-Object {
    $path = $_.FullName
    $preferredPatterns.Where({ $path -match $_ }, 'First').Count -gt 0
} | Sort-Object FullName

$excludedFallbackPatterns = @(
    '\\net\.neoforged\\neoforge\\',
    '\\mezz\.jei\\',
    '\\curse\.maven\\dynamic-lights-227874\\',
    '\\curse\.maven\\kubejs-238086\\',
    '\\curse\.maven\\rhino-416294\\',
    '\\curse\.maven\\lootr-361276\\'
)

$fallbackJars = $allDependencyJars | Where-Object {
    $path = $_.FullName
    $preferredPatterns.Where({ $path -match $_ }, 'First').Count -eq 0 -and
    $excludedFallbackPatterns.Where({ $path -match $_ }, 'First').Count -eq 0
} | Sort-Object -Property @{ Expression = 'LastWriteTime'; Descending = $true }, FullName

$classPath = @($minecraftJar, $neoForgeJar) + @($preferredJars.FullName) + @($fallbackJars.FullName)
$classPath = $classPath | Select-Object -Unique

$sourceRoot = Join-Path $projectRoot 'src\main\java'
$sources = Get-ChildItem -Path $sourceRoot -Recurse -File -Filter '*.java' | Where-Object {
    $relativePath = [IO.Path]::GetRelativePath($sourceRoot, $_.FullName).Replace('\', '/')
    $relativePath -notlike 'com/fiskmods/lightsabers/common/integration/epicfight/*' -and
    $relativePath -notlike 'com/fiskmods/lightsabers/client/integration/epicfight/*' -and
    $relativePath -ne 'com/fiskmods/lightsabers/mixin/MixinEpicFightLivingEntityPatch.java'
} | Sort-Object FullName

$fastBuildRoot = Join-Path $projectRoot 'build\fast-javac'
$classesDir = Join-Path $fastBuildRoot 'classes'
$resolvedBuildRoot = [IO.Path]::GetFullPath((Join-Path $projectRoot 'build'))
$resolvedClassesDir = [IO.Path]::GetFullPath($classesDir)
if (-not $resolvedClassesDir.StartsWith($resolvedBuildRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
    throw "Refusing to clean unexpected output directory: $resolvedClassesDir"
}
if (Test-Path -LiteralPath $classesDir) {
    Remove-Item -LiteralPath $classesDir -Recurse -Force
}
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null

function Quote-JavacArgument([string]$value) {
    return '"' + $value.Replace('\', '\\').Replace('"', '\"') + '"'
}

$arguments = @(
    '--release'
    '25'
    '-encoding'
    'UTF-8'
    '-proc:none'
    '-Xlint:none'
    '-Xdiags:verbose'
    '-Xmaxerrs'
    '20000'
    '-classpath'
    (Quote-JavacArgument ($classPath -join [IO.Path]::PathSeparator))
    '-d'
    (Quote-JavacArgument $classesDir)
) + @($sources.FullName | ForEach-Object { Quote-JavacArgument $_ })

$argumentFile = Join-Path $fastBuildRoot 'javac.args'
[IO.File]::WriteAllLines($argumentFile, $arguments, [Text.UTF8Encoding]::new($false))

Write-Host "Compiling $($sources.Count) sources with Java 25 and $($classPath.Count) dependency jars..."
& $javac '-J-Duser.language=en' '-J-Duser.country=US' "@$argumentFile"
exit $LASTEXITCODE
