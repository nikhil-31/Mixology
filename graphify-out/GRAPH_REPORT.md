# Graph Report - Mixology  (2026-08-17)

## Corpus Check
- 81 files · ~40,952 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 524 nodes · 897 edges · 31 communities (29 shown, 2 thin omitted)
- Extraction: 82% EXTRACTED · 18% INFERRED · 0% AMBIGUOUS · INFERRED: 158 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1779676e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DrinkDetailsContent
- DrinkDao
- Measures
- MixologyTheme
- HotViewModel
- DrinkWidgetProvider.java
- WidgetDataProvider
- MixologyApp
- .sendEffect
- MyApplication
- RandomixerViewModel
- IngredientMeasure
- DrinkRecipeBody
- CocktailURLs.java
- Overlay
- MainViewModel
- ThemeMode
- Drink
- fetch_random_drink.py
- Mixology
- DrinkFilter
- CocktailService
- fetch_randomixer_csv.py
- gradlew
- custom.md

## God Nodes (most connected - your core abstractions)
1. `Drink` - 45 edges
2. `MixologyTheme()` - 30 edges
3. `MixologyApp()` - 19 edges
4. `DrinkDao` - 17 edges
5. `DrinkFilter` - 17 edges
6. `MviViewModel` - 16 edges
7. `RandomixerViewModel` - 15 edges
8. `WidgetDataProvider` - 14 edges
9. `DrinkRepository` - 14 edges
10. `IngredientMeasure` - 14 edges

