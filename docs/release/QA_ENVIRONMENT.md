# QA environment and evidence boundaries

All emulator operations target the dedicated `IhsanDarkFinal` AVD, serial `emulator-5570`. The QA application is `com.example.mol.referenceqa`; the original application ID remains `com.example.mol`. No physical-phone data, existing original application installation, Git reset, commit or push was used for these checks.

Early runtime attempts encountered the Android **SystemUIService** ANR dialog. Saved system logs identify `com.android.systemui` service startup, CPU pressure and concurrent Google service ANRs, not an Ihsan application stack. A native framebuffer and window-focus dump preserve the obstruction. Initial keyboard/focus checks and screenshot attempts failed correctly and are not counted as accepted UI evidence.

The test harness now rejects matrix captures while the Activity does not own focus; selected dialog journeys inspect the foreground package so an actual in-app confirmation dialog is allowed. A false first geometry comparison also paired duplicate category labels before the3582-record library finished loading; the harness waits for source content and pairs occurrences independently.

Between display-size changes, an already-present confirmed SystemUI ANR may be dismissed by reading its accessibility XML and selecting its actual Close-app button. This applies only to the dedicated emulator's known system dialog. It does not dismiss or hide an Ihsan crash/ANR. All accepted matrix cases must still pass the unobscured-window guard. Failed attempts are retained separately; progress messages alone never mean PASS.

The matrix uses actual `wm` pixel sizes and density320:360dp720×1600,393dp786×1704,412dp824×1830,600dp1200×1920. Compose configuration/density enforces the requested RTL/fontScale1.0 or1.3 in the production navigation graph. These are responsive render checks on one Android14/API34 emulator, not a device-farm claim. Top/lower captures are native Android framebuffers; contact sheets only arrange thumbnails and original PNGs remain unchanged.

Video tests use the production official IFrame implementation and API state callbacks. `ANDROID_PLAYING_CONFIRMED` means the official player reported PLAYING for the requested video during a brief QA sample; it is not a full-duration editorial review or a perpetual availability guarantee. Explicit embed failure/external-fallback dispatch is a separate result. HTTP200/oEmbed metadata is never substituted for these Android states.

A neutral locally generated WAV exists only in the instrumentation APK. It tests the actual MediaSession service/decoder/play-pause-seek path, not reciter permission. Unapproved recordings/downloads and direct HLS stay disabled.

Physical compass movement/calibration against a known heading, OEM battery restrictions, exact alarm delivery/reboot behavior and full user-driven external applications remain pending unless an explicit later report records them.
