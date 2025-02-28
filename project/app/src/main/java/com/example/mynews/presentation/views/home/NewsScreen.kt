package com.example.mynews.presentation.views.home

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mynews.presentation.viewmodel.NewsViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.mynews.data.api.Article
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mynews.utils.AppScreenRoutes
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Composable
fun NewsScreen(
    navController: NavHostController,
    newsViewModel: NewsViewModel,
) {

    // Observe the articles
    val articles by newsViewModel.articles.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxSize()

    ) {

        // LazyColumn is used to display a vertically scrolling list of items
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(articles) {article ->
                //Text(text = article.title) // for testing
                //Text(text = article.urlToImage) // for testing
                //Text(text = "----------------") // for testing
                ArticleItem(navController = navController,
                            article = article)

            }


        }

    }


}



@Composable
fun ArticleItem(
    navController: NavHostController,
    article: Article
){
    Card(
        modifier = Modifier
            .padding(8.dp)
            .height(110.dp), // fixing image
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = {
            //val articleJson = Json.encodeToString(article) // convert to JSON
            //Log.d("Serialization Debug", "Encoded JSON: $articleJson")
            //val encodedJson = Uri.encode(articleJson)
            //navController.navigate("${AppScreenRoutes.NewsArticleScreen.route}/$articleJson")

            //navController.navigate(AppScreenRoutes.NewsArticleScreen.route)

            val encodedUrl = Uri.encode(article.url) // Encode to handle special characters
            navController.navigate(AppScreenRoutes.NewsArticleScreen.createRoute(encodedUrl))


        }
    ){
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            // Show the thumbnail

            Log.d("CoilDebug", "Loading image URL: ${article.urlToImage}")


            val placeholderImage : String = "https://s.france24.com/media/display/e6279b3c-db08-11ee-b7f5-005056bf30b7/w:1024/p:16x9/news_en_1920x1080.jpg"

            val articleImageUrl = if (article.urlToImage?.startsWith("https") == true) {
                // use the article.urlToImage only if it is non-null and starts with "https"
                // so image is retrieved correctly
                article.urlToImage
            } else {
                // if article.urlToImage is null, or if article.urlToImage is not null but
                // doesn't start with "https", then use the placeholder image
                placeholderImage
            }

            AsyncImage(model = articleImageUrl /*article.urlToImage?: placeholderImage*/,
                contentDescription = "Article Image",
                modifier = Modifier.size(80.dp)
                    .size(80.dp) // fixing image to fixed square size
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(start = 8.dp) // gap between Date at top of page and the scrollable articles
            ) {

                // Show the article title
                Text(text = article.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis // Cuts off text with "..."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Adds space between the two Text objects
                ) {
                    // Show the source
                    Text(text = article.source.name,
                        maxLines = 1,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Condensed Article",
                        modifier = Modifier.clickable {
                            // Navigate to the CondensedNewsArticleScreen with article content
                            navController.navigate(
                                AppScreenRoutes.CondensedNewsArticleScreen.createRoute(
                                    "Sample text to be replaced by condensed article content eventually"
                                )
                            )
                        },
                        color = Color.Blue,
                        fontSize = 14.sp
                    )
                }

            }
        }
    }
}
