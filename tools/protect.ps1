# Release protection post-processing for Torov Visual.
#
# Computes a SHA-256 over the mod's own class entries (powder/**.class) inside
# the built jar and writes it into the jar's `protection.key` entry. The runtime
# Protection class recomputes the same hash and refuses to start if it differs,
# i.e. if the jar was tampered with after release.
#
# Usage: powershell -ExecutionPolicy Bypass -File tools\protect.ps1 -Jar "build\libs\torov-visual-1.0-1.21.4.jar"

param(
    [Parameter(Mandatory = $true)][string]$Jar
)

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

if (-not (Test-Path $Jar)) { throw "Jar not found: $Jar" }
$full = (Resolve-Path $Jar).Path

# --- pass 1: collect class entry bytes (ordinal sorted, matching Java Collections.sort) ---
$zip = [System.IO.Compression.ZipFile]::OpenRead($full)
try {
    $names = @()
    foreach ($e in $zip.Entries) {
        if ($e.FullName.StartsWith("powder/") -and $e.FullName.EndsWith(".class")) {
            $names += $e.FullName
        }
    }
    $names = $names | Sort-Object -Property @{ Expression = { $_ } } -Culture ([System.Globalization.CultureInfo]::InvariantCulture)
    # ordinal sort
    $names = [System.Collections.Generic.List[string]]$names
    $names.Sort([System.StringComparer]::Ordinal)

    $sha = [System.Security.Cryptography.SHA256]::Create()
    $ms = New-Object System.IO.MemoryStream
    foreach ($n in $names) {
        $entry = $zip.GetEntry($n)
        $s = $entry.Open()
        $s.CopyTo($ms)
        $s.Close()
    }
    $ms.Position = 0
    $hashBytes = $sha.ComputeHash($ms)
    $ms.Close()
    $hash = ([System.BitConverter]::ToString($hashBytes)).Replace("-", "").ToLowerInvariant()
}
finally {
    $zip.Dispose()
}

Write-Output "classes hashed: $($names.Count)"
Write-Output "integrity hash: $hash"

# --- pass 2: write hash into protection.key entry (in place) ---
$mode = [System.IO.Compression.ZipArchiveMode]::Update
$fs = [System.IO.File]::Open($full, [System.IO.FileMode]::Open)
try {
    $archive = New-Object System.IO.Compression.ZipArchive($fs, $mode)
    try {
        $existing = $archive.GetEntry("protection.key")
        if ($existing) { $existing.Delete() }
        $entry = $archive.CreateEntry("protection.key")
        $writer = New-Object System.IO.StreamWriter($entry.Open())
        $writer.Write($hash)
        $writer.Flush()
        $writer.Close()
    }
    finally {
        $archive.Dispose()
    }
}
finally {
    $fs.Close()
}

Write-Output "protection.key written into: $full"
