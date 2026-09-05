# Local build and verification

What a developer needs in order to run the checks that CI runs.

## Requirements

* JDK 21 (AGP 8.13 requires 17 or newer)
* Android SDK with `platforms;android-36` and `build-tools;35.0.0`
* `local.properties` containing `sdk.dir=<path to your SDK>` (git-ignored)

## Commands

| Purpose | Command |
| --- | --- |
| Unit tests and screenshot verification | `./gradlew :app:verifyRoborazziDebug` |
| Unit tests only | `./gradlew :app:testDebugUnitTest` |
| Re-record screenshot baselines | `./gradlew :app:recordRoborazziDebug` |
| Screenshot diffs without failing | `./gradlew :app:compareRoborazziDebug` |
| Lint | `./gradlew :app:lintDebug` |
| Debug APK | `./gradlew :app:assembleDebug` |

`verifyRoborazziDebug` is the task CI runs: it executes the full unit test suite and additionally
compares rendered components against the baselines in `app/src/test/screenshots`.

## Screenshot baselines

Baselines are committed. When a change is *meant* to alter a component's appearance, re-record and
commit the new images in the same commit as the change, so the diff is reviewable:

```
./gradlew :app:recordRoborazziDebug
git add app/src/test/screenshots
```

Never re-record to make a failing build pass without looking at the diff. That is the entire point
of the baselines.

Components are rendered at `w411dp-h891dp-xhdpi`, in both light and dark, hosted in a plain
`AppCompatActivity` themed with `Theme.Kotatsu`. Resource qualifiers must be written in Android's
documented order — the night qualifier goes between the screen dimensions and the density, and
Robolectric rejects any other ordering.

Layouts containing `CoilImageView` or `CoverImageView` are covered, but not by inflating those views:
both are annotated `@AndroidEntryPoint`, and building the Hilt graph pulls in Coil's `ImageLoader`,
which needs the real HTTP client, which asserts it is off the main thread — where Robolectric runs.
The harness substitutes a plain `ShapeableImageView` at inflation time instead, keeping the layout's
own attributes. These baselines therefore assert the frame around the artwork, not the artwork.

That substitution has a cost worth knowing: the stand-in cannot reproduce the aspect-ratio measuring
that `CoverImageView` does, so any capture of a cover supplies its height explicitly. A layout that
leaves a cover with no height at all still looks right in these images. Only a device catches that.

## If KSP fails on a build you expect to pass

A long-lived Gradle daemon that has already run this build will sometimes make `kspDebugKotlin` fail
with an error that has nothing to do with the change in hand — a Room DAO that "must be annotated",
`WorkerAssistedFactory ... Invalid return type: T`, or a FIR resolution failure. The trigger is a
warm daemon combined with a wiped or force-rerun KSP output (`--rerun-tasks`, `rm -rf app/build`).

It is not a code error, and the error text is not a clue. Restart the daemon and build again:

```
./gradlew --stop
./gradlew :app:assembleDebug
```

This was confirmed by bisecting against a clean worktree of the base commit: the same failure
reproduced there with no local changes at all, and disappeared once the daemon was restarted.

## Sample data on a device

Home, history and updates render existing reading activity, so a fresh install shows nothing and
those screens cannot be reviewed. To fill them:

```
python3 tools/dev/seed-device.py
```

This writes six history entries, six tracked series with unread counts, and locally generated cover
files straight into the app database, then hands the files back to the app user. No network and no
real source are involved, and the timestamps are fixed, so a re-run reproduces the same screen.
Remove the rows again with `--clear`.

Add `--local-manga` to also install a generated CBZ — three chapters of six pages — into the app's
own storage. The app indexes it on its own, and it is the only way to see the parts of the app that
need a real manga when no source is reachable: chapter lists, reading progress, the read button and
the reader itself. Pages are drawn so you can tell them apart without a font: a bar of blocks counts
the chapter, a band across the middle counts the page.

Anything that needs a *remote* source still cannot be judged on such a device. A details screen for
a manga whose source is unreachable sits on a spinner that never resolves, and its progress bar
stays empty, because the chapter list never arrives. That is the environment, not the screen.

It needs a rooted emulator image (`adb root`), so it will not work against a physical device or a
Play-flavoured system image. The app is force-stopped while the database is written; start it
afterwards:

```
adb shell am start -n org.koitharu.kotatsu.debug/org.koitharu.kotatsu.main.ui.MainActivity
```

## If your network intercepts TLS

On networks that terminate TLS with a corporate proxy, `curl` works (it trusts the proxy CA through
the system trust store) while every JVM fails with:

```
javax.net.ssl.SSLHandshakeException: PKIX path building failed
```

Gradle, the Gradle wrapper download and `sdkmanager` are all affected. Rather than modifying the
system JDK, build a private trust store and point the JVM at it:

```bash
security find-certificate -a -c "<your proxy CA name>" -p \
  /Library/Keychains/System.keychain > proxy-ca.pem
cp "$(/usr/libexec/java_home)/lib/security/cacerts" truststore.jks
chmod u+w truststore.jks
keytool -importcert -noprompt -trustcacerts -alias proxy-ca \
  -file proxy-ca.pem -keystore truststore.jks -storepass changeit

export JAVA_TOOL_OPTIONS="-Djavax.net.ssl.trustStore=$PWD/truststore.jks \
  -Djavax.net.ssl.trustStorePassword=changeit"
```

Keep the trust store outside the repository. CI runners are unaffected.
