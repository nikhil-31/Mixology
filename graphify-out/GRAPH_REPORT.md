# Graph Report - Mixology  (2026-08-17)

## Corpus Check
- 80 files · ~41,231 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 623 nodes · 1029 edges · 34 communities (30 shown, 4 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 138 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f81f0c47`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Drink
- DrinkDao
- Measures
- MixologyTheme
- HotViewModel
- DrinkWidgetProvider.java
- Cocktail
- MixologyApp
- DrinkDetailsViewModel
- SearchViewModel
- RandomixerViewModel
- DrinkFilterTest
- IngredientMeasure
- MviAndroidViewModel
- ActivityMainEspressoTest
- IntentExtrasTest
- Overlay
- MainViewModel
- ThemeMode
- FilterKind
- DrinkRepository
- fetch_random_drink.py
- Mixology
- DrinkFilter
- CocktailService
- fetch_randomixer_csv.py
- gradlew
- custom.md

## God Nodes (most connected - your core abstractions)
1. `Drink` - 98 edges
2. `MixologyTheme()` - 30 edges
3. `Cocktail` - 26 edges
4. `MixologyApp()` - 19 edges
5. `DrinkFilter` - 17 edges
6. `DrinkDao` - 15 edges
7. `DrinkRepository` - 15 edges
8. `MviAndroidViewModel` - 15 edges
9. `WidgetDataProvider` - 14 edges
10. `CocktailService` - 13 edges

## Surprising Connections (you probably didn't know these)
- `hasUsableThumb()` --references--> `Drink`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/repository/DrinkRepository.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/Drink.java
- `DrinkCard()` --calls--> `DrinkImage()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkCard.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkImage.kt
- `DrinkCard()` --calls--> `FavoriteButton()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkCard.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/FavoriteButton.kt
- `HotCategoryRow()` --calls--> `DrinkCard()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/hot/HotScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkCard.kt
- `SearchScreen()` --calls--> `DrinkCard()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/search/SearchScreen.kt → app/src/main/java/com/capstone/nik/mixology/ui/components/DrinkCard.kt

## Import Cycles
- None detected.

## Communities (34 total, 4 thin omitted)

### Community 1 - "DrinkDao"
Cohesion: 0.07
Nodes (24): DrinkDao, Flow, DrinkEntity, DrinkFilterCrossRef, create(), Callback, importLegacySavedDrinks(), Context (+16 more)

### Community 2 - "Measures"
Cohesion: 0.21
Nodes (5): Creator, Override, Parcel, Measures, Parcelable

### Community 3 - "MixologyTheme"
Cohesion: 0.15
Nodes (10): RandomixerUiState, RandomixerScreen(), SearchUiState, SnackbarHostState, SearchRoute(), SearchScreen(), MixologyTheme(), RandomixerScreenTest (+2 more)

### Community 4 - "HotViewModel"
Cohesion: 0.14
Nodes (16): HotCategory, HotEffect, HotIntent, HotUiState, Load, OpenDrink, OpenFilter, SeeAll (+8 more)

### Community 5 - "DrinkWidgetProvider.java"
Cohesion: 0.18
Nodes (10): ActivityMain, Intent, DrinkWidgetProvider, Context, Intent, Override, AppCompatActivity, AppWidgetManager (+2 more)

### Community 6 - "Cocktail"
Cohesion: 0.09
Nodes (16): Cocktail, Creator, Override, Parcel, drinkExtra(), DrinkIntents, Intent, putDrinkExtra() (+8 more)

### Community 7 - "MixologyApp"
Cohesion: 0.08
Nodes (22): detailsRoute(), DrawerDestination, DrawerNavItem, DrawerSection, encodeRouteArg(), Filter, gridRoute(), Hot (+14 more)

### Community 8 - "DrinkDetailsViewModel"
Cohesion: 0.10
Nodes (19): Back, DrinkDetailsEffect, DrinkDetailsIntent, DrinkDetailsUiState, Load, NavigateBack, Share, ShareRecipe (+11 more)

### Community 9 - "SearchViewModel"
Cohesion: 0.15
Nodes (11): Back, NavigateBack, OpenDrink, Search, SearchEffect, SearchIntent, SearchResultItem, ShowMessageRes (+3 more)

### Community 10 - "RandomixerViewModel"
Cohesion: 0.18
Nodes (10): RandomixerEffect, RandomixerIntent, Refresh, ShowMessageRes, SwipeDiscard, SwipeSave, SnackbarHostState, RandomixerRoute() (+2 more)

### Community 12 - "IngredientMeasure"
Cohesion: 0.07
Nodes (29): CircularDrinkImage(), DrinkImage(), IngredientImage(), Modifier, DrinkHeroImage(), DrinkRecipeBody(), InstructionBlock(), instructionSteps() (+21 more)

### Community 13 - "MviAndroidViewModel"
Cohesion: 0.24
Nodes (11): AndroidViewModel, CollectMviEffects(), Flow, MviAndroidViewModel, MviStore, MviViewModel, E, I (+3 more)

### Community 20 - "Overlay"
Cohesion: 0.18
Nodes (9): Activity, Animator, Overlay, AnimatorListenerAdapter, SaveConfetti, Spark, Canvas, MotionEvent (+1 more)

### Community 21 - "MainViewModel"
Cohesion: 0.11
Nodes (15): CloseDrawer, DismissMenu, DrinkSelected, MainEffect, MainIntent, MainUiState, Navigate, OpenDetails (+7 more)

### Community 22 - "ThemeMode"
Cohesion: 0.18
Nodes (12): SettingsRoute(), SettingsScreen(), fromStorage(), Context, rememberThemeMode(), ThemeMode, DARK, LIGHT (+4 more)

### Community 23 - "FilterKind"
Cohesion: 0.40
Nodes (5): FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT

### Community 24 - "DrinkRepository"
Cohesion: 0.19
Nodes (3): DrinkRepository, hasUsableThumb(), Flow

### Community 25 - "fetch_random_drink.py"
Cohesion: 0.26
Nodes (16): datetime, blank_to_none(), connect(), drink_row(), ensure_schema(), fetch_random_drink(), ingredient_image_url(), ingredient_rows() (+8 more)

### Community 27 - "Mixology"
Cohesion: 0.25
Nodes (7): Credits, License, Mixology, Overview, Play Store, Setup, Stack

### Community 28 - "DrinkFilter"
Cohesion: 0.07
Nodes (28): DrinkListItem, from(), DrinkFilter, ALCOHOLIC, COCKTAIL, COCKTAIL_GLASS, GIN, HIGHBALL_GLASS (+20 more)

### Community 29 - "CocktailService"
Cohesion: 0.33
Nodes (4): CocktailService, Cocktails, Call, GET

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
- **66 isolated node(s):** `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL`, `ORDINARY_DRINK`, `GIN` (+61 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `MixologyTheme`, `DrinkDetailsViewModel`, `RandomixerViewModel`, `DrinkFilterTest`, `IngredientMeasure`, `DrinkRepository`, `CocktailService`?**
  _High betweenness centrality (0.280) - this node is a cross-community bridge._
- **Why does `Cocktail` connect `Cocktail` to `DrinkDao`, `Measures`, `MixologyApp`, `DrinkDetailsViewModel`, `SearchViewModel`, `DrinkRepository`, `DrinkFilter`, `CocktailService`?**
  _High betweenness centrality (0.200) - this node is a cross-community bridge._
- **Why does `MixologyApp()` connect `MixologyApp` to `MixologyTheme`, `HotViewModel`, `DrinkWidgetProvider.java`, `Cocktail`, `DrinkDetailsViewModel`, `RandomixerViewModel`, `MviAndroidViewModel`, `MainViewModel`, `ThemeMode`, `DrinkFilter`?**
  _High betweenness centrality (0.186) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `Drink` (e.g. with `.hasUsableThumb_rejectsMissingAndLiteralNull()` and `.content_numberedInstructions_showsSteps()`) actually correct?**
  _`Drink` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Are the 29 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `rememberThemeMode()`) actually correct?**
  _`MixologyTheme()` has 29 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `MixologyApp()` (e.g. with `.onCreate()` and `DrinkDetailsRoute()`) actually correct?**
  _`MixologyApp()` has 14 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL` to the rest of the system?**
  _66 weakly-connected nodes found - possible documentation gaps or missing edges._