package com.example.mynews.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mynews.presentation.theme.BlueGrey
import com.example.mynews.presentation.theme.BrightBlue
import com.example.mynews.presentation.theme.DarkText
import com.example.mynews.utils.AppScreenRoutes

@Composable
fun BottomNavBar (
    items: List<BottomNavBarItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavBarItem) -> Unit
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    BottomNavigation(
        modifier = modifier.height(70.dp),
        backgroundColor = BlueGrey,
        elevation = 5.dp
    ) {
        items.forEach { item ->

            val selected = item.route == backStackEntry.value?.destination?.route ||

                    // keep home tab highlighted in nav bar when also on NewsArticleScreen, SavedArticlesScreen, CondensedNewsArticleScreen
                    (item.route == AppScreenRoutes.HomeScreen.route &&
                                   (backStackEntry.value?.destination?.route == AppScreenRoutes.NewsArticleScreen.route ||
                                    backStackEntry.value?.destination?.route == AppScreenRoutes.SavedArticlesScreen.route ||
                                    backStackEntry.value?.destination?.route == AppScreenRoutes.CondensedNewsArticleScreen.route)) ||
                    // keep social tab highlighted in nav bar when also on FriendsScreen
                    (item.route == AppScreenRoutes.SocialScreen.route &&
                                    (backStackEntry.value?.destination?.route == AppScreenRoutes.FriendsScreen.route))

            BottomNavigationItem(
                selected = selected,
                onClick = { onItemClick(item) },
                selectedContentColor = BrightBlue,
                unselectedContentColor = DarkText,
                icon = {
                    Column(horizontalAlignment = CenterHorizontally ) {
                        Icon(
                            modifier = modifier.size(28.dp),
                            imageVector = item.icon,
                            contentDescription = null )
                        if(selected) {
                            Text(
                                text = item.name,
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            )
        }
    }
}