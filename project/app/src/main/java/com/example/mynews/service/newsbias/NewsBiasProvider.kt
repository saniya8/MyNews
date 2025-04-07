package com.example.mynews.service.newsbias
import android.content.Context
import android.util.Log
import com.example.mynews.domain.entities.NewsBiasResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

// Note:
// The NewsBiasProvider provides the news source to bias mappings as follows:
// - First, it attempts to retrieve the data from the All Sides API, which contains up-to-date mappings in JSON
// - If there is an issue with the API, it falls back to retrieving the data from the JSON file in src/main/assets
// which is the same JSON file as the one in the API (from the same provider), except this file is hardcoded so
// may not be as up to date

// Note: NewsBiasRatings.json is in src/main/assets/newsbias/NewsBiasRatings.json
// It has to be kept in src/main/assets for it to be correctly loaded and read

class NewsBiasProvider(context: Context) {

    // stores mapping of <source_name, media_bias_rating>
    private val _biasMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val biasMap: StateFlow<Map<String, String>> = _biasMap // for NewsViewModel

    private val newsBiasApi = NewsBiasApiClient()

    private val context = context.applicationContext // prevent memory leaks
    suspend fun startFetchingBiasData() {
        withContext(Dispatchers.IO) {
            _biasMap.value = fetchBiasData(context)
        }
    }

    private suspend fun fetchBiasData(context: Context): Map<String, String> {

        try {
            // Attempt API fetch via Ktor
            val response = newsBiasApi.getBiasRatings()

            Log.d("NewsBiasProvider", "Successfully fetched bias data from API")

            val parsedNewsBias = parseNewsBiasFromApi(response) // parse response directly
            return parsedNewsBias

        } catch (e: IOException) { // network errors
            Log.e("NewsBiasDebug", "Network error: ${e.message}", e)

        } catch (e: HttpException) { // HTTP errors
            Log.e("NewsBiasDebug", "API error: ${e.message}", e)

        } catch (e: Exception) { // any errors
            Log.e("NewsBiasDebug", "Error: ${e.message}", e)
        }

        Log.d("NewsBiasDebug", "Falling back to local JSON") //fallback on failure
        val jsonString = loadNewsBiasFromJson(context)
        return parseNewsBiasFromJson(jsonString)

    }

    // loadNewsBiasFromJson: loads the JSON file from the filepath and stores it in a string (ie raw JSON string)
    private fun loadNewsBiasFromJson(context: Context): String {
        try {
            val json = context.assets.open("newsbias/NewsBiasRatings.json")
                .bufferedReader()
                .use { it.readText() }

            if (json.isBlank()) {
                Log.w("NewsBiasDebug", "JSON file is empty, return default structure")
                return """{"allsides_media_bias_ratings": []}"""
            }

            return json

        } catch (e: Exception) {
            Log.e("NewsBiasDebug", "Error with loadNewsBiasRatings: ${e.message}", e)
            e.printStackTrace()
            return """{"allsides_media_bias_ratings": []}"""
        }
    }

    // parseNewsBiasFromApi: parse API response into bias map
    private fun parseNewsBiasFromApi(response: NewsBiasResponse): Map<String, String> {
        return response.allsides_media_bias_ratings.associate {
            it.publication.source_name to it.publication.media_bias_rating
        }
    }

    // parseNewsBiasFromJson: parses the raw JSON string into a map of source_name -> media_bias_rating
    private fun parseNewsBiasFromJson(jsonString: String): Map<String, String> {
        try {

            // STEP 1
            // deserialize JSON into a NewsBiasResponse object
            // i.e., convert raw JSON string into structured kotlin objects

            val jsonParser = Json { ignoreUnknownKeys = true } // to ignore source_type, source_url, and allsides_url fields
            val parsedData = jsonParser.decodeFromString(NewsBiasResponse.serializer(), jsonString)

            // STEP 2

            // convert the list into a map

            return parsedData.allsides_media_bias_ratings.associate {
                it.publication.source_name to it.publication.media_bias_rating
            }

        } catch (e: Exception) {
            Log.d("NewsBiasDebug", "Error in parseNewsBiasRatings: ${e.message}, e")
            e.printStackTrace()
            return emptyMap()
        }
    }

    fun getAllBiasMappings(): StateFlow<Map<String, String>> {
        return biasMap
    }

    suspend fun getBiasForSource(sourceName: String): String {

        // EXACT MATCHES

        val biasData = biasMap.first { it.isNotEmpty() }

        // check for exact match
        biasData[sourceName]?.let { return it }

        // FUZZY MATCHES

        // tokenized fuzzy catching
        val sourceWords = sourceName.split(" ").map { it.lowercase() }.toSet() // convert to lowercase words

        val closestMatch = biasData.keys.map { jsonSource ->
            val jsonWords = jsonSource.split(" ").map { it.lowercase() }.toSet()
            val commonWords = sourceWords.intersect(jsonWords).size // Count common words
            jsonSource to commonWords
        }
            .filter { (_, commonWords) ->
                // match if:
                // - more than **1 word** matches (higher confidence)
                // - or the source has **exactly 2 words** and **1 matches** (e.g., "CBS Sports" → "CBS 58")
                // - or the source is **one word only**, but an exact word match exists in JSON (e.g., "CBS" → "CBS 58")
                commonWords > 1 || (sourceWords.size == 2 && commonWords == 1) || (sourceWords.size == 1 && commonWords == 1)
            }
            .maxByOrNull { it.second } // pick the match with the most common words

        // return the matched bias, else Neutral
        return closestMatch?.let { biasData[it.first] } ?: "Neutral"
    }

}