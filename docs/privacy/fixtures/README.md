# Synthetic privacy fixtures

`synthetic-profile.json` was authored from scratch for this remediation. All identity fields are explicit synthetic labels; none were sampled, masked, hashed, or transformed from a real user's data.

The phone placeholder is intentionally not a telephone number. Use it for documentation/display examples, not tests requiring a valid phone number. No exported phone database is an approved fixture. Tests requiring persistence must construct temporary databases from schema and synthetic rows at runtime.

Do not copy device identifiers, profiles, worship records, screenshots, logcat, or database exports into this directory. Keep Room schema JSON, migrations, Kotlin tests, licensed application assets, and Gradle inputs in their existing source locations.
