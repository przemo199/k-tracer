# AGENT.md

Guidance for AI coding agents (Claude Code, Codex, Copilot, Cursor, …) working in this repository.
Humans are welcome too. `CLAUDE.md` simply imports this file.

## What this is

`k-tracer` is a CPU ray tracer written in Kotlin, following *The Ray Tracer Challenge* (Jamis Buck).
It is a CLI: it reads a YAML scene and writes a PNG.

```
ktracer [-r SERIAL|PARALLEL] <scene.yaml> <output.png>
```

Single Gradle module, no runtime services, no network access at runtime. About 5k lines of code, with roughly 290 unit tests.

## Commands

Always use the Gradle wrapper. On Windows PowerShell use `.\gradlew.bat`; in bash, Git Bash or CI use `./gradlew`.

| Task | Command |
| --- | --- |
| Run all tests | `./gradlew test` |
| Run one test class | `./gradlew test --tests "org.example.ktracer.shapes.SphereTest"` |
| Run one test method | `./gradlew test --tests "org.example.ktracer.shapes.SphereTest.sphere normal"` |
| Compile only (fast feedback) | `./gradlew compileKotlin compileTestKotlin` |
| Full build | `./gradlew build` |
| Render a scene | `./gradlew run --args="scenes/cover.yaml out/cover.png"` |

- Test reports are written to `build/reports/tests/test/index.html`, and the raw XML (with failure messages) to `build/test-results/test/`.
- Configuration cache and build cache are enabled, so a warm `./gradlew test` takes a few seconds.
- Pass `--console=plain` when you parse Gradle output.

### Rendering for verification

Every scene in `scenes/` is configured for 3840 px output, which is slow. To check a change visually:

1. Copy the scene somewhere outside the repo, such as your scratch or temp directory.
2. Lower the camera's `width` and `height` (for example to 400×200).
3. Render the copy to a path outside `rendered_images/`.

Do **not** overwrite `rendered_images/*.png` unless you were asked to regenerate the README images.

## Toolchain: read this before you write Kotlin

This project deliberately runs on bleeding-edge tooling. Some of the syntax postdates many models' training data. **Do not "fix" it back to older forms.**

- **Kotlin `2.5.0-Beta2`**, **JDK 27** (Gradle toolchain, auto-provisioned through the foojay resolver), **Gradle 9.x**.
- Experimental compiler flags in `build.gradle.kts`:
  - `-Xcompanion-blocks-and-extensions`: companions are written as `companion { ... }`, **not** `companion object { ... }`. Every file in the project uses this form. Follow it.
  - `-Xfull-value-classes`: multi-field `value class` declarations, such as `Color` and `Coordinates`, are valid here.
- Do not bump Kotlin, Gradle, the JDK or dependency versions, or add or remove compiler flags, unless the task asks for it.
- CI (`.github/workflows/tests.yaml`) runs `./gradlew test` on Windows, Linux and macOS.

## Architecture

All code is under `src/main/kotlin/org/example/ktracer/`:

| Package | Role |
| --- | --- |
| `KTracer.kt` | CLI entry point (Clikt). `main` → `SceneLoader` → `Camera.render` → `Canvas.saveAsPng`. |
| `globals.kt` | `EPSILON`, `coarseEquals`, `squared()`, `solveQuadratic`. |
| `primitives/` | `Tuple` → `Point` (w=1) and `Vector` (w=0). Also `Color`, `Matrix` (4×4; `Transformation` is a typealias), `Transformations` (factory for `translation`, `scaling`, `rotationX/Y/Z`, `shearing`, `viewTransform`, …), `Light`, `Coordinates` (u,v). |
| `composites/` | `Ray`, `Intersection`, `Intersections` (a reusable buffer), `ComputedHit` (precomputed shading data), `Material` (Phong), `World` (shading, shadows, reflection, refraction), `Camera` (rays and serial/parallel render), `Canvas` (pixels and PNG output through scrimage). |
| `shapes/` | `Shape` base class plus `Sphere`, `Plane`, `Cube`, `Cylinder`, `Cone`, `Triangle`, `Group`, `ConstructiveSolidGeometry`, `BoundingBox`. |
| `colorproviders/` | `ColorProvider` interface. `patterns/` (solid 3D patterns: stripe, gradient, ring, checker), `textures/` (UV textures), `mappings/` (point → UV: planar, spherical, cylindrical, cube). |
| `scene/` | `SceneLoader` parses the YAML scene format; `Keyword` holds the YAML keys. |

