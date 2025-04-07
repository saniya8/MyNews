package com.example.mynews.utils

import com.example.mynews.domain.entities.Article

fun articleToMap(article: Article): Map<String, Any?> {
    return mapOf(
        "author" to article.author,
        "content" to article.content,
        "description" to article.description,
        "publishedAt" to article.publishedAt,
        "source" to mapOf(
            "id" to article.source.id,
            "name" to article.source.name
        ),
        "title" to article.title,
        "url" to article.url,
        "urlToImage" to article.urlToImage
    )
}