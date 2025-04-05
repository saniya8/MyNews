package com.example.mynews.service.newsbias
import android.content.Context
import android.util.Log
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


    //private var biasMap: Map<String, String> = emptyMap()
    private val _biasMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val biasMap: StateFlow<Map<String, String>> = _biasMap // for NewsViewModel

    //private val newsBiasApi = NewsBiasRetrofitInstance.newsBiasApi
    private val newsBiasApi = NewsBiasApiClient()

    /*init {
        // load the JSON data as a raw string
        //val jsonString = loadNewsBiasRatings(context)
        //Log.d("NewsBiasDebug", "Loaded JSON: $jsonString")
        // parse the JSON raw string and store it as a map
        //biasMap = parseNewsBiasRatings(jsonString)
        //Log.d("NewsBiasDebug", "Bias Map: $biasMap")

        CoroutineScope(Dispatchers.IO).launch {
            _biasMap.value = fetchBiasData(context)
        }

    }*/

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

        /*

        try {
            //throw IOException("Forced failure") // to test that fallback occurred
            //val response = newsBiasApiClient.getBiasRatings()
            val response = newsBiasApi.getBiasRatings()

            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d("NewsBiasProvider", "successfully fetched bias data from API")
                    val parsedNewsBias = parseNewsBiasFromApi(it)
                    return parsedNewsBias
                }
            }

            // API responded but was not successful (API request fails but no exception thrown)
            Log.w("NewsBiasDebug", "API request failed but no exception thrown")

        } catch (e: IOException) { // network errors
            Log.e("NewsBiasDebug", "Network error: ${e.message}", e)

        } catch (e: HttpException) { // HTTP errors
            Log.e("NewsBiasDebug", "API error: ${e.message}", e)

        } catch (e: Exception) { // any errors
            Log.e("NewsBiasDebug", "Error: ${e.message}", e)

        }

        Log.d("NewsBiasDebug", "Falling back to local JSON")
        val jsonString = loadNewsBiasFromJson(context)
        return parseNewsBiasFromJson(jsonString) // fallback to local JSON

         */

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

    // parse API response into bias map
    private fun parseNewsBiasFromApi(response: NewsBiasResponse): Map<String, String> {
        return response.allsides_media_bias_ratings.associate {
            it.publication.source_name to it.publication.media_bias_rating
        }
    }

    // parseNewsBiasFromJson: parses the raw JSON string into a map of source_name -> media_bias_rating
    private fun parseNewsBiasFromJson(jsonString: String): Map<String, String> {
        try {

            /*

            // Example: jsonString

            {
              "allsides_media_bias_ratings": [
                {
                  "publication": {
                    "source_name": "CNN",
                    "media_bias_rating": "Left"
                  }
                },
                {
                  "publication": {
                    "source_name": "Fox News",
                    "media_bias_rating": "Right"
                  }
                }
              ]
            }

            */


            // STEP 1
            // deserialize JSON into a NewsBiasResponse object
            // i.e., convert raw JSON string into structured kotlin objects

            /*

            // Example:

                NewsBiasResponse(
                    allsides_media_bias_ratings = listOf(
                        AllSidesData(
                            publication = Publication(
                                source_name = "CNN",
                                media_bias_rating = "Left",
                                allsides_url = "",  // Other fields exist but are ignored in our mapping
                                source_type = "",
                                source_url = ""
                            )
                        ),
                        AllSidesData(
                            publication = Publication(
                                source_name = "Fox News",
                                media_bias_rating = "Right",
                                allsides_url = "",
                                source_type = "",
                                source_url = ""
                            )
                        )
                    )
                )

             */

            val jsonParser = Json { ignoreUnknownKeys = true } // to ignore source_type, source_url, and allsides_url fields
            val parsedData = jsonParser.decodeFromString(NewsBiasResponse.serializer(), jsonString)

            // STEP 2

            // convert the list into a map

            /*

            // Example:

                {
                    "CNN" -> "Left",
                    "Fox News" -> "Right"
                }


             */

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

        //return biasMap[sourceName] ?: "Neutral"

        // -----------------------------------------

        // FUZZY MATCHES

        val biasData = biasMap.first { it.isNotEmpty() }

        // check for exact match
        biasData[sourceName]?.let { return it }

        /*

        // fuzzy match 1

        // if no exact match, check for a fuzzy match
        val closestMatch = biasMap.keys.find { jsonSource ->
            sourceName.startsWith(jsonSource, ignoreCase = true) ||
            jsonSource.startsWith(sourceName, ignoreCase = true) // More refined match
        }

        // if close match found then return its bias, otherwise return Neutral
        //return closestMatch?.let { biasMap[it] } ?: "Neutral"
        return if (closestMatch != null && biasMap.containsKey(closestMatch)) {
            biasMap[closestMatch]!! // guaranteed to be non-null
        } else {
            "Neutral"
        }

         */

        /*

        // fuzzy match 2

        val sourceWords = sourceName.split(" ").map { it.lowercase() } // Convert to lowercase words


        val closestMatch = biasMap.keys.find { jsonSource ->
            val jsonWords = jsonSource.split(" ").map { it.lowercase() } // Convert JSON source to words
            sourceWords.any { it in jsonWords } // Check if any word in sourceName exists in JSON keys
        }

        // return the matched bias, else Neutral
        return closestMatch?.let { biasMap[it] } ?: "Neutral"

         */

        /*

        // fuzzy match 3

        val sourceWords = sourceName.split(" ").map { it.lowercase() } // Convert to lowercase words

        val closestMatch = biasMap.keys
            .filter { jsonSource ->
                val jsonWords = jsonSource.split(" ").map { it.lowercase() }
                sourceWords.any { it in jsonWords }
            }
            .minByOrNull { it.length } // prefers shorter, simplet matches matches

        return closestMatch?.let { biasMap[it] } ?: "Neutral"

         */

        // fuzzy match 4

        // good enough for now
        // some fuzzy issues like
        // Minneapolis Star Tribune
        // NBCSports

        // tokenized fuzzy catching
        val sourceWords = sourceName.split(" ").map { it.lowercase() }.toSet() // Convert to lowercase words

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