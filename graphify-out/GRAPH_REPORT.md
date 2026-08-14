# Graph Report - Mixology  (2026-08-14)

## Corpus Check
- 78 files · ~40,086 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 558 nodes · 866 edges · 30 communities (24 shown, 6 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 105 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `01815146`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Drink
- DrinkDao
- DrinkRepository
- RandomixerViewModel
- MixologyTheme
- LoginActivityEspressoTest
- Cocktail
- DrinkWidgetProvider.java
- DrinkDetailsViewModel
- SearchScreen
- IngredientMeasure
- DrinkFilterTest
- FilterKind
- MviAndroidViewModel
- ActivityDetailsEspressoTest
- IntentExtrasTest
- ActivityDetailsRobolectricTest
- MainViewModel
- Mixology
- DrinkFilter
- CocktailService
- Measures
- gradlew
- custom.md

## God Nodes (most connected - your core abstractions)
1. `Drink` - 96 edges
2. `MixologyTheme()` - 27 edges
3. `Cocktail` - 26 edges
4. `DrinkRepository` - 20 edges
5. `DrinkFilter` - 18 edges
6. `DrinkDao` - 16 edges
7. `WidgetDataProvider` - 14 edges
8. `MviAndroidViewModel` - 14 edges
9. `CocktailService` - 13 edges
10. `MainViewModel` - 13 edges

## Surprising Connections (you probably didn't know these)
- `hasUsableThumb()` --references--> `Drink`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/repository/DrinkRepository.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/Drink.java
- `DrinkCard()` --calls--> `DrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/grid/DrinkGridScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `SearchRow()` --calls--> `CircularDrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/search/SearchScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `DrinkRecipeBody()` --calls--> `FavoriteButton()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkRecipeBody.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/FavoriteButton.kt
- `DrinkDetailsContent()` --calls--> `DrinkRecipeBody()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/details/DrinkDetailsScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkRecipeBody.kt

## Import Cycles
- None detected.

## Communities (30 total, 6 thin omitted)

### Community 1 - "DrinkDao"
Cohesion: 0.09
Nodes (19): DrinkDao, Flow, DrinkEntity, from(), DrinkFilterCrossRef, create(), Callback, importLegacySavedDrinks() (+11 more)

### Community 2 - "DrinkRepository"
Cohesion: 0.10
Nodes (10): ApplicationComponent, Singleton, Override, MyApplication, DrinkRepository, Flow, ContentProviderHelperMethods, Context (+2 more)

### Community 3 - "RandomixerViewModel"
Cohesion: 0.21
Nodes (8): RandomixerEffect, RandomixerIntent, Refresh, ShowMessageRes, SwipeDiscard, SwipeSave, Job, RandomixerViewModel

### Community 4 - "MixologyTheme"
Cohesion: 0.07
Nodes (19): ActivityLogin, AppCompatActivity, Bundle, ActivityPasswordChange, AppCompatActivity, Bundle, ActivitySignUp, AppCompatActivity (+11 more)

### Community 6 - "Cocktail"
Cohesion: 0.09
Nodes (13): Cocktail, Creator, Override, Parcel, getCocktailExtra(), DrinkWidgetService, Context, Intent (+5 more)

### Community 7 - "DrinkWidgetProvider.java"
Cohesion: 0.15
Nodes (12): ActivityDetails, AppCompatActivity, Bundle, ActivityMain, AppCompatActivity, Bundle, DrinkWidgetProvider, Context (+4 more)

### Community 8 - "DrinkDetailsViewModel"
Cohesion: 0.11
Nodes (17): Back, DrinkDetailsEffect, DrinkDetailsIntent, DrinkDetailsUiState, Load, NavigateBack, Share, ShareRecipe (+9 more)

### Community 9 - "SearchScreen"
Cohesion: 0.09
Nodes (21): ActivitySearch, AppCompatActivity, Bundle, FavoriteButton(), Modifier, Back, NavigateBack, OpenDrink (+13 more)

### Community 10 - "IngredientMeasure"
Cohesion: 0.11
Nodes (20): CircularDrinkImage(), DrinkImage(), IngredientImage(), Modifier, DrinkHeroImage(), DrinkRecipeBody(), Modifier, IngredientRow() (+12 more)

### Community 12 - "FilterKind"
Cohesion: 0.29
Nodes (6): FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT, hasUsableThumb()

### Community 13 - "MviAndroidViewModel"
Cohesion: 0.08
Nodes (24): AndroidViewModel, DrawerDestination, DrawerNavItem, DrawerSection, Filter, gridRoute(), Randomixer, MixologyApp() (+16 more)

### Community 21 - "MainViewModel"
Cohesion: 0.10
Nodes (18): CloseDrawer, DismissMenu, DrinkSelected, MainEffect, MainIntent, MainUiState, Navigate, OpenDetails (+10 more)

### Community 27 - "Mixology"
Cohesion: 0.14
Nodes (13): Credits, Details Screen, Libraries used, License, Main Screen, Mixology, Overview, Phone (+5 more)

### Community 28 - "DrinkFilter"
Cohesion: 0.08
Nodes (27): DrinkListItem, DrinkFilter, ALCOHOLIC, COCKTAIL, COCKTAIL_GLASS, GIN, HIGHBALL_GLASS, NON_ALCOHOLIC (+19 more)

### Community 29 - "CocktailService"
Cohesion: 0.33
Nodes (4): CocktailService, Cocktails, Call, GET

### Community 30 - "Measures"
Cohesion: 0.21
Nodes (5): Creator, Override, Parcel, Measures, Parcelable

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

## Knowledge Gaps
- **61 isolated node(s):** `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL`, `ORDINARY_DRINK`, `GIN` (+56 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `DrinkRepository`, `RandomixerViewModel`, `MixologyTheme`, `DrinkDetailsViewModel`, `SearchScreen`, `IngredientMeasure`, `DrinkFilterTest`, `FilterKind`, `CocktailService`?**
  _High betweenness centrality (0.335) - this node is a cross-community bridge._
- **Why does `Cocktail` connect `Cocktail` to `DrinkDao`, `DrinkRepository`, `DrinkDetailsViewModel`, `SearchScreen`, `CocktailService`, `Measures`?**
  _High betweenness centrality (0.210) - this node is a cross-community bridge._
- **Why does `MixologyTheme()` connect `MixologyTheme` to `DrinkWidgetProvider.java`, `DrinkDetailsViewModel`, `SearchScreen`, `MviAndroidViewModel`, `DrinkFilter`?**
  _High betweenness centrality (0.183) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `Drink` (e.g. with `.hasUsableThumb_rejectsMissingAndLiteralNull()` and `.content_showsRecipe_andFavoriteClick()`) actually correct?**
  _`Drink` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Are the 26 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `.onCreate()`) actually correct?**
  _`MixologyTheme()` has 26 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL` to the rest of the system?**
  _61 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Drink` be split into smaller, more focused modules?**
  _Cohesion score 0.024390243902439025 - nodes in this community are weakly interconnected._