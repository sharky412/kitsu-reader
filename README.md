<div align="center">

# Kitsu

**A free and open-source manga reader for Android with built-in online content sources.**

![Android 6.0](https://img.shields.io/badge/android-6.0+-brightgreen)
[![License](https://img.shields.io/github/license/sharky412/kitsu-reader)](LICENSE)

</div>

Kitsu is a fork of [Kotatsu](https://github.com/KotatsuApp/Kotatsu), which was discontinued by its
original authors in 2025. This fork continues development with a redesigned interface.

### Main Features

* Online [manga catalogues](https://github.com/KotatsuApp/kotatsu-parsers) with 1200+ manga sources
* Search manga by name, genres and more filters
* Favorites organized by user-defined categories
* Reading history, bookmarks and incognito mode support
* Download manga and read it offline; third-party CBZ archives are also supported
* Clean Material You UI, optimized for phones, tablets and desktop
* Standard and Webtoon-optimized customizable reader with gesture support
* Notifications about new chapters, updates feed and manga recommendations
* Integration with manga tracking services: Shikimori, AniList, MyAnimeList, Kitsu
* Password / fingerprint-protected access to the app
* Support for older devices running Android 6.0+

### Building

Requires JDK 17+ and the Android SDK.

```bash
./gradlew assembleRelease
```

Build variants:

| Variant   | Application id                  | Notes                                  |
|-----------|---------------------------------|----------------------------------------|
| `debug`   | `org.koitharu.kotatsu.debug`    | StrictMode and debug tooling enabled    |
| `nightly` | `org.koitharu.kotatsu.nightly`  | Side-by-side install, LeakCanary        |
| `release` | `org.koitharu.kotatsu`          | Distribution build                      |

### Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

### License

Licensed under the [GNU General Public License v3.0](LICENSE), inherited from the upstream project.
