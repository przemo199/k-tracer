# AGENTS.md

Guidance for AI coding agents (Claude Code, Codex, Copilot, Cursor, …) working in this repository.
Humans are welcome too. `CLAUDE.md` simply imports this file.

## What this is

`k-tracer` is a CPU ray tracer written in Kotlin, following *The Ray Tracer Challenge* (Jamis Buck).
It is a CLI: it reads a YAML scene and writes a PNG.

```
ktracer [-r SERIAL|PARALLEL] <scene.yaml> <output.png>
```

Built with **Kotlin Toolchain** (JetBrains' `kotlin` CLI, formerly called *Amper*), **not Gradle**. Two modules: the app `k-tracer` (the project root) and `benchmarks/` (JMH microbenchmarks, see [Performance](#performance)). No runtime services, no network access at runtime. About 5k lines of code, with roughly 290 unit tests.

## Commands

Always use the project wrapper. In bash, Git Bash or CI use `./kotlin`; on Windows PowerShell or cmd use `.\kotlin.bat`. The wrapper pins the toolchain version (0.13.0) and downloads it, a JRE and the build JDK on first use.

| Task | Command |
| --- | --- |
| Run all tests | `./kotlin test` |
| Run one test class (wildcards allowed) | `./kotlin test -m k-tracer --include-classes 'org.example.ktracer.shapes.SphereTest'` (test filters need `-m`) |
| Run one test method | `./kotlin test -m k-tracer --include-test 'org.example.ktracer.shapes.SphereTest.sphere normal'` |
| Compile (main and test) | `./kotlin build` |
| Render a scene | `./kotlin run -m k-tracer -- scenes/cover.yaml out/cover.png` (`-m` is required: there are two application modules) |
| Benchmarks (~2.5 min) | `cmd //c "scripts\bench.cmd"`; see [Performance](#performance) |
| Executable JAR | `./kotlin package`, which writes `build/tasks/_k-tracer_executableJarJvm/k-tracer-jvm-executable.jar` |
| Effective configuration | `./kotlin show settings` / `./kotlin show dependencies` |
| Clean | `./kotlin clean` |

- The build is configured entirely in YAML. There are no `build.gradle.kts`, `settings.gradle.kts` or `gradlew` files. Don't add them back.
  - `project.yaml` lists the modules besides the root one.
  - `common.module-template.yaml` holds the Kotlin version, compiler flags and JDK, applied by every module. Change them there, never per module.
  - `module.yaml` is the app (dependencies, main class); `benchmarks/module.yaml` is the benchmarks module.
- The `module.yaml` schema is documented at <https://kotlin-toolchain.org/latest/reference/module/>. It is new; don't guess keys from old Amper or Gradle knowledge. Check them with `./kotlin show settings`, which prints every setting with its source.
- JUnit XML results (with failure messages) are written to `build/reports/k-tracer/jvm/`.
- A warm `./kotlin test` takes about 3 s, and a clean one about 15 s.
- Behind a TLS-intercepting proxy or antivirus, the JVM may fail downloads with `PKIX path building failed`. Set `JAVA_TOOL_OPTIONS=-Djavax.net.ssl.trustStoreType=Windows-ROOT` on Windows so it uses the OS certificate store.

### Rendering for verification

Every scene in `scenes/` is configured for 3840 px output, which is slow. To check a change visually:

1. Copy the scene somewhere outside the repo, such as your scratch or temp directory.
2. Lower the camera's `width` and `height` (for example to 400×200).
3. Render the copy to a path outside `rendered_images/`.

Do **not** overwrite `rendered_images/*.png` unless you were asked to regenerate the README images.

## Toolchain: read this before you write Kotlin

This project deliberately runs on bleeding-edge tooling. Some of the syntax postdates many models' training data. **Do not "fix" it back to older forms.**

- **Kotlin `2.5.0-Beta2`** (`settings.kotlin.version`) and **JDK 26** (`settings.jvm.jdk.version`, auto-provisioned). Kotlin Toolchain refuses JDK 27 with Kotlin 2.5.0 (Beta2 and RC support JDK up to 26), so don't raise it until a Kotlin version that supports 27 is pinned.
- Experimental compiler flags in `common.module-template.yaml` (`settings.kotlin.freeCompilerArgs`). Every module needs them, including modules that only *use* these features:
  - `-Xcompanion-blocks-and-extensions`: companions are written as `companion { ... }`, **not** `companion object { ... }`. Every file in the project uses this form. Follow it.
  - `-Xfull-value-classes`: multi-field `value class` declarations, such as `Color` and `Coordinates`, are valid here.
- Do not bump Kotlin, the toolchain (the version pinned in the `kotlin` and `kotlin.bat` wrappers), the JDK or dependency versions, or add or remove compiler flags, unless the task asks for it.
- CI (`.github/workflows/ci.yaml`) runs `./kotlin test` (`kotlin.bat` on Windows) on Windows, Linux and macOS, plus a benchmark smoke run on Linux (`-f 0 -wi 0 -i 1 -r 1ms`: it checks the JMH harness and each benchmark's setup, not timings). Changes that only touch `*.md`, `rendered_images/` or `.claude/` don't trigger CI. When a run fails, the JUnit reports are uploaded as the `test-reports-<OS>` artifact. Validate workflow edits with [actionlint](https://github.com/rhysd/actionlint).

## Architecture

All code is under `src/org/example/ktracer/` (Kotlin Toolchain default layout: `src/`, `test/`, `resources/`, `testResources/` at the project root):

| Package | Role |
| --- | --- |
| `KTracer.kt` | CLI entry point (Clikt). `main` → `SceneLoader` → `Camera.render` → `Canvas.saveAsPng`. |
| `globals.kt` | `EPSILON`, `coarseEquals`, `squared()`, `solveQuadratic`. |
| `primitives/` | `Tuple` → `Point` (w=1) and `Vector` (w=0). Also `Color`, `Matrix` (4×4; `Transformation` is a typealias), `Transformations` (factory for `translation`, `scaling`, `rotationX/Y/Z`, `shearing`, `viewTransform`, …), `Light`, `Coordinates` (u,v). |
| `composites/` | `Ray`, `Intersection`, `Intersections` (a reusable buffer), `ComputedHit` (precomputed shading data), `Material` (Phong), `World` (shading, shadows, reflection, refraction), `Camera` (rays and serial/parallel render), `Canvas` (pixels and PNG output through scrimage). |
| `shapes/` | `Shape` base class plus `Sphere`, `Plane`, `Cube`, `Cylinder`, `Cone`, `Triangle`, `Group`, `ConstructiveSolidGeometry`, `BoundingBox`. |
| `colorproviders/` | `ColorProvider` interface. `patterns/` (solid 3D patterns: stripe, gradient, ring, checker), `textures/` (UV textures), `mappings/` (point → UV: planar, spherical, cylindrical, cube). |
| `scene/` | YAML scene files, in two stages. `SceneFormat.kt` is the typed model of the file (kotlinx.serialization with [kotaml](https://github.com/Heapy/kotaml)); `SceneResolver` resolves named definitions and builds the `World` and `Camera`; `SceneLoader` ties them together and reports problems as `SceneException`. |

### Render pipeline

`Camera.rayForPixel` → `World.colorAt` → `collectIntersections` (each shape: `Ray.intersect` → `Shape.localIntersect`) → `Intersections.hit()` → `Intersection.prepareComputations` → `World.shadeHit` (lighting per light, shadow rays, and recursive `reflectedColor`/`refractedColor`, capped by `World.MAX_REFLECTION_ITERATIONS`).

### Invariants and gotchas

- **Floating-point equality.** Tuples compare through `coarseEquals` with `EPSILON`. In tests, `assertEquals` on `Point`, `Vector` and `Matrix` relies on this tolerant `equals`. Do not compare raw doubles from geometry with `==`.
- **Object space.** A shape implements `localIntersect` and `localNormalAt` in *object space*. `Shape.normalAt` and `Ray.intersect` handle the world ↔ object transforms, including `parent` chains for `Group` and CSG.
- **Matrices are effectively immutable.** `Matrix.inverse()` is cached on the instance. Never mutate a matrix's elements after construction. Build a new transform instead.
- **Transform order.** `A * B` applies `B` first. For example, `translation(...) * scaling(...)` scales, then translates.
- **`Intersections` is a reused scratch buffer.** It is passed down the call stack and is per-thread in parallel rendering (`ThreadLocal`). `World.collectIntersections` clears it. Do not keep references to it across calls, and do not share one between threads.
- **Hot path.** Intersection and shading code runs billions of times per render. Avoid allocations, lambdas that capture state, `Sequence`s and boxing in `localIntersect`, `localNormalAt` and `World` shading unless you have measured the cost (see [Performance](#performance)).
- **Scene format coverage.** Scenes can contain `camera`, `light`, `plane`, `sphere`, `cube`, `cone`, `cylinder` and the patterns `stripes`, `gradient`, `rings`, `checkers`. Groups, triangles, CSG and textures exist in code but are **not** yet in the YAML format.
- **Scene format rules.** A `define` is a material (a mapping), a transform (a list of steps) or a color (three numbers), told apart by its value, not its name. Only materials can `extend`. Transform steps apply in list order, whether inline or named. Unknown keys, undefined names, non-invertible transforms and a missing camera are errors that report `file:line:column`. Keep it that way: never ignore input silently.

## Conventions

- Kotlin official code style (`kotlin.code.style=official`), 4-space indent, no wildcard imports, imports sorted lexicographically.
- Prefer the concise expression-body form for one-line functions (`fun x() = ...`). This matches recent refactors.
- Operator overloads for math (`plus`, `times`, `dot`, `cross`) are idiomatic here.
- Constants live in `companion { }` blocks (for example `Vector.UP`, `Point.ORIGIN`, `Color.WHITE`).

### Tests

- Tests live in `test/...`, mirroring the main package, one `XxxTest` per class.
- Use `kotlin.test` (`@Test`, `assertEquals`, …) on JUnit Platform. Use `junit-jupiter-params` for parameterised tests.
- Name test methods as backtick sentences, usually echoing the book's scenarios: ``fun `ray intersects sphere at two points`()``.
- Every new shape, pattern or math routine needs unit tests. Every bug fix needs a regression test.

### Recipes

**Add a shape:** subclass `Shape` in `shapes/`, then:

1. Implement `localIntersect` (push `Intersection`s into the passed buffer), `localNormalAt`, `boundingBox()`, `equals`, `hashCode` and `toString`.
2. Add `XxxTest` covering hits, misses, tangents, normals and the bounding box.
3. Optionally add it to the scene format: a `ShapeKind` entry in `SceneFormat.kt` and a branch in `SceneResolver.shape`.

**Add a pattern:** subclass `Pattern`, implement `colorAt(point)` in pattern space, add a test, then add a `PatternType` entry in `SceneFormat.kt` and a branch in `SceneResolver.pattern`.

**Add a scene key:** add a property to the matching `@Serializable` class in `SceneFormat.kt` (`@SerialName` for kebab-case keys; make it nullable or give it a default if it is optional), use it in `SceneResolver`, and add a case to `SceneLoaderTest`.

## Performance

### Benchmarks (JMH, `benchmarks/`)

| Class | Measures |
| --- | --- |
| `VectorBenchmark`, `ColorBenchmark`, `MatrixBenchmark` | single math operations (`add`, `dot`, `cross`, `normalized`, `reflect`, `mulPoint`, `mulMatrix`, `inverse`, `viewTransform`, …) |
| `IntersectBenchmark.intersect` | `Ray.intersect` per shape, param `case=<shape>/<hit\|miss>` |
| `NormalAtBenchmark.normalAt` | `Shape.normalAt`, param `shape=sphere\|cube` |
| `IntersectionsBenchmark.hit` | `Intersections.hit()` on lists of 8 intersections (score is per list) |

Inputs come from a fixed-seed generator (`Rng` in `Common.kt`) in batches of 1024. Scores are average **ns per input**. A full run takes about 2.5 minutes. Always use the script: it pins the run to two physical cores at high priority. Comparing two runs of unchanged code, the median change was 3.6% pinned and 5.5% unpinned, and only the unpinned run produced false "regressed" or "improved" verdicts (4 of 29).

```sh
cmd //c "scripts\bench.cmd --save-baseline before"           # before the change
cmd //c "scripts\bench.cmd --baseline before"                # after; prints a comparison table
cmd //c "scripts\bench.cmd Intersect -p case=cube/hit --baseline before"   # filter by regex and/or param
```

- Arguments are JMH options and benchmark regexes, matched against full names: `Vector` also matches `MatrixBenchmark.mulVector`. Anchor the regex (`VectorBenchmark\.`) for exact groups. `-l` lists benchmarks.
- Defaults are 3 forks of 3 × 200 ms warm-up and measurement iterations, set in `BenchmarkLauncher.kt`. Any option you pass wins over the default.
- Baselines are stored in `.benchmarks/baselines/<name>.csv`. That folder is git-ignored and survives `kotlin clean`. A filtered run only replaces the scores it measured. Compare only baselines recorded on the same machine.
- Verdicts:
  - **no change**: within 5%.
  - **within noise**: more than 5%, but the 99.9% confidence intervals overlap. Re-run before trusting it.
  - **REGRESSED** or **improved**: more than 5%, and the intervals don't overlap.
- Some allocation-heavy benchmarks are bimodal across JVM forks. For example, `MatrixBenchmark.mulVector` measured about 8.5 ns in some runs and 21–23 ns in others, with no code change. Wide error bars there are real, not a bug.
- Close the IDE, any indexing and other heavy programs first.

How it works, and the rules for adding a benchmark:

- **Harness.** Kotlin Toolchain has no JMH plugin, and JMH's annotation processor only sees Java sources. `BenchmarkLauncher` therefore runs JMH's *bytecode generator* over the compiled classes, compiles the generated harness with the JDK's javac, then starts `org.openjdk.jmh.Main` in a child JVM. Generated files go to `build/benchmarks/harness/`, results to `build/benchmarks/latest.csv`.
- **New benchmark classes:**
  - Annotate them with `@State`. The module's `allOpen` setting makes them non-final, which JMH requires.
  - Write parameters as `@Param(...) lateinit var x: String`. A `lateinit` property's backing field is public, which JMH's generated code needs.
  - Add `@OperationsPerInvocation(BATCH)` and loop over batched inputs.
- **Defeat dead-code elimination.** Write results into a preallocated output array and return it, or pass them to a `Blackhole`. Never discard a result.
- **Shape benchmarks call `polluteShapeCallSites()` in `@Setup`.** It sends every shape type through `Ray.intersect` and `Shape.normalAt`. Otherwise each JMH fork would only ever see one shape type, and the JIT would inline calls that stay virtual in a real scene.
- **Watch for caches.** `Matrix.inverse()` caches its result, so `MatrixBenchmark.inverse` gets fresh, uncached instances before each invocation from the `UncachedMatrices` state. Look for similar caches before benchmarking anything else.

### Whole-render comparisons

Microbenchmarks miss interactions such as memory traffic and megamorphic calls. For hot-path changes, also compare whole renders:

1. For each variant, run `./kotlin package` and copy `build/tasks/_k-tracer_executableJarJvm/k-tracer-jvm-executable.jar` to `out/<variant>.jar`.
2. Make a copy of a scene with the camera lowered to about 960×640.
3. Alternate `java -jar out/<variant>.jar -r SERIAL <scene copy> out/<variant>.png` at least 7 times per variant, and compare the medians of the printed `Rendered image in …` times. Differences under about 3% are noise. Use the JDK 26 that the toolchain provisioned, under `%LOCALAPPDATA%\JetBrains\Kotlin\extract.cache\*OpenJDK26U*\jdk-26*\bin\java`.
4. Check that the PNGs are byte-identical (`cmp`), unless the image is meant to change.

## Working rules for agents

1. **Verify before you claim.** Run `./kotlin test` (or at least the affected test classes) and report real results, including failures.
2. **Keep the diff minimal and on task.** The working tree often holds the owner's uncommitted, in-progress refactors. Don't reformat, rename or "clean up" unrelated files, and don't revert changes you didn't make.
3. **Don't commit, push or open PRs unless asked.** If you are asked to, work on a branch, not `master`.
4. **Don't touch** `rendered_images/` or `.idea/`, the toolchain versions, or the `kotlin` and `kotlin.bat` wrapper scripts unless the task requires it.
5. **Write scratch output** (renders, logs) outside the repo or under `out/`, which is git-ignored.
6. When behaviour follows the book, preserve the book's semantics and name the chapter or feature in your summary.
7. **Back performance changes with numbers.** For changes in the hot path (intersection, normals, shading, `primitives/`), record a benchmark baseline *before* editing, compare it afterwards, and report the relevant rows. Back a claimed speed-up with a whole-render comparison.
