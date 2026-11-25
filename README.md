# Mixology

## Play Store
<a href='https://play.google.com/store/apps/details?id=com.capstone.nik.mixology&hl=en&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="250" height="100"/></a>

## Overview
Mixology is a Jetpack Compose cocktail app that talks to the Mixology backend. Stock My Bar and see what you can make, ask Ada (an AI bartender) for a drink, import recipes from a link or a social post, write your own, and keep saved recipes plus a shopping list grouped by aisle. Search works by drink name or ingredient. An account is required; new accounts get a 7-day free trial, then a subscription. Phones and tablets (two-pane details) are supported. Spanish strings are included. Appearance follows system, light, or dark mode.

Store listing copy and a screenshot checklist live in `docs/PLAY_STORE.md`.

## Stack
* Jetpack Compose + Material 3
* Navigation Compose (single activity)
* MVI ViewModels
* Room + Kotlin coroutines
* Retrofit + Gson
* Coil
* Hilt
* Glance (home-screen widget)
* Firebase Crashlytics + Analytics

## Setup
The app talks to the Mixology backend (`../mixology-backend`). Point it at a server in `local.properties` (gitignored):

```
MIXOLOGY_API_BASE_URL=http://127.0.0.1:8000/
```

Debug builds fall back to `http://10.0.2.2:8000/`, the emulator's address for a server on the host. On a phone over USB, use `127.0.0.1` with `adb reverse tcp:8000 tcp:8000`. Release and `mix` builds refuse to build unless the URL is set and uses `https://`; CI passes it through the `MIXOLOGY_API_BASE_URL` environment variable.

## Signing
Keystores and their passwords stay out of the repo. Copy `keystore.properties.example` to `keystore.properties` (gitignored) and point `storeFile` at a keystore kept outside the checkout; `keystore.mix.properties` does the same for the `mix` build type. Without those files, release builds are left unsigned.

The two keystores that used to be tracked here are public in this repo's history, along with their passwords, so treat them as compromised and don't sign a release with them.

## Credits
This product uses TheCocktailDB API.

## License
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF THE LICENSE, either express or implied. See the License for the specific language governing permissions and limitations under the License.

Copyright 2016 Nikhil Bhaskar
