# AI Agent Task — Runtime Bottom Navigation Match Without Video Attachment

## Important operating mode

Execute this task in **Agent Mode** inside the currently opened Android project **mol** only.

## Mandatory repository boundary

Apply all fixes directly inside the existing project root:

```text
mol/
```

Do not create another project, copy, worktree, repository, folder, or branch.

Remain in the current `mol` folder and on the current Git branch.

The task is to modify the existing `mol` source files in place.

The original runtime videos cannot be attached to this conversation. Therefore, treat the written observations in this document as the authoritative visual specification extracted from those videos.

Do not begin implementation until the current working tree is inspected and any unrelated Quran/audio/notification work is separated from this UI task.

---

# 1. Role

Act as a senior Android Jetpack Compose engineer specialized in:

- Runtime UI debugging
- Animated bottom navigation
- Navigation Compose
- RTL Arabic layouts
- WindowInsets
- Light and Dark themes
- Accessibility
- Visual regression testing
- State/animation synchronization

The goal is to fix the existing Ihsan Bottom Navigation so that its runtime behavior matches the intended polished design, without changing application logic, routes, architecture, or back-stack behavior.

---

# 2. Current expected repository state

The latest known state before this task was:

```text
Branch:
fix/audio-runtime-adhan-quran

Known HEAD:
74a9e1a
```

The working tree may contain uncommitted changes related to:

- `MainActivity.kt`
- `AppNavHost.kt`
- Quran audio files
- Notification click routing
- Untracked reports or screenshots

Before changing anything, run:

```bash
git branch --show-current
git rev-parse --short HEAD
git status --short
git diff --stat
git log --oneline --decorate -10
```

Do not assume the branch or HEAD still matches the values above. Report the actual state.

---

# 3. Working-tree safety rules

Do not run:

```bash
git reset
git restore
git clean
git stash
git add .
git commit -a
```

Do not discard any existing work.

Classify all current uncommitted changes into:

1. Quran audio changes
2. Notification click-routing changes
3. MainActivity changes
4. Navigation changes
5. Bottom-navigation changes
6. Documentation/screenshots/temp files
7. Unknown changes requiring review

Do not mix unrelated audio or notification work with this UI task.

Treat unrelated changes as protected work in progress.

Do not inspect the entire repository diff repeatedly. Inspect only whether the files targeted by this Bottom Navigation task already contain uncommitted changes.

Protected files that must be left untouched unless the user explicitly changes scope:

- `MainActivity.kt`
- `AppNavHost.kt`
- `QuranViewModel.kt`
- `AudioPlayerHandler.kt`
- `QuranAudioService.kt`
- Adhan audio/channel files
- Notification click-routing files

If one of the actual Bottom Navigation target files already contains unrelated changes, stop only for that specific file and report:

- File
- Diff summary
- Why it conflicts
- Recommended handling

Do not create or switch branches.

Continue inside the current `mol` project and current branch. Existing unrelated uncommitted work must remain untouched.

---

# 4. Confirmed runtime visual problems

The original runtime videos showed the following confirmed issues.

## 4.1 Invisible selected icon

On Home, the selected item appeared like a blank white circle.

Observed behavior:

- The selected icon changed to white immediately.
- The green selection indicator had not yet reached the new item.
- The icon became white over the white navigation surface.
- The selected halo was also white/translucent.
- The result looked like an empty white circle.

## 4.2 Duplicate selected states

During navigation from Home to Ehsan:

1. The Ehsan heart icon changed to selected immediately.
2. The old selection indicator remained under Home.
3. The indicator then moved toward Ehsan.
4. For part of the transition, two items visually looked selected.
5. After the indicator arrived, the selected icon could disappear inside the white halo.

This indicates state/animation decoupling.

## 4.3 Clipped Arabic labels

The labels:

- الرئيسية
- إحسان
- حسابي

were clipped at the bottom. Only small letter marks/dots were visible.

The current bar height was reported as approximately `72.dp`, while:

- Icon/halo area consumed around `48.dp`
- Vertical padding and spacing consumed additional space
- Label font needed approximately `15–16sp` line height
- The total content exceeded the available height
- Clipping occurred at the bar/container level

## 4.4 Excessive animated shadow

The moving indicator used approximately:

- `9.dp` elevation while moving
- dark-green shadow tint around alpha `0.28`

This produced a wide gray/green shadow inconsistent with the soft visual language of Ihsan.

