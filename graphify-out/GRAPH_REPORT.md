# Graph Report - Mixology  (2026-08-13)

## Corpus Check
- 40 files · ~36,201 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 483 nodes · 957 edges · 21 communities (19 shown, 2 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 100 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d6c95bee`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Drink
- DrinkDao
- DrinkRepository
- SearchAdapter
- FragmentRandomixer.java
- Cocktail
- AppCompatActivity
- ActivityLogin
- ActivityMain
- ActivitySignUp
- DrinkFilterTest
- Mixology
- DrinkFilter
- FragmentDetails.java
- Measures
- gradlew
- custom.md

## God Nodes (most connected - your core abstractions)
1. `Drink` - 95 edges
2. `Cocktail` - 32 edges
3. `FragmentDetails` - 23 edges
4. `ActivityMain` - 21 edges
5. `CocktailService` - 19 edges
6. `DrinkFilter` - 18 edges
7. `IngredientsAdapter` - 17 edges
8. `FragmentRandomixer` - 17 edges
9. `Measures` - 17 edges
10. `ActivityLogin` - 16 edges

## Surprising Connections (you probably didn't know these)
- `hasUsableThumb()` --references--> `Drink`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/repository/DrinkRepository.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/Drink.java
- `ActivitySearch` --implements--> `OnAdapterItemSelectedListener`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Activities/ActivitySearch.java → app/src/main/java/com/capstone/nik/mixology/Adapters/SearchAdapter.java
- `ActivitySearch` --references--> `SearchAdapter`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Activities/ActivitySearch.java → app/src/main/java/com/capstone/nik/mixology/Adapters/SearchAdapter.java
- `MyViewHolder` --inherits--> `ViewHolder`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Adapters/IngredientsAdapter.java → app/src/main/java/com/capstone/nik/mixology/Adapters/DrinkAdapter.kt
- `SearchViewHolder` --inherits--> `ViewHolder`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Adapters/SearchAdapter.java → app/src/main/java/com/capstone/nik/mixology/Adapters/DrinkAdapter.kt

## Import Cycles
- None detected.

## Communities (21 total, 2 thin omitted)

### Community 1 - "DrinkDao"
Cohesion: 0.09
Nodes (17): DrinkDao, Flow, DrinkEntity, DrinkFilterCrossRef, create(), importLegacySavedDrinks(), Context, MixologyDatabase (+9 more)

### Community 2 - "DrinkRepository"
Cohesion: 0.09
Nodes (15): ApplicationComponent, Singleton, Override, MyApplication, DrinkRepository, FilterKind, ALCOHOL, DRINK_TYPE (+7 more)

### Community 4 - "SearchAdapter"
Cohesion: 0.21
Nodes (10): Adapter, Activity, ImageView, LayoutInflater, Override, TextView, View, ViewGroup (+2 more)

### Community 5 - "FragmentRandomixer.java"
Cohesion: 0.12
Nodes (20): IngredientsAdapter, Context, ImageView, LayoutInflater, Override, TextView, View, ViewGroup (+12 more)

### Community 6 - "Cocktail"
Cohesion: 0.09
Nodes (13): Cocktail, Creator, Override, Parcel, DrinkWidgetService, Context, Intent, Override (+5 more)

### Community 7 - "AppCompatActivity"
Cohesion: 0.13
Nodes (17): ActivityDetails, Bundle, Menu, Override, ActivityPasswordChange, Bundle, ImageView, Override (+9 more)

### Community 8 - "ActivityLogin"
Cohesion: 0.14
Nodes (14): ActivityLogin, Bundle, Button, Intent, LinearLayout, Override, ProgressDialog, TextInputEditText (+6 more)

### Community 9 - "ActivityMain"
Cohesion: 0.15
Nodes (12): ActivityMain, Bundle, Menu, Override, TextView, View, CircleImageView, MenuItem (+4 more)

### Community 10 - "ActivitySignUp"
Cohesion: 0.30
Nodes (9): ActivitySignUp, Bundle, Button, ImageView, LinearLayout, Override, ProgressDialog, TextView (+1 more)

### Community 27 - "Mixology"
Cohesion: 0.14
Nodes (13): Credits, Details Screen, Libraries used, License, Main Screen, Mixology, Overview, Phone (+5 more)

### Community 28 - "DrinkFilter"
Cohesion: 0.05
Nodes (37): AndroidViewModel, Diff, DrinkAdapter, ImageView, RecyclerView, TextView, ViewGroup, ViewHolder (+29 more)

### Community 29 - "FragmentDetails.java"
Cohesion: 0.09
Nodes (24): ActivitySearch, Bundle, Menu, Override, RecyclerView, TextView, Callback, FragmentDetails (+16 more)

### Community 30 - "Measures"
Cohesion: 0.17
Nodes (4): Creator, Override, Parcel, Measures

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

## Knowledge Gaps
- **26 isolated node(s):** `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL`, `ORDINARY_DRINK`, `GIN` (+21 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `DrinkRepository`, `.setUIData`, `SearchAdapter`, `FragmentRandomixer.java`, `DrinkFilterTest`, `.onBindViewHolder`, `FragmentDetails.java`, `Measures`?**
  _High betweenness centrality (0.286) - this node is a cross-community bridge._
- **Why does `Cocktail` connect `Cocktail` to `DrinkDao`, `DrinkRepository`, `SearchAdapter`, `FragmentRandomixer.java`, `ActivityMain`, `DrinkFilter`, `FragmentDetails.java`?**
  _High betweenness centrality (0.194) - this node is a cross-community bridge._
- **Why does `FragmentDetails` connect `FragmentDetails.java` to `DrinkRepository`, `.setUIData`, `FragmentRandomixer.java`, `Cocktail`, `ActivityMain`, `Measures`?**
  _High betweenness centrality (0.075) - this node is a cross-community bridge._
- **What connects `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL` to the rest of the system?**
  _26 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Drink` be split into smaller, more focused modules?**
  _Cohesion score 0.043478260869565216 - nodes in this community are weakly interconnected._
- **Should `DrinkDao` be split into smaller, more focused modules?**
  _Cohesion score 0.09407665505226481 - nodes in this community are weakly interconnected._
- **Should `DrinkRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.0907563025210084 - nodes in this community are weakly interconnected._