# TwitchPro — notes for Claude

## Done
- Version number in the settings (1.2.0): under the screen name in the left panel of the TV settings, from
  `BuildConfig.VERSION_NAME`, " (debug)" in the debug builds.

## Conventions
- Every new button of the TV player gets its icon in Settings > Player buttons too: add it to the `icons` map of
  `PlayerButtonSettingsFragment` (SettingsActivity.kt). Player icons are filled (an outline only for a state, like the
  heart of Follow).

## Deliberate decisions (do not "fix" without asking)
- `lint { disable += "ContentDescription" }` in app/build.gradle.kts: the TV UI relies on focus states rather than TalkBack narration, and enabling the rule floods the build with warnings. Revisit as a dedicated accessibility pass.
- `MANAGE_EXTERNAL_STORAGE` in AndroidManifest.xml is required by the TV backup feature: TvBackup.kt writes/overwrites/scans `TwitchPro-backup.zip` in the shared Downloads folder across Android 7–14+ (requested contextually at backup time). Video downloads use app-specific storage (`getExternalFilesDirs(".downloads")`) and do not need it.
- Release signing uses an external keystore via gitignored `keystore.properties`; Release tasks fail loudly without it. The old committed debug keystore was removed from git (kept locally — debug builds use it; fresh clones fall back to the default `~/.android` debug keystore).
