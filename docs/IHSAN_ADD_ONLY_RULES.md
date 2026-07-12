# Ihsan Add-Only Implementation Rules

## 1. Purpose
Protect the current working application while adding future improvements.

## 2. Forbidden Actions
- Do not delete existing files.
- Do not rename existing files.
- Do not move existing files.
- Do not refactor existing packages.
- Do not modify existing composables, ViewModels, or repositories.
- Do not modify `IhsanDatabase.kt` or Room DAOs/Entities.
- Do not modify `AppNavHost.kt` or `Screen.kt`.
- Do not modify Gradle files or add new dependencies.

## 3. Allowed Actions
- Add new documentation files.
- Add new Kotlin files under new packages (e.g., `feature/.../ihsanplus/`).
- Add new isolated models, use cases, or components.
- Add new preview-only UI for testing.
- Add new DTOs or contracts for future integration.
- Add new test files for new features.
- Add integration request documentation.

## 4. Integration Rule
If an existing file must be changed, do not change it. Add an entry to `docs/APPEND_ONLY_INTEGRATION_REQUEST.md`.

## 5. Database Rule
Do not modify `IhsanDatabase`, entities, DAOs, version, or migrations. Use DataStore or a separate future enhancement database only when explicitly approved.

## 6. Navigation Rule
Do not modify `Screen.kt` or `AppNavHost.kt`. Create route proposals only in the integration request document.

## 7. Design System Rule
Use the existing design system tokens and components. Do not invent new tokens or modify existing theme files.

## 8. Dependency Rule
Do not modify Gradle files or add libraries. If needed, create `docs/DEPENDENCY_REQUESTS.md`.

## 9. Build Rule
Run `./gradlew assembleDebug` after adding new files to ensure the project still builds.

## 10. Git Rule
Run `git status --short` and ensure existing files were not modified by your actions.
