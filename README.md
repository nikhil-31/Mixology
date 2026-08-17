# Mixology

## Play Store
<a href='https://play.google.com/store/apps/details?id=com.capstone.nik.mixology&hl=en&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="250" height="100"/></a>

## Overview
Mixology is a Jetpack Compose cocktail browser backed by [TheCocktailDB](https://www.thecocktaildb.com/). Browse Hot picks, filter by alcohol, category, ingredient, or glass, search the catalog, and swipe through Randomixer. Saved drinks live in Room and appear in a home-screen widget. Phones and tablets (two-pane details) are supported. Appearance follows system, light, or dark mode.

## Stack
* Jetpack Compose + Material 3
* Navigation Compose (single activity)
* MVI ViewModels
* Room + Kotlin coroutines
* Retrofit + Gson
* Coil (screens) / Picasso (widget)
* Dagger 2
* Firebase Crashlytics + Analytics

## Setup
Put your TheCocktailDB API key in `local.properties` (gitignored):

```
COCKTAIL_DB_API_KEY=your_key
```

If the property is missing, debug builds fall back to the public test key `1`.

## Credits
This product uses TheCocktailDB API.

## License
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF THE LICENSE, either express or implied. See the License for the specific language governing permissions and limitations under the License.

Copyright 2016 Nikhil Bhaskar
