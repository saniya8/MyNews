import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.model.home.HomeModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.presentation.viewmodel.home.HomeViewModel
import com.example.mynews.utils.MainDispatcherRule
import com.example.mynews.utils.TestDataFactory
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val userModel: UserModel = mock()
    private val homeModel: HomeModel = mock()
    private val logger: Logger = mock()

    private val testUserId = "unittestmock1"
    private val testArticle: Article = TestDataFactory.createIndexedArticle(1)

    @Before
    fun setup() {
        reset(userModel, homeModel, logger)
    }

    @Test
    fun `init calls trackReactions when user is valid`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        val viewModel = HomeViewModel(userModel, homeModel, logger)
        advanceUntilIdle()

        verify(homeModel).trackReactions(testUserId)
        verify(logger, never()).e(any(), any())
    }

    @Test
    fun `init logs error when user is null`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        val viewModel = HomeViewModel(userModel, homeModel, logger)
        advanceUntilIdle()

        verify(logger).e("HomeViewModel", "No user logged in. User ID is null or empty")
        verify(homeModel, never()).trackReactions(any())
    }

    @Test
    fun `fetchReaction calls model and returns result`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)
        whenever(homeModel.getReaction(testUserId, testArticle)).thenReturn("😂")

        var result: String? = null
        val viewModel = HomeViewModel(userModel, homeModel, logger)

        viewModel.fetchReaction(testArticle) { reaction ->
            result = reaction
        }

        advanceUntilIdle()

        verify(homeModel).getReaction(testUserId, testArticle)
        assert(result == "😂")
    }

    @Test
    fun `fetchReaction logs error when user is null`() = runTest {

        whenever(userModel.getCurrentUserId()).thenReturn(null)

        val viewModel = HomeViewModel(userModel, homeModel, logger)

        var wasCalled = false

        viewModel.fetchReaction(testArticle) { wasCalled = true }

        advanceUntilIdle()

        verify(logger, times(2)).e("HomeViewModel", "No user logged in. User ID is null or empty")
        verify(homeModel, never()).getReaction(any(), any())
        assert(!wasCalled)
    }



    @Test
    fun `updateReaction calls model with correct data`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        val viewModel = HomeViewModel(userModel, homeModel, logger)
        viewModel.updateReaction(testArticle, "❤️")

        advanceUntilIdle()

        verify(homeModel).setReaction(testUserId, testArticle, "❤️")
    }

    @Test
    fun `updateReaction logs error when user is null`() = runTest {

        whenever(userModel.getCurrentUserId()).thenReturn(null)

        val viewModel = HomeViewModel(userModel, homeModel, logger)

        viewModel.updateReaction(testArticle, "😡")

        advanceUntilIdle()

        verify(logger, times(2)).e("HomeViewModel", "No user logged in. User ID is null or empty")
        verify(homeModel, never()).setReaction(any(), any(), any())
    }


}
