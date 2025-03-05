Pinkt
=====

[![Language](https://img.shields.io/badge/language-kotlin-brightgreen.svg)](https://www.github.com/fibelatti/pinboard-kotlin)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Android CI](https://github.com/fibelatti/pinboard-kotlin/actions/workflows/android-ci.yml/badge.svg?branch=main)](https://github.com/fibelatti/pinboard-kotlin/actions?query=workflow%3A%22Android+CI%22+branch%3Amain)

Pinkt is an unofficial, [FOSS](https://en.wikipedia.org/wiki/Free_and_open-source_software) Android client
for [Pinboard](http://pinboard.in/) and [Linkding](https://github.com/sissbruecker/linkding).

* Pinboard is a fast, no-nonsense bookmarking site for people who value privacy and speed.
* Linkding is a self-hosted bookmark manager that is designed be to be minimal, fast, and easy to set up using Docker.

Downloads
--------

<a href='https://play.google.com/store/apps/details?id=com.fibelatti.pinboard'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width='150' /></a>
<a href='https://apt.izzysoft.de/fdroid/index/apk/com.fibelatti.pinboard'><img alt='Get it on IzzyOnDroid' src='https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png' width='150' /></a>
<a href="https://github.com/fibelatti/pinboard-kotlin/releases/latest"><img alt="Get it on GitHub" src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" width="150"></a>

> [!Note]
>
> Downloads from Google Play are verified by Google Play Protect.
>
> Downloads from IzzyOnDroid are verified
> by [Reproducible Builds](https://android.izzysoft.de/articles/named/iod-rbs-mirrors-clients?lang=en).
>
> Downloads from GitHub can be verified with third-party tools such
> as [AppVerifier](https://github.com/soupslurpr/AppVerifier) using the following SHA-256 digest:
>
> 9E:B9:92:5B:95:8F:C0:C4:EB:FB:99:FD:C5:1E:85:56:02:D0:42:E2:30:AF:91:71:6E:EC:B6:58:AE:04:19:89

Features
--------

Save links from your favorite websites and apps quickly by sharing them to Pinkt.

- Manage all your bookmarks: add, edit, delete, share
- View your saved bookmarks without leaving the app
- Quickly save links from any app using the Android share sheet actions
- Auto-fill bookmarks: Pinkt can optionally auto-fill the title and description of saved URLs
- Search by term: find bookmarks that contain the term in its URL, Title or Description
- Filter by tags
- Six pre-defined filters: All, Recent, Public, Private, Unread and Untagged
- Sync bookmarks and tags
- Cached data for faster usage
- Dark and Light themes
- Dynamic color support
- Portrait and Landscape support
- Optimized for tablets and chromebooks

Pinboard-only features:

- View your saved notes without leaving the app
- Popular bookmarks: see what's trending and save them to your collection

About the project
--------

Pinkt is a playground to study modern Android development.

Here you will find a codebase that encourages the usage of a stateful single source of truth, using Kotlin Coroutines +
Kotlin Flows as foundation for a pragmatic unidirectional dataflow implementation. These are some of the topics you can
expect to see:

- Clean & beautiful UI built with Jetpack Compose and Google's [material design three](https://m3.material.io/)
  guidelines, with support for Material You theming
- Android Jetpack Libraries, including WorkManager and Room (with FTS)
- Kotlin, Coroutines and Flows
- DI using Hilt
- CI with GitHub Actions

Contributing
--------

Anyone is welcome to submit bug reports, feature requests and improvement ideas. Submit yours using
the [provided templates](https://github.com/fibelatti/pinboard-kotlin/issues/new/choose).

License
--------

    Copyright 2019 Filipe Belatti

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
