# Local CI Verification Script
$ErrorActionPreference = "Stop"

function Write-Section {
    param([string]$title)
    Write-Host "`n======================================================================" -ForegroundColor Cyan
    Write-Host "  $title" -ForegroundColor Cyan
    Write-Host "======================================================================" -ForegroundColor Cyan
}

$rootDir = Split-Path -Parent $PSScriptRoot

try {
    # 1. FRONTEND
    Write-Section "1. FRONTEND: LINTING, AUDIT, TESTS AND BUILD"
    Push-Location "$rootDir\frontend"
    try {
        Write-Host ">> Checking Frontend Lint (oxlint)..." -ForegroundColor Yellow
        npm.cmd run lint
        Write-Host "[OK] Frontend Lint passed." -ForegroundColor Green

        Write-Host ">> Checking Dependencies Audit (npm audit)..." -ForegroundColor Yellow
        npm.cmd audit --audit-level=high
        Write-Host "[OK] Frontend Audit passed." -ForegroundColor Green

        Write-Host ">> Running Frontend Unit Tests..." -ForegroundColor Yellow
        npm.cmd test
        Write-Host "[OK] Frontend Unit Tests passed." -ForegroundColor Green

        Write-Host ">> Building Production Frontend Bundle..." -ForegroundColor Yellow
        npm.cmd run build
        Write-Host "[OK] Frontend Production Build passed." -ForegroundColor Green
    } finally {
        Pop-Location
    }

    # 2. BACKEND
    Write-Section "2. BACKEND: CHECKSTYLE, TESTS AND BUILD"
    Push-Location "$rootDir\backend"
    try {
        Write-Host ">> Running Checkstyle Static Analysis..." -ForegroundColor Yellow
        cmd.exe /c "mvnw.cmd checkstyle:check"
        if ($LASTEXITCODE -ne 0) { throw "Backend Checkstyle failed!" }
        Write-Host "[OK] Backend Checkstyle passed." -ForegroundColor Green

        Write-Host ">> Running Backend Unit Tests..." -ForegroundColor Yellow
        cmd.exe /c "mvnw.cmd test"
        if ($LASTEXITCODE -ne 0) { throw "Backend Unit Tests failed!" }
        Write-Host "[OK] Backend Unit Tests passed." -ForegroundColor Green

        Write-Host ">> Packaging Backend Production Jar..." -ForegroundColor Yellow
        cmd.exe /c "mvnw.cmd package -DskipTests"
        if ($LASTEXITCODE -ne 0) { throw "Backend Package Build failed!" }
        Write-Host "[OK] Backend Package passed." -ForegroundColor Green
    } finally {
        Pop-Location
    }

    Write-Section "ALL CI QUALITY GATES PASSED (100% GREEN)"
    Write-Host "All checks succeeded with strict error enforcement!" -ForegroundColor Green

} catch {
    Write-Host "`n[FAIL] CI Quality Gate failed: $_" -ForegroundColor Red
    exit 1
}
