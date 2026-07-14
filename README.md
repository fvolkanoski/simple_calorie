# Calorie Tracker (Android)

A native Android app built with Kotlin + Jetpack Compose. Everything is stored
locally on the phone (SharedPreferences) — no network calls, no account needed.

Screens: Today (ring + macros + meal log), Progress (7-day chart), Goals (edit targets).

## Option A — Build in the cloud with GitHub Actions (no software to install)

1. Create a new repository on GitHub (github.com → New repository).
2. Upload all the files in this folder to that repo (drag-and-drop works fine
   on the GitHub website — "Add file" → "Upload files" — or use `git push` if
   you're comfortable with git).
3. Go to the repo's **Actions** tab. A workflow called "Build APK" will run
   automatically (or click "Run workflow" if it doesn't start on its own).
4. Wait for it to finish (2–5 minutes on the first run, faster after).
5. Open the finished run, scroll to **Artifacts**, and download
   `calorie-tracker-debug-apk`. Unzip it — inside is `app-debug.apk`.
6. Copy `app-debug.apk` to your phone (email it to yourself, use Google Drive,
   USB cable, whatever's easiest).
7. On your phone, tap the file. Android will ask to allow installs from this
   source the first time — allow it, then install.

This is a debug build, so Android will show a plain "install unknown app"
prompt rather than a Play Store install — that's expected for any app that
isn't distributed through the Play Store.

## Option B — Build locally with Android Studio

1. Install [Android Studio](https://developer.android.com/studio) (free).
2. Open Android Studio → **Open** → select this `CalorieTrackerAndroid` folder.
3. Let it sync (it'll download the Android SDK/Gradle it needs the first time).
4. Plug your phone in via USB with USB debugging enabled (Settings → About
   phone → tap "Build number" 7 times → Developer options → USB debugging),
   or use the wireless pairing option in Android Studio.
5. Hit the green **Run ▶** button. It builds and installs straight onto your
   phone.

This route is better if you want to keep changing the code yourself —
edits + Run is much faster than round-tripping through GitHub each time.

## Project structure

```
app/src/main/java/com/calorietracker/app/
  MainActivity.kt              — entry point
  model/Models.kt              — FoodEntry, Goals data classes
  data/CalorieStore.kt         — local persistence (SharedPreferences + JSON)
  ui/theme/Theme.kt            — color palette
  ui/CalorieTrackerApp.kt      — all screens and composables
```

## Customizing

- Colors: `ui/theme/Theme.kt`
- Default goals: `model/Models.kt` (`Goals()` defaults)
- App icon: regenerate the PNGs in `res/mipmap-*` folders
- Fonts: currently system default (sans/serif/monospace). To match the web
  version exactly, drop `.ttf` files for Fraunces, Manrope, and IBM Plex Mono
  into `res/font/` and reference them as `FontFamily(Font(R.font.xxx))` in
  place of `FontFamily.Monospace` / default text styles.
