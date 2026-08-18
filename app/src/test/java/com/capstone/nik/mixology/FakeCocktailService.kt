package com.capstone.nik.mixology

import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.remoteModel.CatalogListItem
import com.capstone.nik.mixology.Network.remoteModel.CatalogListResponse
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbDrink
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbResponse

class FakeCocktailService : CocktailService {
    var alcohol = CocktailDbResponse()
    var glass = CocktailDbResponse()
    var ingredient = CocktailDbResponse()
    var drinkType = CocktailDbResponse()
    var drinkTypesByQuery: Map<String, CocktailDbResponse> = emptyMap()
    var search = CocktailDbResponse()
    var random = CocktailDbResponse()
    var lookup = CocktailDbResponse()
    var letter = CocktailDbResponse()
    var categories = CatalogListResponse()
    var glasses = CatalogListResponse()
    var ingredients = CatalogListResponse()
    var alcoholic = CatalogListResponse()
    var failLookup = false
    var failRandom = false

    override suspend fun getAlcoholFilter(filter: String) = alcohol
    override suspend fun getGlassFilter(filter: String) = glass
    override suspend fun getIngredientFilter(filter: String) = ingredient
    override suspend fun getDrinkTypeFilter(filter: String) =
        drinkTypesByQuery[filter] ?: drinkType
    override suspend fun getSearchResults(search: String) = this.search
    override suspend fun getRandomixer(): CocktailDbResponse {
        if (failRandom) error("should not hit random.php")
        return random
    }
    override suspend fun getDrinkById(id: String): CocktailDbResponse {
        if (failLookup) error("lookup failed")
        return lookup
    }
    override suspend fun getSearchByLetter(letter: String) = this.letter
    override suspend fun listCategories(list: String) = categories
    override suspend fun listGlasses(list: String) = glasses
    override suspend fun listIngredients(list: String) = ingredients
    override suspend fun listAlcoholic(list: String) = alcoholic
}

fun cocktailDrink(
    id: String,
    name: String,
    thumb: String = "https://example.com/$id.jpg",
    instructions: String? = "Shake.",
    ingredient: String? = "Gin",
): CocktailDbDrink = CocktailDbDrink(
    idDrink = id,
    strDrink = name,
    strDrinkThumb = thumb,
    strInstructions = instructions,
    strIngredient1 = ingredient,
    strMeasure1 = "1 oz",
)

fun catalog(vararg names: String) = CatalogListResponse(
    drinks = names.map { CatalogListItem(strIngredient1 = it) },
)
