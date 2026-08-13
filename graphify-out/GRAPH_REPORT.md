# Graph Report - Mixology  (2026-08-14)

## Corpus Check
- 69 files · ~38,319 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 470 nodes · 703 edges · 29 communities (23 shown, 6 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 87 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `dda4ab8f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Drink
- DrinkDao
- DrinkRepository
- DrinkDetailsViewModel
- .onCreate
- LoginActivityEspressoTest
- Cocktail
- DrinkWidgetProvider.java
- .onCreate
- MixologyTheme
- RandomixerViewModel
- DrinkFilterTest
- FilterKind
- gridRoute
- ActivityDetailsEspressoTest
- IntentExtrasTest
- ActivityDetailsRobolectricTest
- Mixology
- DrinkFilter
- CocktailService
- Measures
- gradlew
- custom.md

## God Nodes (most connected - your core abstractions)
1. `Drink` - 92 edges
2. `Cocktail` - 26 edges
3. `MixologyTheme()` - 24 edges
4. `DrinkRepository` - 20 edges
5. `DrinkFilter` - 19 edges
6. `DrinkDao` - 16 edges
7. `WidgetDataProvider` - 14 edges
8. `CocktailService` - 13 edges
9. `Cocktails` - 12 edges
10. `DrinkEntity` - 12 edges

## Surprising Connections (you probably didn't know these)
- `hasUsableThumb()` --references--> `Drink`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/repository/DrinkRepository.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/Drink.java
- `DrinkHeroImage()` --calls--> `DrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkRecipeBody.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `SearchRow()` --calls--> `CircularDrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/search/SearchScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `IngredientRow()` --calls--> `IngredientImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/components/IngredientRow.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `DrinkRecipeBody()` --calls--> `FavoriteButton()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkRecipeBody.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/FavoriteButton.kt

## Import Cycles
- None detected.

## Communities (29 total, 6 thin omitted)

### Community 1 - "DrinkDao"
Cohesion: 0.09
Nodes (18): DrinkDao, Flow, DrinkEntity, DrinkFilterCrossRef, create(), Callback, importLegacySavedDrinks(), Context (+10 more)

### Community 2 - "DrinkRepository"
Cohesion: 0.10
Nodes (10): ApplicationComponent, Singleton, Override, MyApplication, DrinkRepository, Flow, ContentProviderHelperMethods, Context (+2 more)

### Community 3 - "DrinkDetailsViewModel"
Cohesion: 0.07
Nodes (23): DrinkHeroImage(), DrinkRecipeBody(), Modifier, IngredientRow(), Modifier, DrinkDetailsContent(), DrinkDetailsRoute(), DrinkDetailsScaffold() (+15 more)

### Community 4 - ".onCreate"
Cohesion: 0.20
Nodes (5): ActivitySignUp, AppCompatActivity, Bundle, SignUpScreen(), SignUpScreenTest

### Community 6 - "Cocktail"
Cohesion: 0.10
Nodes (13): Cocktail, Creator, Override, Parcel, getCocktailExtra(), DrinkWidgetService, Context, Intent (+5 more)

### Community 7 - "DrinkWidgetProvider.java"
Cohesion: 0.15
Nodes (12): ActivityDetails, AppCompatActivity, Bundle, ActivityMain, AppCompatActivity, Bundle, DrinkWidgetProvider, Context (+4 more)

### Community 8 - ".onCreate"
Cohesion: 0.20
Nodes (5): ActivityPasswordChange, AppCompatActivity, Bundle, PasswordChangeScreen(), PasswordChangeScreenTest

### Community 9 - "MixologyTheme"
Cohesion: 0.08
Nodes (21): ActivityLogin, AppCompatActivity, Bundle, ActivitySearch, AppCompatActivity, Bundle, LoginScreen(), FavoriteButton() (+13 more)

### Community 10 - "RandomixerViewModel"
Cohesion: 0.29
Nodes (5): AndroidViewModel, Job, StateFlow, RandomixerUiState, RandomixerViewModel

### Community 12 - "FilterKind"
Cohesion: 0.29
Nodes (6): FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT, hasUsableThumb()

### Community 13 - "gridRoute"
Cohesion: 0.12
Nodes (9): DrawerDestination, DrawerNavItem, DrawerSection, Filter, gridRoute(), Randomixer, MixologyDrawer(), DrawerDestinationTest (+1 more)

### Community 27 - "Mixology"
Cohesion: 0.14
Nodes (13): Credits, Details Screen, Libraries used, License, Main Screen, Mixology, Overview, Phone (+5 more)

### Community 28 - "DrinkFilter"
Cohesion: 0.07
Nodes (29): DrinkListItem, from(), DrinkFilter, ALCOHOLIC, COCKTAIL, COCKTAIL_GLASS, GIN, HIGHBALL_GLASS (+21 more)

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
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `DrinkRepository`, `DrinkDetailsViewModel`, `MixologyTheme`, `DrinkFilterTest`, `FilterKind`, `CocktailService`?**
  _High betweenness centrality (0.354) - this node is a cross-community bridge._
- **Why does `Cocktail` connect `Cocktail` to `DrinkDao`, `DrinkRepository`, `DrinkDetailsViewModel`, `MixologyTheme`, `DrinkFilter`, `CocktailService`, `Measures`?**
  _High betweenness centrality (0.222) - this node is a cross-community bridge._
- **Why does `MixologyTheme()` connect `MixologyTheme` to `DrinkDetailsViewModel`, `.onCreate`, `DrinkWidgetProvider.java`, `.onCreate`, `gridRoute`, `DrinkFilter`?**
  _High betweenness centrality (0.216) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `Drink` (e.g. with `.hasUsableThumb_rejectsMissingAndLiteralNull()` and `.content_showsRecipe_andFavoriteClick()`) actually correct?**
  _`Drink` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Are the 23 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `.onCreate()`) actually correct?**
  _`MixologyTheme()` has 23 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL` to the rest of the system?**
  _30 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Drink` be split into smaller, more focused modules?**
  _Cohesion score 0.024390243902439025 - nodes in this community are weakly interconnected._