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

## Important: one-time reinstall needed

Earlier builds were signed with a different, randomly-generated key on every
GitHub Actions run (each run is a fresh machine with no memory of the last
one). That's why updating without uninstalling gave a package conflict error,
and it also meant the widget-refresh code from a previous update never
actually made it onto your phone.

This version fixes that permanently by committing a fixed `debug.keystore` to
the repo, so every build — from GitHub Actions or from Android Studio — signs
with the same key from now on.

**You'll need to uninstall the app one more time** to move onto this new,
consistent signing key (this one time will lose your logged data, sorry —
after this it won't happen again). From this point forward, installing a new
APK over the old one will update in place and keep your data, no uninstall
needed.

## Home screen widget

The app now includes a small widget showing "X kcal left" (or "over" in red
if you've gone past your goal). Tapping it opens the app.

To add it after installing the app:
1. Long-press an empty spot on your home screen.
2. Tap **Widgets**.
3. Find **Calorie Tracker** in the list and drag its widget onto your screen.

It updates automatically whenever you add food or change your goals in the
app. It also refreshes on its own roughly every 30 minutes in the background
(standard Android behavior for widgets), so if you log food, then don't open
the app again for a while, it'll still be accurate.

## Project structure

```
app/src/main/java/com/calorietracker/app/
  MainActivity.kt              — entry point
  model/Models.kt              — FoodEntry, Goals data classes
  data/CalorieStore.kt         — local persistence (SharedPreferences + JSON)
  ui/theme/Theme.kt            — color palette
  ui/CalorieTrackerApp.kt      — all screens and composables
  widget/CalorieWidget.kt      — home screen widget (Glance) + its receiver
app/src/main/res/xml/calorie_widget_info.xml   — widget size/behavior config
app/src/main/res/layout/widget_loading.xml     — placeholder shown before first data load
```

## Customizing

- Colors: `ui/theme/Theme.kt`
- Default goals: `model/Models.kt` (`Goals()` defaults)
- App icon: regenerate the PNGs in `res/mipmap-*` folders
- Fonts: currently system default (sans/serif/monospace). To match the web
  version exactly, drop `.ttf` files for Fraunces, Manrope, and IBM Plex Mono
  into `res/font/` and reference them as `FontFamily(Font(R.font.xxx))` in
  place of `FontFamily.Monospace` / default text styles.
