# Graph Report - Mixology  (2026-08-17)

## Corpus Check
- 92 files · ~44,467 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 654 nodes · 1051 edges · 35 communities (29 shown, 6 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 141 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ad4966dd`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Drink
- DrinkDao
- Cocktail
- MixologyTheme
- HotViewModel
- LoginActivityEspressoTest
- WidgetDataProvider
- MviAndroidViewModel
- DrinkDetailsViewModel
- SearchViewModel
- RandomixerViewModel
- DrinkFilterTest
- IngredientMeasure
- DrawerDestination
- ActivityDetailsEspressoTest
- IntentExtrasTest
- ActivityDetailsRobolectricTest
- MainViewModel
- ThemeMode
- FilterKind
- Measures
- fetch_random_drink.py
- .onCreate
- Mixology
- DrinkFilter
- CocktailService
- fetch_randomixer_csv.py
- gradlew
- custom.md

## God Nodes (most connected - your core abstractions)
1. `Drink` - 97 edges
2. `MixologyTheme()` - 39 edges
3. `Cocktail` - 26 edges
4. `DrinkRepository` - 20 edges
5. `DrinkFilter` - 18 edges
6. `DrinkDao` - 16 edges
7. `MviAndroidViewModel` - 15 edges
8. `WidgetDataProvider` - 14 edges
9. `MixologyApp()` - 14 edges
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

## Communities (35 total, 6 thin omitted)

### Community 1 - "DrinkDao"
Cohesion: 0.09
Nodes (19): DrinkDao, Flow, DrinkEntity, from(), DrinkFilterCrossRef, create(), Callback, importLegacySavedDrinks() (+11 more)

### Community 2 - "Cocktail"
Cohesion: 0.07
Nodes (15): ApplicationComponent, Singleton, Cocktail, Creator, Override, Parcel, Override, MyApplication (+7 more)

### Community 3 - "MixologyTheme"
Cohesion: 0.06
Nodes (23): ActivityLogin, AppCompatActivity, Bundle, ActivityPasswordChange, AppCompatActivity, Bundle, ActivitySearch, AppCompatActivity (+15 more)

### Community 4 - "HotViewModel"
Cohesion: 0.14
Nodes (16): HotCategory, HotEffect, HotIntent, HotUiState, Load, OpenDrink, OpenFilter, SeeAll (+8 more)

### Community 6 - "WidgetDataProvider"
Cohesion: 0.10
Nodes (17): ActivityMain, AppCompatActivity, Bundle, DrinkWidgetProvider, Context, Intent, Override, DrinkWidgetService (+9 more)

### Community 7 - "MviAndroidViewModel"
Cohesion: 0.09
Nodes (20): AndroidViewModel, gridRoute(), MixologyApp(), ScreenHeader(), MixologyDrawer(), CollectMviEffects(), Flow, MviAndroidViewModel (+12 more)

### Community 8 - "DrinkDetailsViewModel"
Cohesion: 0.07
Nodes (25): ActivityDetails, AppCompatActivity, Bundle, DrinkHeroImage(), DrinkRecipeBody(), Modifier, FavoriteButton(), Modifier (+17 more)

### Community 9 - "SearchViewModel"
Cohesion: 0.15
Nodes (11): Back, NavigateBack, OpenDrink, Search, SearchEffect, SearchIntent, SearchResultItem, ShowMessageRes (+3 more)

### Community 10 - "RandomixerViewModel"
Cohesion: 0.21
Nodes (8): RandomixerEffect, RandomixerIntent, Refresh, ShowMessageRes, SwipeDiscard, SwipeSave, Job, RandomixerViewModel

### Community 12 - "IngredientMeasure"
Cohesion: 0.12
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

### Community 23 - "FilterKind"
Cohesion: 0.29
Nodes (6): FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT, hasUsableThumb()

### Community 24 - "Measures"
Cohesion: 0.21
Nodes (5): Creator, Override, Parcel, Measures, Parcelable

### Community 25 - "fetch_random_drink.py"
Cohesion: 0.26
Nodes (16): datetime, blank_to_none(), connect(), drink_row(), ensure_schema(), fetch_random_drink(), ingredient_image_url(), ingredient_rows() (+8 more)

### Community 26 - ".onCreate"
Cohesion: 0.20
Nodes (5): ActivitySignUp, AppCompatActivity, Bundle, SignUpScreen(), SignUpScreenTest

### Community 27 - "Mixology"
Cohesion: 0.14
Nodes (13): Credits, Details Screen, Libraries used, License, Main Screen, Mixology, Overview, Phone (+5 more)

### Community 28 - "DrinkFilter"
Cohesion: 0.07
Nodes (28): DrinkListItem, DrinkFilter, ALCOHOLIC, COCKTAIL, COCKTAIL_GLASS, GIN, HIGHBALL_GLASS, NON_ALCOHOLIC (+20 more)

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
- **69 isolated node(s):** `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL`, `ORDINARY_DRINK`, `GIN` (+64 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `Cocktail`, `MixologyTheme`, `DrinkDetailsViewModel`, `RandomixerViewModel`, `DrinkFilterTest`, `IngredientMeasure`, `FilterKind`, `CocktailService`?**
  _High betweenness centrality (0.281) - this node is a cross-community bridge._
- **Why does `MixologyTheme()` connect `MixologyTheme` to `HotViewModel`, `WidgetDataProvider`, `MviAndroidViewModel`, `DrinkDetailsViewModel`, `DrawerDestination`, `ThemeMode`, `.onCreate`, `DrinkFilter`?**
  _High betweenness centrality (0.216) - this node is a cross-community bridge._
- **Why does `Cocktail` connect `Cocktail` to `DrinkDao`, `WidgetDataProvider`, `DrinkDetailsViewModel`, `SearchViewModel`, `Measures`, `CocktailService`?**
  _High betweenness centrality (0.172) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `Drink` (e.g. with `.hasUsableThumb_rejectsMissingAndLiteralNull()` and `.content_showsRecipe_andFavoriteClick()`) actually correct?**
  _`Drink` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Are the 38 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `.onCreate()`) actually correct?**
  _`MixologyTheme()` has 38 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL` to the rest of the system?**
  _69 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Drink` be split into smaller, more focused modules?**
  _Cohesion score 0.024390243902439025 - nodes in this community are weakly interconnected._