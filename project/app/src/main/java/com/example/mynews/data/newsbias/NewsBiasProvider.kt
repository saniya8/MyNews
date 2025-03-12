package com.example.mynews.data.newsbias
import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException


// Note: NewsBiasRatings.json is in src/main/assets/newsbias/NewsBiasRatings.json
// It has to be kept in src/main/assets for it to be correctly loaded and read

// the root object in the JSON file that represents the entire JSON structure
@Serializable
data class AllSidesData(
    val allsides_media_bias_ratings: List<PublicationWrapper>
)

// represents each entry in the allsides_media_bias_ratings list (ie in the root)
// each entry has a "publication" field which holds the actual NewsBias object
@Serializable
data class PublicationWrapper(
    val publication: NewsBias
)

class NewsBiasProvider(context: Context) {

    // stores mapping of <source_name, media_bias_rating>
    private val biasMap: Map<String, String>

    init {
        // load the JSON data as a raw string
        val jsonString = loadNewsBiasRatings(context)
        //Log.d("NewsBiasDebug", "Loaded JSON: $jsonString")
        // parse the JSON raw string and store it as a map
        biasMap = parseNewsBiasRatings(jsonString)
        //Log.d("NewsBiasDebug", "Bias Map: $biasMap")
    }

    // loadNewsBiasRatings: loads the JSON file from the filepath and stores it in a string (ie raw JSON string)
    private fun loadNewsBiasRatings(context: Context): String {
        return try {
            context.assets.open("newsbias/NewsBiasRatings.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            Log.e("NewsBiasProvider", "Error with loadNewsBiasRatings: ${e.message}", e)
            e.printStackTrace()
            "{}"
        }
    }

    // parseNewsBiasRatings: parses the raw JSON string into a map of source_name -> media_bias_rating
    private fun parseNewsBiasRatings(jsonString: String): Map<String, String> {
        return try {

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
            // deserialize JSON into AllSidesData (which will have List<PublicationWrapper>
            // i.e., convert raw JSON string into structured kotlin objects

            /*

            // Example: allSidesData (which stores post-deserializing the Json string)

            AllSidesData(
                allsides_media_bias_ratings = listOf(
                    PublicationWrapper(
                        publication = NewsBias(source_name = "CNN", media_bias_rating = "Left")
                    ),
                    PublicationWrapper(
                        publication = NewsBias(source_name = "Fox News", media_bias_rating = "Right")
                    )
                )
            )

             */

            val jsonParser = Json { ignoreUnknownKeys = true } // to ignore source_type, source_url, and allsides_url fields
            val allSidesData: AllSidesData = jsonParser.decodeFromString(jsonString)

            // STEP 2

            // convert the list into a map

            /*

            // Example: allSidesData (after converting the list from STEP 1 to a map)

            {
                "CNN" -> "Left",
                "Fox News" -> "Right"
            }


             */

            return allSidesData.allsides_media_bias_ratings.associate {
                it.publication.source_name to it.publication.media_bias_rating
            }


        } catch (e: Exception) {
            Log.d("NewsBiasDebug", "Error in parseNewsBiaseRatings: ${e.message}, e")
            e.printStackTrace()
            emptyMap()
        }
    }

    fun getAllBiasMappings(): Map<String, String> {
        return biasMap
    }

    fun getBiasForSource(sourceName: String): String {

        // EXACT MATCHES

        //return biasMap[sourceName] ?: "Neutral"

        // -----------------------------------------

        // FUZZY MATCHES

        // check for exact match
        biasMap[sourceName]?.let { return it }

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

        val closestMatch = biasMap.keys.map { jsonSource ->
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
        return closestMatch?.let { biasMap[it.first] } ?: "Neutral"

    }


}