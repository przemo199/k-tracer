@echo off
rem JMH benchmarks pinned to two physical cores (logical CPUs 4 and 6) at high priority: much less noise on Windows.
rem Not one core: a JVM limited to one CPU switches to the serial GC and shares that core with the JIT compiler.
rem Usage: scripts\bench.cmd [JMH filters/options] [--save-baseline NAME] [--baseline NAME]
rem   e.g. scripts\bench.cmd --save-baseline before       then   scripts\bench.cmd Intersect --baseline before
pushd "%~dp0.."
start "" /B /WAIT /HIGH /AFFINITY 0x50 kotlin.bat run -m benchmarks -- %*
set BENCH_EXIT=%ERRORLEVEL%
popd
exit /b %BENCH_EXIT%
