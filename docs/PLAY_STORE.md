# Play Store listing

Use this copy for the Mixology store listing. Capture screenshots on a Pixel-class phone at the default font scale, then again at 1.3× and 2.0× to confirm TalkBack labels and wrapping.

## English

**Short description**
Stock your bar, see what you can make, and ask Ada, your AI bartender.

**Full description**
Mixology helps you make better drinks with what you already have.

- My Bar: add your bottles and mixers, see what you can make tonight, and which one bottle unlocks the most new drinks
- Ada, an AI bartender who knows your bar and can update it and your shopping list for you
- Import recipes from a link or a post on Instagram, TikTok, YouTube and more, or write your own
- Search thousands of recipes by drink name or ingredient, and browse by category, glass, ingredient or alcoholic type
- Shopping list grouped by aisle, with what you buy added to My Bar
- Saved recipes with notes and ratings, synced to your account
- Home-screen widget for saved cocktails
- Light, dark, and system appearance

Mixology needs an account. New accounts get everything free for 7 days; after that a monthly or yearly subscription is required.

Recipes are for adults of legal drinking age. Drink responsibly.

## Español

**Descripción breve**
Arma tu bar, descubre qué puedes preparar y pregúntale a Ada, tu bartender con IA.

**Descripción completa**
Mixology te ayuda a preparar mejores bebidas con lo que ya tienes.

- Mi Bar: añade tus botellas y mezcladores, mira qué puedes preparar hoy y qué botella te abre más bebidas nuevas
- Ada, una bartender con IA que conoce tu bar y puede actualizarlo, igual que tu lista de compras
- Importa recetas desde un enlace o una publicación de Instagram, TikTok, YouTube y más, o escribe las tuyas
- Busca entre miles de recetas por nombre o ingrediente, y explora por categoría, vaso, ingrediente o tipo alcohólico
- Lista de compras agrupada por pasillo; lo que compras pasa a Mi Bar
- Recetas guardadas con notas y valoraciones, sincronizadas con tu cuenta
- Widget de cócteles guardados
- Apariencia clara, oscura o del sistema

Mixology necesita una cuenta. Las cuentas nuevas tienen todo gratis durante 7 días; después hace falta una suscripción mensual o anual.

Las recetas son para adultos en edad legal. Bebe con responsabilidad.

## Screenshot checklist

1. Home with the My Bar carousel
2. My Bar with "Buy next" and "You can make"
3. Ada with drink cards and a recipe card
4. Drink details (ingredients, instructions, save)
5. Search with Drink name / Ingredient chips
6. Shopping list by aisle
7. Import recipe
8. Home-screen widget

## Play Console checklist

The listing copy above is not the whole submission. These are the declarations Play asks for, with what the app does today so the answers match the code.

**Privacy policy**
- URL: `https://nikhil-31.github.io/Mixology/privacy-policy` (the same link Settings and the paywall open). Check it covers everything in the Data safety list below.

**Account deletion**
- In the app: Settings > Delete account, and on the trial-ended paywall.
- Play also needs a web link where someone can ask for deletion without installing the app. To do: publish that page and enter its URL in the Data safety form.

**Data safety**
What leaves the device, and why:

| Data | Where it goes | Why |
|---|---|---|
| Email, display name, account ID | Mixology backend (Firebase sign-in) | Account |
| Saved drinks, My Bar, shopping list, your recipes, notes and ratings | Mixology backend | Sync across devices |
| Recipe photos and photos sent to Ada | Mixology backend | Your recipes, and Ada reading a bottle or menu |
| Messages to Ada, and text or links you import | Mixology backend and its AI provider | Ada's replies and recipe import |
| Purchase history | Google Play Billing, confirmed by the backend | Subscription |
| Crash logs and diagnostics | Firebase Crashlytics | Stability |
| App interactions | Firebase Analytics | Product analytics |
| Advertising ID and device identifiers | AdMob, only for accounts that are shown ads and after the consent prompt | Ads |

- Voice input uses the device's speech recognizer (`RECORD_AUDIO`). The app sends the recognized text to Ada, not the audio. Declare the microphone permission's purpose, and check how the device's recognizer handles audio before answering "audio collected: no".
- Data is encrypted in transit: release builds refuse a backend URL that isn't `https://`.
- Users can request deletion: yes (see above).
- To do: confirm the AI provider and retention periods with the backend before submitting.

**Content rating and audience**
- The app is about alcohol: answer the rating questionnaire's alcohol questions accordingly, and set the target audience to adults only (18+). To do: decide whether the listing needs country restrictions where alcohol content is limited.

**Ads**
- Declare "Contains ads": banner ads show for accounts that qualify (see `showsBannerAds`).

**Subscription**
- Products: `mixology_premium_monthly`, `mixology_premium_yearly`.
- The description already says an account is required and that the 7-day trial is followed by a paid subscription; keep that sentence, Play rejects listings that hide it.
- The paywall shows the price and renewal terms, and Settings links to Google Play to manage or cancel. Recheck both after any price change.

**Signing**
- Do not upload a build signed with either keystore that was once in this repo; see "Signing" in the README.
