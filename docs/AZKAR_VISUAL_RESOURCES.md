# Azkar visual resources

The Azkar PNG assets live in `feature/src/main/res/drawable-nodpi` so Android
keeps their authored pixel dimensions and Compose can scale them explicitly.

## Resource groups

- `bg_azkar_*`: reusable Azkar backgrounds.
- `ic_azkar_*`: transparent interface and category icons.
- `img_azkar_*`: category illustrations.
- `ihsan_mosque_sunrise_landscape`: shared wide mosque sunrise artwork.

Use these resources through `com.example.feature.R.drawable` and
`painterResource(...)`. Their normalized Android resource names intentionally
omit source-copy suffixes such as `(1)` and `(2)`.
