# Graph Report - Mixology  (2026-08-14)

## Corpus Check
- 56 files · ~36,539 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 414 nodes · 609 edges · 26 communities (24 shown, 2 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 36 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c8c8ccac`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Drink
- ApplicationModule.java
- ApplicationComponent
- DrinkDetailsViewModel
- Cocktail
- DrinkDao
- WidgetDataProvider
- DrinkWidgetProvider.java
- MixologyTheme
- SearchViewModel
- RandomixerViewModel
- DrinkFilterTest
- FilterKind
- DrawerDestination.kt
- Mixology
- DrinkFilter
- CocktailService
- Measures
- gradlew
- custom.md

## God Nodes (most connected - your core abstractions)
1. `Drink` - 89 edges
2. `Cocktail` - 26 edges
3. `DrinkRepository` - 20 edges
4. `DrinkFilter` - 19 edges
5. `DrinkDao` - 16 edges
6. `WidgetDataProvider` - 14 edges
7. `CocktailService` - 13 edges
8. `Cocktails` - 12 edges
9. `DrinkEntity` - 12 edges
10. `DrinkGridViewModel` - 12 edges

## Surprising Connections (you probably didn't know these)
- `hasUsableThumb()` --references--> `Drink`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/repository/DrinkRepository.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/Drink.java
- `importLegacySavedDrinks()` --calls--> `DrinkEntity`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/data/MixologyDatabase.kt → app/src/main/java/com/capstone/nik/mixology/data/DrinkEntity.kt
- `DrinkCard()` --calls--> `DrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/grid/DrinkGridScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `SearchRow()` --calls--> `CircularDrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/search/SearchScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `DrinkRecipeBody()` --calls--> `FavoriteButton()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkRecipeBody.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/FavoriteButton.kt

## Import Cycles
- None detected.

## Communities (26 total, 2 thin omitted)

### Community 1 - "ApplicationModule.java"
Cohesion: 0.16
Nodes (14): create(), Callback, importLegacySavedDrinks(), Context, MixologyDatabase, ApplicationModule, Application, Context (+6 more)

### Community 2 - "ApplicationComponent"
Cohesion: 0.18
Nodes (8): ApplicationComponent, Singleton, Override, MyApplication, ContentProviderHelperMethods, Context, Application, Component

### Community 3 - "DrinkDetailsViewModel"
Cohesion: 0.07
Nodes (28): CircularDrinkImage(), DrinkImage(), IngredientImage(), Modifier, DrinkHeroImage(), DrinkRecipeBody(), Modifier, IngredientRow() (+20 more)

### Community 4 - "Cocktail"
Cohesion: 0.10
Nodes (7): Cocktail, Creator, Override, Parcel, DrinkRepository, Flow, getCocktailExtra()

### Community 5 - "DrinkDao"
Cohesion: 0.19
Nodes (4): DrinkDao, Flow, DrinkEntity, DrinkFilterCrossRef

### Community 6 - "WidgetDataProvider"
Cohesion: 0.17
Nodes (8): DrinkWidgetService, Context, Intent, Override, WidgetDataProvider, RemoteViews, RemoteViewsFactory, RemoteViewsService

### Community 7 - "DrinkWidgetProvider.java"
Cohesion: 0.15
Nodes (12): ActivityDetails, AppCompatActivity, Bundle, ActivityMain, AppCompatActivity, Bundle, DrinkWidgetProvider, Context (+4 more)

### Community 8 - "MixologyTheme"
Cohesion: 0.07
Nodes (16): ActivityLogin, AppCompatActivity, Bundle, ActivityPasswordChange, AppCompatActivity, Bundle, ActivitySearch, AppCompatActivity (+8 more)

### Community 9 - "SearchViewModel"
Cohesion: 0.18
Nodes (11): FavoriteButton(), Modifier, SnackbarHostState, SearchRoute(), SearchRow(), SearchScreen(), AndroidViewModel, StateFlow (+3 more)

### Community 10 - "RandomixerViewModel"
Cohesion: 0.29
Nodes (5): AndroidViewModel, Job, StateFlow, RandomixerUiState, RandomixerViewModel

### Community 12 - "FilterKind"
Cohesion: 0.29
Nodes (6): FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT, hasUsableThumb()

### Community 13 - "DrawerDestination.kt"
Cohesion: 0.29
Nodes (6): DrawerDestination, DrawerNavItem, DrawerSection, Filter, gridRoute(), Randomixer

### Community 27 - "Mixology"
Cohesion: 0.14
Nodes (13): Credits, Details Screen, Libraries used, License, Main Screen, Mixology, Overview, Phone (+5 more)

### Community 28 - "DrinkFilter"
Cohesion: 0.10
Nodes (22): DrinkListItem, from(), DrinkFilter, ALCOHOLIC, COCKTAIL, COCKTAIL_GLASS, GIN, HIGHBALL_GLASS (+14 more)

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
- **30 isolated node(s):** `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL`, `ORDINARY_DRINK`, `GIN` (+25 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Cocktail` connect `Cocktail` to `ApplicationComponent`, `DrinkDetailsViewModel`, `DrinkDao`, `WidgetDataProvider`, `SearchViewModel`, `DrinkFilter`, `CocktailService`, `Measures`?**
  _High betweenness centrality (0.412) - this node is a cross-community bridge._
- **Why does `Drink` connect `Drink` to `FilterKind`, `DrinkFilterTest`, `Cocktail`, `CocktailService`?**
  _High betweenness centrality (0.351) - this node is a cross-community bridge._
- **Why does `DrinkDetailsRoute()` connect `DrinkDetailsViewModel` to `Cocktail`, `DrinkWidgetProvider.java`?**
  _High betweenness centrality (0.224) - this node is a cross-community bridge._
- **What connects `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL` to the rest of the system?**
  _30 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Drink` be split into smaller, more focused modules?**
  _Cohesion score 0.024390243902439025 - nodes in this community are weakly interconnected._
- **Should `DrinkDetailsViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.06794871794871794 - nodes in this community are weakly interconnected._
- **Should `Cocktail` be split into smaller, more focused modules?**
  _Cohesion score 0.10052910052910052 - nodes in this community are weakly interconnected._