## 4.5 Wrong bottom-bar visibility

The bar appeared on child screens where it should be hidden, including runtime examples such as:

- Haram/Madinah Live
- Add Donation

The implementation reportedly used direct route equality and a default visual index of Home for unknown routes.

This caused:

- Child destinations to show the bar
- Unknown routes to visually select Home
- The bar to remain visible while a non-root screen was shown

## 4.6 Content overlap

Bottom content and Ehsan UI elements could appear behind or too close to the navigation bar.

Possible cause:

- Outer Scaffold inner padding
- Manual bottom Spacer such as `88.dp`
- `navigationBarsPadding()`
- FAB offset
- Fixed bottom padding

may be combined inconsistently.

---

# 5. Root causes already identified

Treat the following as the current diagnosis to verify against the code.

## 5.1 State/animation race condition

`IhsanBottomNavigationItem` changes its `selected` appearance immediately after a tap, while `AnimatedSelectionIndicator` moves separately over roughly `280ms`.

This causes the new icon to use selected colors before the indicator reaches it.

## 5.2 Two separate selection sources

The item visuals and indicator position appear to depend on different states, such as:

- a local clicked/selected index
- current navigation destination
- route-derived index
- animation target state

These states are not synchronized.

## 5.3 Fixed-height overrun

The bar/container height is too small for:

- 40–48dp halo
- icon
- spacing
- Arabic label
- vertical padding
- system navigation inset

## 5.4 Wrong fallback behavior

Unknown/child routes appear to use:

```kotlin
selectedIndex = 0
```

or equivalent, which falsely selects Home.

## 5.5 Route-visibility logic too broad

The current visibility rule may depend on:

- direct string equality
- parent destination hierarchy
- default selected index
- graph membership

instead of an explicit allowlist of root destinations.

---

# 6. Scope of this implementation

Modify only presentation/navigation-UI files required for the fix, such as:

- `IhsanBottomNavigation.kt`
- `MainScreen.kt`
- bottom-navigation-specific components
- route-to-bottom-destination presentation helper
- design-system navigation colors/dimens/shapes
- `EhsanScreen.kt` only if duplicated bottom padding is proven
- Compose previews
- UI/source tests

Do not modify `AppNavHost.kt` in this task.

Use the destination state already exposed to `MainScreen.kt`. If the current code truly makes the fix impossible without touching `AppNavHost.kt`, stop and explain the exact missing presentation input before modifying it.

Do not mix notification deep-link work into this task.

---

# 7. Files and systems that must not change

Do not change:

- Navigation routes
- Route names
- Navigation graph structure
- Back-stack behavior
- `saveState`
- `restoreState`
- `launchSingleTop`
- ViewModels
- Repositories
- Use cases
- Room
- DataStore
- Dependency injection
- Quran audio
- Adhan audio
- Prayer calculations
- Prayer scheduling
- Ehsan business logic
- Profile business logic
- Feature flags
- `applicationId`
- R8
- Manifest
- IhsanPlus contracts

This is a presentation/navigation-UI correction only.

---

# 8. Required destination policy

Use one explicit top-level allowlist.

## Bottom navigation visible

Only on:

```text
Home root
Ehsan/Donations root
```

## Bottom navigation hidden

Hide it on:

```text
Profile
Tasbih
Azkar
Dua
Dua details
Hadith
Asma
Qibla
Prayer
Quran list
Quran reader
Haram Live
Madinah Live
Add Donation
Request Help
Ihsan Details
Donation Details
Settings
Reminders
Statistics
Edit Profile
Search
Daily Activities
Auth flows
All child/detail routes
Unknown routes
```

Profile must remain without the bar according to the approved profile design.

Do not infer bar visibility from a default selected index.

Prefer a pure, testable resolver such as:

```kotlin
fun resolveBottomBarDestination(route: String?): BottomBarDestination?
```

or an equivalent implementation.

Behavior:

- Home route → Home destination
- Ehsan root route → Ehsan destination
- Profile route → `null`
- Child route → `null`
- Unknown route → `null`

Do not return Home as fallback.

---

# 9. One source of truth for selection

The current navigation destination must be the only source of truth.

Required flow:

```text
User tap
→ navigation callback
→ NavController destination updates
→ selected destination is derived
→ one shared transition starts
→ indicator, icon, halo, label, and shadow animate together
```

Do not update visual selected state directly inside `onClick`.

