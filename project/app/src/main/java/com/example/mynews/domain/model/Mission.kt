package com.example.mynews.domain.model

data class Mission(
    val id: String,
    val name: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int,
    val isCompleted: Boolean,
    val type: String
) {
    companion object {
        val defaultMissions = listOf(
            Mission(
                id = "mission_1",
                name = "Read 3 Articles",
                description = "Read 3 news articles to stay informed",
                targetCount = 3,
                currentCount = 0,
                isCompleted = false,
                type = "read_article"
            ),
            Mission(
                id = "mission_2",
                name = "React to 2 Articles",
                description = "React to 2 articles to share your thoughts",
                targetCount = 2,
                currentCount = 0,
                isCompleted = false,
                type = "react_to_article"
            ),
            Mission(
                id = "mission_3",
                name = "Add 5 Friends",
                description = "Add 5 friends to your network",
                targetCount = 5,
                currentCount = 0,
                isCompleted = false,
                type = "add_friend"
            ),
            Mission(
                id = "mission_4",
                name = "Add 20 Friends",
                description = "Add 20 friends to your network",
                targetCount = 20,
                currentCount = 0,
                isCompleted = false,
                type = "add_friend"
            ),
            Mission(
                id = "mission_5",
                name = "React to 50 Articles",
                description = "React to 50 articles with an emoji",
                targetCount = 50,
                currentCount = 0,
                isCompleted = false,
                type = "add_friend"
            ),
            Mission(
                id = "mission_6",
                name = "Read 30 Articles",
                description = "Read 30 different articles",
                targetCount = 30,
                currentCount = 0,
                isCompleted = false,
                type = "add_friend"
            )
        )
    }
}

