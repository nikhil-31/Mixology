# Graph Report - Mixology  (2026-08-14)

## Corpus Check
- 88 files · ~42,677 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 620 nodes · 992 edges · 32 communities (26 shown, 6 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 139 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `7b8e9cb1`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Drink
- DrinkDao
- DrinkRepository
- .onCreate
- DrinkListItem
- LoginActivityEspressoTest
- Cocktail
- MviAndroidViewModel
- DrinkDetailsViewModel
- MixologyTheme
- RandomixerViewModel
- DrinkFilterTest
- IngredientMeasure
- DrawerDestination
- ActivityDetailsEspressoTest
- IntentExtrasTest
- ActivityDetailsRobolectricTest
- MainViewModel
- ThemeMode
- DrinkWidgetProvider.java
- .onCreate
- Mixology
- DrinkFilter
- CocktailService
- gradlew
- custom.md

## God Nodes (most connected - your core abstractions)
1. `Drink` - 97 edges
2. `MixologyTheme()` - 38 edges
3. `Cocktail` - 26 edges
4. `DrinkRepository` - 20 edges
5. `DrinkFilter` - 18 edges
6. `DrinkDao` - 16 edges
7. `MviAndroidViewModel` - 15 edges
8. `WidgetDataProvider` - 14 edges
9. `CocktailService` - 13 edges
10. `DrinkListItem` - 13 edges

## Surprising Connections (you probably didn't know these)
- `hasUsableThumb()` --references--> `Drink`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/repository/DrinkRepository.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/Drink.java
- `DrinkHeroImage()` --calls--> `DrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkRecipeBody.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `DrinkCard()` --calls--> `DrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/grid/DrinkGridScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `HotDrinkCard()` --calls--> `DrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/hot/HotScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `SearchRow()` --calls--> `CircularDrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/search/SearchScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt

## Import Cycles
- None detected.

## Communities (32 total, 6 thin omitted)

### Community 1 - "DrinkDao"
Cohesion: 0.09
Nodes (19): DrinkDao, Flow, DrinkEntity, from(), DrinkFilterCrossRef, create(), Callback, importLegacySavedDrinks() (+11 more)

### Community 2 - "DrinkRepository"
Cohesion: 0.08
Nodes (16): ApplicationComponent, Singleton, Override, MyApplication, DrinkRepository, FilterKind, ALCOHOL, DRINK_TYPE (+8 more)

### Community 3 - ".onCreate"
Cohesion: 0.20
Nodes (5): ActivityPasswordChange, AppCompatActivity, Bundle, PasswordChangeScreen(), PasswordChangeScreenTest

### Community 4 - "DrinkListItem"
Cohesion: 0.08
Nodes (25): DrinkListItem, FavoriteButton(), Modifier, DrinkCard(), DrinkGridRoute(), DrinkGridScreen(), SnackbarHostState, HotCategory (+17 more)

### Community 6 - "Cocktail"
Cohesion: 0.07
Nodes (18): Cocktail, Creator, Override, Parcel, Creator, Override, Parcel, Measures (+10 more)

### Community 7 - "MviAndroidViewModel"
Cohesion: 0.09
Nodes (19): AndroidViewModel, gridRoute(), MixologyApp(), MixologyDrawer(), CollectMviEffects(), Flow, MviAndroidViewModel, MviStore (+11 more)

### Community 8 - "DrinkDetailsViewModel"
Cohesion: 0.09
Nodes (20): DrinkHeroImage(), DrinkRecipeBody(), Modifier, Back, DrinkDetailsEffect, DrinkDetailsIntent, DrinkDetailsUiState, Load (+12 more)

### Community 9 - "MixologyTheme"
Cohesion: 0.06
Nodes (29): ActivityLogin, AppCompatActivity, Bundle, ActivitySearch, AppCompatActivity, Bundle, LoginScreen(), RandomixerUiState (+21 more)

### Community 10 - "RandomixerViewModel"
Cohesion: 0.21
Nodes (8): RandomixerEffect, RandomixerIntent, Refresh, ShowMessageRes, SwipeDiscard, SwipeSave, Job, RandomixerViewModel

### Community 12 - "IngredientMeasure"
Cohesion: 0.13
Nodes (19): CircularDrinkImage(), DrinkImage(), IngredientImage(), Modifier, IngredientRow(), Modifier, IngredientMeasure, ingredientMeasures() (+11 more)

### Community 13 - "DrawerDestination"
Cohesion: 0.14
Nodes (11): DrawerDestination, DrawerNavItem, DrawerSection, Filter, Hot, Randomixer, Settings, BottomNavItem (+3 more)

### Community 21 - "MainViewModel"
Cohesion: 0.11
Nodes (16): CloseDrawer, DismissMenu, DrinkSelected, MainEffect, MainIntent, MainUiState, Navigate, OpenDetails (+8 more)

### Community 22 - "ThemeMode"
Cohesion: 0.18
Nodes (12): SettingsRoute(), SettingsScreen(), fromStorage(), Context, rememberThemeMode(), ThemeMode, DARK, LIGHT (+4 more)

### Community 23 - "DrinkWidgetProvider.java"
Cohesion: 0.15
Nodes (12): ActivityDetails, AppCompatActivity, Bundle, ActivityMain, AppCompatActivity, Bundle, DrinkWidgetProvider, Context (+4 more)

### Community 24 - ".onCreate"
Cohesion: 0.20
Nodes (5): ActivitySignUp, AppCompatActivity, Bundle, SignUpScreen(), SignUpScreenTest

### Community 27 - "Mixology"
Cohesion: 0.14
Nodes (13): Credits, Details Screen, Libraries used, License, Main Screen, Mixology, Overview, Phone (+5 more)

### Community 28 - "DrinkFilter"
Cohesion: 0.10
Nodes (21): DrinkFilter, ALCOHOLIC, COCKTAIL, COCKTAIL_GLASS, GIN, HIGHBALL_GLASS, NON_ALCOHOLIC, ORDINARY_DRINK (+13 more)

### Community 29 - "CocktailService"
Cohesion: 0.33
Nodes (4): CocktailService, Cocktails, Call, GET

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

## Knowledge Gaps
- **69 isolated node(s):** `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL`, `ORDINARY_DRINK`, `GIN` (+64 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `DrinkRepository`, `DrinkDetailsViewModel`, `MixologyTheme`, `RandomixerViewModel`, `DrinkFilterTest`, `IngredientMeasure`, `CocktailService`?**
  _High betweenness centrality (0.311) - this node is a cross-community bridge._
- **Why does `MixologyTheme()` connect `MixologyTheme` to `.onCreate`, `DrinkListItem`, `MviAndroidViewModel`, `DrinkDetailsViewModel`, `DrawerDestination`, `ThemeMode`, `DrinkWidgetProvider.java`, `.onCreate`?**
  _High betweenness centrality (0.237) - this node is a cross-community bridge._
- **Why does `Cocktail` connect `Cocktail` to `DrinkDao`, `DrinkRepository`, `DrinkListItem`, `DrinkDetailsViewModel`, `MixologyTheme`, `CocktailService`?**
  _High betweenness centrality (0.191) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `Drink` (e.g. with `.hasUsableThumb_rejectsMissingAndLiteralNull()` and `.content_showsRecipe_andFavoriteClick()`) actually correct?**
  _`Drink` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Are the 37 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `.onCreate()`) actually correct?**
  _`MixologyTheme()` has 37 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL` to the rest of the system?**
  _69 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Drink` be split into smaller, more focused modules?**
  _Cohesion score 0.024390243902439025 - nodes in this community are weakly interconnected._