package com.example.mynews.utils

import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.entities.Source

object TestDataFactory {

    fun createArticle(
        url: String,
        title: String,
        author: String,
        description: String,
        imageUrl: String,
        publishedAt: String,
        content: String,
        sourceId: String,
        sourceName: String
    ): Article {
        return Article(
            source = Source(id = sourceId, name = sourceName),
            author = author,
            title = title,
            description = description,
            url = url,
            urlToImage = imageUrl,
            publishedAt = publishedAt,
            content = content
        )
    }

    fun createIndexedArticle(index: Int): Article {
        return createArticle(
            url = "https://example.com/article$index",
            title = "Article Title $index",
            author = "Author $index",
            description = "This is the description for article $index.",
            imageUrl = "https://example.com/image$index.jpg",
            publishedAt = "2025-04-${String.format("%02d", index)}T12:00:00Z",
            content = "This is the full content for article $index.",
            sourceId = "source-id-$index",
            sourceName = "Source $index"
        )
    }

    fun createArticleList(count: Int): List<Article> {
        return (1..count).map { index -> createIndexedArticle(index) }
    }
}

// Indices
//1–10: Saved Articles
// 100–110: Reactions