## Surprising Connections (you probably didn't know these)
- `DrinkGridScreen()` --calls--> `DrinkCard()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/grid/DrinkGridScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkCard.kt
- `HotCategoryRow()` --calls--> `DrinkCard()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/hot/HotScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkCard.kt
- `SearchScreen()` --calls--> `DrinkCard()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/search/SearchScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkCard.kt
- `DrinkSwipePhoto()` --calls--> `DrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/randomixer/RandomixerScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `IngredientRow()` --calls--> `IngredientImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/components/IngredientRow.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt

## Import Cycles
- None detected.

## Communities (31 total, 2 thin omitted)

### Community 0 - "DrinkDetailsContent"
Cohesion: 0.25
Nodes (8): DrinkDetailsUiState, DrinkDetailsContent(), DrinkDetailsRoute(), DrinkDetailsScaffold(), HeroActionButton(), SnackbarHostState, DrinkDetailsScreenTest, ImageVector

### Community 1 - "DrinkDao"
Cohesion: 0.11
Nodes (14): DrinkDao, Flow, DrinkEntity, toEntity(), DrinkFilterCrossRef, create(), Callback, importLegacySavedDrinks() (+6 more)

### Community 2 - "Measures"
Cohesion: 0.21
Nodes (5): Override, Measures, Creator, Parcel, Parcelable

### Community 3 - "MixologyTheme"
Cohesion: 0.15
Nodes (10): RandomixerUiState, RandomixerScreen(), SearchUiState, SnackbarHostState, SearchRoute(), SearchScreen(), MixologyTheme(), RandomixerScreenTest (+2 more)

### Community 4 - "HotViewModel"
Cohesion: 0.14
Nodes (16): HotCategory, HotEffect, HotIntent, HotUiState, Load, OpenDrink, OpenFilter, SeeAll (+8 more)

### Community 5 - "DrinkWidgetProvider.java"
Cohesion: 0.18
Nodes (10): ActivityMain, Intent, DrinkWidgetProvider, Context, Intent, Override, AppCompatActivity, AppWidgetManager (+2 more)

### Community 6 - "WidgetDataProvider"
Cohesion: 0.16
Nodes (9): WidgetEntryPoint, DrinkWidgetService, Context, Intent, Override, WidgetDataProvider, RemoteViews, RemoteViewsFactory (+1 more)

### Community 7 - "MixologyApp"
Cohesion: 0.07
Nodes (24): detailsRoute(), DrawerDestination, DrawerNavItem, DrawerSection, encodeRouteArg(), Filter, gridRoute(), Hot (+16 more)

### Community 8 - ".sendEffect"
Cohesion: 0.07
Nodes (30): Back, DrinkDetailsEffect, DrinkDetailsIntent, Load, NavigateBack, Share, ShareRecipe, ShowMessageRes (+22 more)

### Community 10 - "RandomixerViewModel"
Cohesion: 0.20
Nodes (8): RandomixerEffect, RandomixerIntent, Refresh, ShowMessageRes, SwipeDiscard, SwipeSave, Job, RandomixerViewModel

### Community 11 - "IngredientMeasure"
Cohesion: 0.09
Nodes (15): IngredientListConverter, CocktailDbDrink, IngredientRow(), Modifier, IngredientMeasure, ActionCircleButton(), DrinkSwipeDetails(), DrinkSwipePhoto() (+7 more)

### Community 12 - "DrinkRecipeBody"
Cohesion: 0.10
Nodes (18): DrinkCard(), Modifier, CircularDrinkImage(), DrinkImage(), IngredientImage(), Modifier, DrinkHeroImage(), DrinkRecipeBody() (+10 more)

### Community 20 - "Overlay"
Cohesion: 0.18
Nodes (9): Activity, Animator, Overlay, AnimatorListenerAdapter, SaveConfetti, Spark, Canvas, MotionEvent (+1 more)

### Community 21 - "MainViewModel"
Cohesion: 0.11
Nodes (15): CloseDrawer, DismissMenu, DrinkSelected, MainEffect, MainIntent, MainUiState, Navigate, OpenDetails (+7 more)

### Community 22 - "ThemeMode"
Cohesion: 0.18
Nodes (12): SettingsRoute(), SettingsScreen(), fromStorage(), Context, rememberThemeMode(), ThemeMode, DARK, LIGHT (+4 more)

### Community 24 - "Drink"
Cohesion: 0.07
Nodes (15): ActivityMainEspressoTest, Drink, DrinkRepository, FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT (+7 more)

### Community 25 - "fetch_random_drink.py"
Cohesion: 0.26
Nodes (16): datetime, blank_to_none(), connect(), drink_row(), ensure_schema(), fetch_random_drink(), ingredient_image_url(), ingredient_rows() (+8 more)

### Community 27 - "Mixology"
Cohesion: 0.25
Nodes (7): Credits, License, Mixology, Overview, Play Store, Setup, Stack

### Community 28 - "DrinkFilter"
Cohesion: 0.08
Nodes (24): DrinkFilter, ALCOHOLIC, COCKTAIL, COCKTAIL_GLASS, GIN, HIGHBALL_GLASS, NON_ALCOHOLIC, ORDINARY_DRINK (+16 more)

### Community 29 - "CocktailService"
Cohesion: 0.18
Nodes (5): Context, NetworkModule, CocktailService, CocktailDbResponse, OkHttpClient

### Community 30 - "fetch_randomixer_csv.py"
Cohesion: 0.28
Nodes (12): Exception, Path, cell(), drink_row(), fetch_random_drink(), FetchError, load_existing_ids(), main() (+4 more)

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

## Knowledge Gaps
- **67 isolated node(s):** `CocktailURLs`, `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL`, `ORDINARY_DRINK` (+62 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `DrinkDetailsContent`, `MixologyTheme`, `HotViewModel`, `WidgetDataProvider`, `MixologyApp`, `.sendEffect`, `RandomixerViewModel`, `IngredientMeasure`, `DrinkRecipeBody`, `MainViewModel`, `DrinkFilter`?**
  _High betweenness centrality (0.389) - this node is a cross-community bridge._
- **Why does `MixologyApp()` connect `MixologyApp` to `DrinkDetailsContent`, `MixologyTheme`, `HotViewModel`, `DrinkWidgetProvider.java`, `.sendEffect`, `MainViewModel`, `ThemeMode`, `Drink`, `DrinkFilter`?**
  _High betweenness centrality (0.106) - this node is a cross-community bridge._
- **Why does `MixologyTheme()` connect `MixologyTheme` to `DrinkDetailsContent`, `HotViewModel`, `DrinkWidgetProvider.java`, `MixologyApp`, `ThemeMode`, `DrinkFilter`?**
  _High betweenness centrality (0.092) - this node is a cross-community bridge._
- **Are the 15 inferred relationships involving `Drink` (e.g. with `.drinkExtras_showsDrinkName()` and `.content_numberedInstructions_showsSteps()`) actually correct?**
  _`Drink` has 15 INFERRED edges - model-reasoned connections that need verification._
- **Are the 29 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `rememberThemeMode()`) actually correct?**
  _`MixologyTheme()` has 29 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `MixologyApp()` (e.g. with `.onCreate()` and `DrinkDetailsRoute()`) actually correct?**
  _`MixologyApp()` has 14 INFERRED edges - model-reasoned connections that need verification._
- **What connects `CocktailURLs`, `ALCOHOLIC`, `NON_ALCOHOLIC` to the rest of the system?**
  _67 weakly-connected nodes found - possible documentation gaps or missing edges._