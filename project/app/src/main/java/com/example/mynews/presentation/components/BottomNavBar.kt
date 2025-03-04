package com.example.mynews.presentation.components

import androidx.compose.foundation.layout.Column
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
import com.example.mynews.domain.model.BottomNavBarItem
import com.example.mynews.ui.theme.*
import com.example.mynews.utils.AppScreenRoutes

// Purpose: bottom navigation bar that is shared among multiple screens

@Composable
fun BottomNavBar (
    items: List<BottomNavBarItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavBarItem) -> Unit
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    BottomNavigation(
        modifier = modifier,
        backgroundColor = BlueGrey,
        elevation = 5.dp
    ) {
        items.forEach { item ->
            //val selected = item.route == backStackEntry.value?.destination?.route
            // The screen and its subscreens will keep the tab highlighted if selected
            // E.g. if HomeScreen is selected, then even after navigating to
            // NewsArticleScreen, home tab will still be highlighted

            /*
            val selected = item.route == backStackEntry.value?.destination?.route ||
                    (item.route == AppScreenRoutes.HomeScreen.route && backStackEntry.value?.destination?.route == AppScreenRoutes.NewsArticleScreen.route)


             */

            val selected = item.route == backStackEntry.value?.destination?.route ||
                    (item.route == AppScreenRoutes.HomeScreen.route &&
                                   (backStackEntry.value?.destination?.route == AppScreenRoutes.NewsArticleScreen.route ||
                                    backStackEntry.value?.destination?.route == AppScreenRoutes.SavedArticlesScreen.route ||
                                    backStackEntry.value?.destination?.route == AppScreenRoutes.CondensedNewsArticleScreen.route       ))

            BottomNavigationItem(
                selected = selected,
                onClick = { onItemClick(item) },
                selectedContentColor = BrightBlue,
                unselectedContentColor = DarkText,
                icon = {
                    Column(horizontalAlignment = CenterHorizontally ) {
                        Icon( imageVector = item.icon,
                            contentDescription = null )
                        if(selected) {
                            Text(
                                text = item.name,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            )
        }
    }
}