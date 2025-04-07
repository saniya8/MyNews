package com.example.mynews.service.repositories.home

import com.example.mynews.service.condensednewsarticle.CondensedNewsArticleApiClient
import com.example.mynews.utils.MainDispatcherRule
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
class CondensedNewsArticleRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val apiClient: CondensedNewsArticleApiClient = mock()
    private val logger: Logger = mock()

    private lateinit var repository: CondensedNewsArticleRepositoryImpl

    @Before
    fun setup() {
        repository = CondensedNewsArticleRepositoryImpl(
            condensedNewsArticleApiClient = apiClient,
            logger = logger
        )
    }

    @Test
    fun `getArticleText returns extracted and truncated text when html is valid`() = runTest {
        val html = "<html><body><p>This is a test article text.</p></body></html>"
        whenever(apiClient.getArticleText(eq("https://example.com"))).thenReturn(html)

        val result = repository.getArticleText("https://example.com")

        assertTrue(result.contains("This is a test article text"))
        verify(logger).d(eq("CondensedNewsApi"), argThat { contains("Fetching article") })
        verify(logger).d(eq("CondensedNewsApi"), argThat { contains("Fetched HTML") })
        verify(logger).d(eq("CondensedNewsApi"), argThat { contains("Extracted text") })
    }

    @Test
    fun `getArticleText returns fallback message when html is empty`() = runTest {
        whenever(apiClient.getArticleText(eq("https://example.com"))).thenReturn("")

        val result = repository.getArticleText("https://example.com")

        assertEquals("No article content found in Diffbot response", result)
    }

    @Test
    fun `getArticleText returns fallback message when parsed text is empty`() = runTest {
        val html = "<html><body></body></html>"
        whenever(apiClient.getArticleText(eq("https://example.com"))).thenReturn(html)

        val result = repository.getArticleText("https://example.com")

        assertEquals("Failed to extract meaningful content from the article", result)
    }

    @Test
    fun `getArticleText returns error message on exception`() = runTest {
        val testUrl = "https://example.com"
        val exception = RuntimeException("Network error")

        // Arrange: simulate exception
        whenever(apiClient.getArticleText(eq(testUrl)))
            .thenThrow(exception)

        // Act
        val result = repository.getArticleText(testUrl)

        // Assert
        assertTrue(result.startsWith("Error: Network error"))

        // Verify logger call with exact values (no matchers)
        verify(logger).e(
            eq("CondensedNewsApi"),
            eq("Error fetching article: ${exception.message}"),
            eq(exception)
        )
    }


    @Test
    fun `summarizeText returns summary when input is valid`() = runTest {
        val input = "This is a long article about climate change and policy."
        val expectedSummary = "Summary of the article"
        whenever(apiClient.summarizeText(eq(input), eq(50))).thenReturn(expectedSummary)

        val result = repository.summarizeText(input, 50)

        assertEquals(expectedSummary, result)

        verify(logger).d(eq("CondensedNewsApi"), eq("Summarizing text with word limit: 50"))
        verify(logger).d(eq("CondensedNewsApi"), eq("Summary result: $expectedSummary"))
    }

    @Test
    fun `summarizeText returns fallback if input starts with Error`() = runTest {
        val input = "Error: Failed to fetch"

        val result = repository.summarizeText(input, 50)

        assertEquals("Error: Unable to summarize text.", result)

        // Ensure the summarization client and logger weren't used
        verifyNoInteractions(apiClient)
        verifyNoMoreInteractions(logger)
    }


    @Test
    fun `summarizeText returns error message on exception`() = runTest {
        val exception = RuntimeException("NLP crash")

        // Fix: use anyString() instead of any() to avoid null issues
        whenever(apiClient.summarizeText(anyString(), eq(100)))
            .thenThrow(exception)

        val result = repository.summarizeText("Valid text to summarize", 100)

        assertEquals("Error: Unable to summarize text.", result)

        // Fix: use full eq(...) on all args to avoid matcher conflict
        verify(logger).e(
            eq("CondensedNewsApi"),
            eq("Error summarizing text: ${exception.message}"),
            eq(exception)
        )
    }


}