Remove any parallel local visual state such as:

```kotlin
remember { mutableIntStateOf(...) }
```

when it duplicates navigation destination state.

At all times:

- Only one destination is selected semantically
- Unknown routes have no selected bottom destination
- Child screens do not visually select Home

---

# 10. Unified animation

Use one shared transition or one coordinated animated progress source for:

- Indicator horizontal position
- Indicator color if needed
- Icon tint
- Label color
- Halo alpha
- Halo scale
- Icon scale
- Shadow elevation
- Border alpha

Recommended duration:

```text
240ms–280ms
```

Recommended easing:

```text
FastOutSlowInEasing
```

or a soft spring without excessive bounce.

Do not create independent animations that can target different selected states.

---

# 11. Synchronize item visuals with indicator movement

Do not switch the new icon to white immediately.

Derive a visual selection fraction from the animated indicator position.

Conceptual example:

```kotlin
val fraction = (
    1f - abs(animatedIndicatorIndex - itemIndex)
).coerceIn(0f, 1f)
```

Use the fraction to interpolate:

- Icon color
- Label color
- Halo alpha
- Icon scale
- Halo scale
- Border alpha

Expected visual behavior:

- Previous item gradually loses emphasis
- New item gradually gains emphasis
- White selected icon is never shown over a white surface
- Halo does not appear before the selection background reaches the item
- No frame shows two fully selected visual states

Semantics should still report only the actual current destination as selected.

---

# 12. Light theme specification

Use existing design-system semantic tokens.

Suggested behavior:

```text
Bar surface:
Light surface / white elevated surface

Selected indicator:
Ihsan green #073028 through a semantic token

Selected icon:
onPrimary / white

Selected label:
High-contrast selected content

Unselected icon:
onSurfaceVariant

Unselected label:
onSurfaceVariant

Halo:
Subtle translucent light surface drawn only over the green indicator

Outline:
Very subtle

Shadow:
Soft and low
```

Do not place a white icon over a white halo on a white bar when the green indicator has not arrived.

---

# 13. Dark theme specification

Use the project-wide Dark Mode semantic tokens already introduced.

Suggested behavior:

```text
Bar:
navigationSurface or surfaceElevated

Indicator:
navigationIndicator / dark brand container

Selected icon:
selectedContent with strong contrast

Selected label:
selectedContent

Unselected icon/text:
onSurfaceVariant

Halo:
navigationIconHalo

Outline:
navigationOutline

Shadow:
Low elevation plus tonal separation
```

Do not use:

- `Color.White` as a fixed background
- dark brand green as text on a dark surface
- heavy black shadow
- neon glow
- Light-only hardcoded values

Light and Dark must remain visually consistent.

---

# 14. Correct the bar height and Arabic label clipping

Do not solve clipping by reducing touch targets.

Requirements:

- Touch target ≥ 48dp
- Halo around 40–44dp
- Icon area stable
- Spacing around 3–4dp
- Label font around 11–12sp
- Line height at least 15–16sp
- Arabic dots and diacritics must remain visible
- Bottom inset must be included correctly

The visual bar content will likely require approximately:

```text
80dp–88dp
```

before or together with navigation-bar inset, depending on the existing layout.

Do not use:

- negative text offsets
- tiny fixed heights
- `requiredHeight` smaller than content
- clipping Arabic glyphs
- AnimatedVisibility that removes/re-adds labels
- line height too small for Arabic

The label should always remain in the layout. Animate color/alpha only if needed.

---

# 15. Recommended item layout

Each item should use a stable layout similar to:

```text
Box or Column
  Stable halo/icon area
  Small spacing
  Label
```

Rules:

- Halo must not change measured item height
- Icon scaling should use `graphicsLayer`
- Label should remain permanently laid out
- Selection should not shift the item vertically
- The three items should receive equal horizontal width
- No selected item may push adjacent items

---

# 16. Shadow correction

Replace the current excessive moving shadow.

Recommended values:

```text
Indicator resting elevation:
1dp–2dp

Indicator moving elevation:
3dp–4dp maximum

Shadow alpha:
approximately 0.10–0.16

Bar elevation:
low and stable
```

In Dark Mode, use tonal surface contrast and outline rather than relying on shadow.

Do not use the previous approximately `9.dp` moving elevation.

---

# 17. RTL position calculation

The indicator position must use:

- Actual layout width
- Equal item widths
- Item count
- Current layout direction
- Selected destination index