### Render pipeline

`Camera.rayForPixel` → `World.colorAt` → `collectIntersections` (each shape: `Ray.intersect` → `Shape.localIntersect`) → `Intersections.hit()` → `Intersection.prepareComputations` → `World.shadeHit` (lighting per light, shadow rays, and recursive `reflectedColor`/`refractedColor`, capped by `World.MAX_REFLECTION_ITERATIONS`).

### Invariants and gotchas

- **Floating-point equality.** Tuples compare through `coarseEquals` with `EPSILON`. In tests, `assertEquals` on `Point`, `Vector` and `Matrix` relies on this tolerant `equals`. Do not compare raw doubles from geometry with `==`.
- **Object space.** A shape implements `localIntersect` and `localNormalAt` in *object space*. `Shape.normalAt` and `Ray.intersect` handle the world ↔ object transforms, including `parent` chains for `Group` and CSG.
- **Matrices are effectively immutable.** `Matrix.inverse()` is cached on the instance. Never mutate a matrix's elements after construction. Build a new transform instead.
- **Transform order.** `A * B` applies `B` first. For example, `translation(...) * scaling(...)` scales, then translates.
- **`Intersections` is a reused scratch buffer.** It is passed down the call stack and is per-thread in parallel rendering (`ThreadLocal`). `World.collectIntersections` clears it. Do not keep references to it across calls, and do not share one between threads.
- **Hot path.** Intersection and shading code runs billions of times per render. Avoid allocations, lambdas that capture state, `Sequence`s and boxing in `localIntersect`, `localNormalAt` and `World` shading unless you have measured the cost.
- **Scene format coverage.** `SceneLoader` currently handles `camera`, `light`, `plane`, `sphere`, `cube`, `cone`, `cylinder` and the patterns `stripes`, `gradient`, `rings`, `checkers`. Groups, triangles, CSG and textures exist in code but are **not** yet in the YAML format. Named definitions are dispatched by suffix: `*-color`, `*-material`, `*-pattern`, `*-transform` or `*-object`.

## Conventions

- Kotlin official code style (`kotlin.code.style=official`), 4-space indent, no wildcard imports, imports sorted lexicographically.
- Prefer the concise expression-body form for one-line functions (`fun x() = ...`). This matches recent refactors.
- Operator overloads for math (`plus`, `times`, `dot`, `cross`) are idiomatic here.
- Constants live in `companion { }` blocks (for example `Vector.UP`, `Point.ORIGIN`, `Color.WHITE`).

### Tests

- Tests live in `src/test/kotlin/...`, mirroring the main package, one `XxxTest` per class.
- Use `kotlin.test` (`@Test`, `assertEquals`, …) on JUnit Platform. Use `junit-jupiter-params` for parameterised tests.
- Name test methods as backtick sentences, usually echoing the book's scenarios: ``fun `ray intersects sphere at two points`()``.
- Every new shape, pattern or math routine needs unit tests. Every bug fix needs a regression test.

### Recipes

**Add a shape:** subclass `Shape` in `shapes/`, then:

1. Implement `localIntersect` (push `Intersection`s into the passed buffer), `localNormalAt`, `boundingBox()`, `equals`, `hashCode` and `toString`.
2. Add `XxxTest` covering hits, misses, tangents, normals and the bounding box.
3. Optionally wire it into `SceneLoader.parseScene`.

**Add a pattern:** subclass `Pattern`, implement `colorAt(point)` in pattern space, add a test, and add a `type` branch in `SceneLoader.parseColorProvider`.

**Add a scene keyword:** add a constant to `scene/Keyword.kt` and use it from `SceneLoader`. Don't scatter string literals.

## Working rules for agents

1. **Verify before you claim.** Run `./gradlew test` (or at least the affected test classes) and report real results, including failures.
2. **Keep the diff minimal and on task.** The working tree often holds the owner's uncommitted, in-progress refactors. Don't reformat, rename or "clean up" unrelated files, and don't revert changes you didn't make.
3. **Don't commit, push or open PRs unless asked.** If you are asked to, work on a branch, not `master`.
4. **Don't touch** `rendered_images/` or `.idea/`, the toolchain versions, or `gradle/wrapper/*` unless the task requires it.
5. **Write scratch output** (renders, logs) outside the repo or under `out/`, which is git-ignored.
6. When behaviour follows the book, preserve the book's semantics and name the chapter or feature in your summary.