Do not use fixed pixel offsets.

Verify visual direction in RTL for:

- Home → Ehsan
- Ehsan → Home
- Reopening on Ehsan
- Returning from a child route
- Fast repeated taps
- Theme change
- Configuration recreation

Expected order:

```text
Home on the right
Ehsan in the center
Profile on the left
```

Even though Profile remains hidden as a root-screen bottom bar according to current product policy, the component preview/tests may still represent all three items if the component itself supports them.

---

# 18. Content padding and overlap

Audit:

- Outer `Scaffold` inner padding
- Bottom navigation height
- `navigationBarsPadding()`
- Manual `Spacer`
- screen-level bottom padding
- FAB position

Focus on:

- Home
- Ehsan

Requirements:

- Final card can scroll fully above the bar
- No content hidden behind the bar
- No duplicated blank bottom space
- Ehsan FAB remains above the bar
- FAB does not use a disconnected magic offset
- Child screens do not reserve bottom-bar space after the bar is hidden

If `EhsanScreen` uses a manual `88.dp` spacer while outer scaffold padding already covers the bar, remove only the proven duplicate.

Do not change Ehsan callbacks or business behavior.

---

# 19. Bottom-bar show/hide transition

Correctness is more important than animation.

A small fade/vertical slide is acceptable only if it:

- Does not delay navigation
- Does not leave empty bar space
- Does not flash on child routes
- Does not recreate NavHost
- Does not produce a white frame

If this introduces instability, use immediate correct visibility.

---

# 20. Accessibility

Each item must preserve:

- `Role.Tab`
- Correct `selected` semantics
- Content description
- Label
- Touch target ≥ 48dp
- Good contrast
- TalkBack support

Only one item may expose `selected = true`.

Do not use color as the only selected cue. The selected state should also be indicated through:

- Container/indicator
- Icon style or scale
- Semantics
- Label emphasis

---

# 21. Previews

Add previews for:

- Home selected — Light
- Ehsan selected — Light
- Profile selected — Light
- Home selected — Dark
- Ehsan selected — Dark
- Profile selected — Dark
- RTL 320dp
- RTL 360dp
- RTL 430dp
- Large font scale

The previews must use the real theme.

Preview data must remain preview-only.

---

# 22. Tests

## 22.1 Route visibility tests

Verify:

```text
Home → visible
Ehsan root → visible
Profile → hidden
Add Donation → hidden
Request Help → hidden
Ihsan Details → hidden
Donation Details → hidden
Live Haram → hidden
Live Madinah → hidden
Tasbih → hidden
Azkar → hidden
Dua → hidden
Prayer → hidden
Quran → hidden
Quran Reader → hidden
Settings → hidden
Reminders → hidden
Statistics → hidden
Unknown route → hidden
```

## 22.2 Selection tests

Verify:

- One route resolves to one bottom destination
- Unknown route does not resolve to Home
- No duplicate local selected state
- Tap invokes correct callback once
- Exactly one selected semantic item
- Destination update moves indicator target
- Current item retap does not alter back-stack behavior

## 22.3 Layout tests

Verify:

- All labels exist
- Arabic labels are not clipped
- Three items exist in the component
- Touch targets are correct
- RTL order is correct
- Light and Dark render
- Child routes do not reserve bar space

Do not write brittle pixel-perfect animation tests.

---

# 23. Runtime acceptance without original videos

Because the videos are unavailable to the agent, use the following written acceptance matrix as the runtime reference.

## Home initial state

Expected:

- Home icon visible
- Home label fully visible
- Green indicator under Home
- No blank white circle
- Ehsan and Profile unselected
- No clipped text

## Home → Ehsan

Expected:

- Indicator moves smoothly to center
- Home gradually loses selected emphasis
- Ehsan gradually gains selected emphasis
- Heart never becomes invisible
- No two fully selected items
- No wide gray shadow
- Label remains visible throughout

## Ehsan → child Add Donation

Expected:

- Bottom bar disappears
- No Home indicator fallback
- Child screen uses full available bottom space
- No reserved empty bar area
- Back returns to Ehsan with correct selection restored

## Home → Live

Expected:

- Bottom bar disappears
- No Home indicator remains
- Back returns to Home with Home selected

## Profile

Expected:

- No Bottom Navigation
- Approved profile design remains unchanged

## Light and Dark

Expected:

- Same layout
- Correct theme colors
- Selected icon remains visible
- No white-on-white or dark-on-dark state
- Shadow/outline appropriate per theme

---

# 24. Runtime recording after implementation

If a physical device is available, record a new short video showing:

1. Home initial state
2. Home → Ehsan
3. Ehsan → Add Donation
4. Back to Ehsan
5. Home → Live
6. Back to Home
7. Profile
8. Light Mode
9. Dark Mode
10. Fast repeated taps

Even though the original videos are unavailable, compare the new runtime result against the written acceptance matrix above.

Do not claim runtime verification if no device was used.

---

# 25. Suggested commits

Use separate commits:

```text
test(navigation-ui): capture bottom bar route and selection behavior

fix(navigation-ui): unify selection indicator and item transition

fix(navigation-ui): correct bottom bar sizing and rtl layout

fix(navigation-ui): restrict bar visibility to root destinations

fix(navigation-ui): align content padding and ehsan fab

test(navigation-ui): verify runtime acceptance
```

Do not combine these commits with:

- Quran audio
- Notification routing
- Adhan audio
- Unrelated Dark Mode changes

---

# 26. Verification commands

Run:

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
./gradlew :designsystem:testDebugUnitTest
./gradlew :feature:testDebugUnitTest
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug --no-parallel
```

If a device is connected:

```bash
./gradlew :app:installDebug
```

---

# 27. Final report

Create:

```text
docs/RUNTIME_BOTTOM_NAVIGATION_IMPLEMENTATION_REPORT.md
```

Include:

1. Starting branch and commit
2. Initial working-tree state
3. Handling of unrelated dirty changes
4. Final branch and commit
5. Written runtime reference used
6. Root causes
7. Selected-state source before/after
8. Animation synchronization
9. Indicator behavior
10. Icon and label color behavior
11. Height and clipping fix
12. Shadow changes
13. Route visibility allowlist
14. Hidden child routes
15. Profile behavior
16. Content padding
17. Ehsan FAB
18. Light Mode result
19. Dark Mode result
20. RTL result
21. Accessibility result
22. Build results
23. Test results
24. Lint result
25. Device verification
26. New runtime video result if available
27. Known differences or remaining issues
28. Confirmation that business logic and routes were unchanged

Append:

```bash
git status --short
git diff --stat
git log --oneline --decorate -15
```

---

# 28. Stop conditions

Stop and report instead of continuing if:

- The fix requires changing route names
- The fix requires changing navigation graph structure
- The fix requires changing ViewModels
- The fix requires changing business logic
- The fix requires changing Profile behavior
- Existing AppNavHost changes cannot be separated safely
- The fix conflicts with Quran or notification work
- The current route cannot be resolved reliably
- Light Mode regresses
- Dark Mode regresses
- Content padding cannot be corrected without architecture changes

---


# 28A. Immediate execution instruction for project mol

After the three read-only checks below, begin implementation immediately inside the current `mol` project:

```bash
git branch --show-current
git rev-parse --short HEAD
git status --short
```

Do not run a repository-wide `git diff` unless a target file is already modified.

Primary target files:

```text
IhsanBottomNavigation.kt
MainScreen.kt
Bottom-navigation resolver/helper
Bottom-navigation tests
EhsanScreen.kt only if duplicate bottom padding is proven
```

Do not create a commit until the user reviews the resulting diff.

After implementation, show:

```bash
git status --short
git diff --stat
```

and summarize only the files changed for this Bottom Navigation task.

# 29. Final success criteria

The task succeeds only when:

- There is one source of truth for selection
- Indicator, icon, label, halo, and shadow animate in sync
- No white icon appears over a white background
- No duplicate selected visual state appears
- Arabic labels are fully visible
- Shadow is soft and controlled
- Home and Ehsan root are the only screens showing the bar
- Profile does not show the bar
- All child/detail routes hide the bar
- Unknown routes do not visually select Home
- Content and FAB do not overlap the bar
- Light Mode remains correct
- Dark Mode remains correct
- RTL remains correct
- Routes remain unchanged
- Back-stack behavior remains unchanged
- Business logic remains unchanged
- Debug build passes
- Release build passes
- Tests pass
- Lint passes

Final classification:

```text
Runtime Bottom Navigation Match Complete
Selection State Unified
Route Visibility Corrected
Label Clipping Fixed
Light and Dark Preserved
Business Logic Unchanged
Navigation Routes Unchanged
```
