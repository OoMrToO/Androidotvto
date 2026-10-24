package com.nuvio.tv.ui.components

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.nuvio.tv.ui.util.contentTextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.nuvio.tv.R
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nuvio.tv.domain.model.ContentType
import com.nuvio.tv.domain.model.CardDepthSurface
import com.nuvio.tv.domain.model.MetaPreview
import com.nuvio.tv.domain.model.PosterShape
import com.nuvio.tv.ui.theme.NuvioTheme
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.nuvio.tv.ui.util.localizedGenreLabel
import com.nuvio.tv.ui.util.recompositionHighlighter
import com.nuvio.tv.domain.model.PLACEHOLDER_IMAGE_URL
import com.nuvio.tv.ui.util.rememberLongPressKeyTracker
import kotlinx.coroutines.delay

private const val BACKDROP_ASPECT_RATIO = 16f / 9f
private const val TRAILER_PREVIEW_REQUEST_FOCUS_DEBOUNCE_MS = 140L
private val YEAR_REGEX = Regex("""\b(19|20)\d{2}\b""")
private val YEAR_RANGE_REGEX = Regex("""^((19|20)\d{2})\s*[-–]\s*((19|20)\d{2})?$""")

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContentCard(
    item: MetaPreview,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    posterCardStyle: PosterCardStyle = PosterCardDefaults.Style,
    showLabels: Boolean = true,
    showImdbRatings: Boolean = true,
    placeholderShimmerOffsetState: State<Float>? = null,
    focusedPosterBackdropExpandEnabled: Boolean = false,
    focusedPosterBackdropExpandDelaySeconds: Int = 3,
    focusedPosterBackdropTrailerEnabled: Boolean = false,
    focusedPosterBackdropTrailerMuted: Boolean = true,
    trailerPreviewUrl: String? = null,
    trailerPreviewAudioUrl: String? = null,
    onRequestTrailerPreview: (MetaPreview) -> Unit = {},
    isWatched: Boolean = false,
    onFocus: (MetaPreview) -> Unit = {},
    onBackdropExpandedChanged: ((Boolean) -> Unit)? = null,
    expandedDownFocusRequester: FocusRequester? = null,
    expandedUpFocusRequester: FocusRequester? = null,
    onLongPress: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val cardShape = remember(posterCardStyle.cornerRadius) { RoundedCornerShape(posterCardStyle.cornerRadius) }
    val cardDepthStyle = LocalCardDepthStyle.current
    val globalLandscape = LocalLandscapePosterMode.current
    val effectivePosterShape = if (globalLandscape) PosterShape.LANDSCAPE else item.posterShape
    val baseCardWidth = when (effectivePosterShape) {
        PosterShape.POSTER -> posterCardStyle.width
        PosterShape.LANDSCAPE -> posterCardStyle.height
        PosterShape.SQUARE -> 170.dp
    }
    val baseCardHeight = when (effectivePosterShape) {
        PosterShape.POSTER -> posterCardStyle.height
        PosterShape.LANDSCAPE -> posterCardStyle.height / PosterShape.LANDSCAPE.aspectRatio()
        PosterShape.SQUARE -> 170.dp
    }
    val expandedCardWidth = if (globalLandscape) baseCardWidth else baseCardHeight * BACKDROP_ASPECT_RATIO
    val effectiveExpandEnabled = focusedPosterBackdropExpandEnabled

    var isFocused by remember { mutableStateOf(false) }
    var longPressTriggered by remember { mutableStateOf(false) }
    val longPressKeyTracker = rememberLongPressKeyTracker()
    var interactionNonce by remember { mutableIntStateOf(0) }
    var isBackdropExpanded by remember { mutableStateOf(false) }
    var trailerFirstFrameRendered by remember(trailerPreviewUrl) { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(isBackdropExpanded) {
        onBackdropExpandedChanged?.invoke(isBackdropExpanded)
    }
    val needsFocusState = true
    val lastFocusedRef = remember { booleanArrayOf(false) }

    val isPlaceholderItem = item.poster == PLACEHOLDER_IMAGE_URL

    if (effectiveExpandEnabled && !isPlaceholderItem) {
        LaunchedEffect(
            focusedPosterBackdropExpandDelaySeconds,
            isFocused,
            interactionNonce,
            item.id
        ) {
            if (!isFocused) {
                isBackdropExpanded = false
                return@LaunchedEffect
            }

            val delaySeconds = focusedPosterBackdropExpandDelaySeconds.coerceAtLeast(0)

            isBackdropExpanded = false
            val backdropDelayMs = if (delaySeconds == 0) 370L else delaySeconds * 1000L
            delay(backdropDelayMs)
            if (isFocused && effectiveExpandEnabled &&
                lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            ) {
                isBackdropExpanded = true
            }
        }
    }

    val animatedCardWidth = when {
        !effectiveExpandEnabled -> baseCardWidth
        !isFocused && !isBackdropExpanded -> baseCardWidth
        else -> {
            val targetCardWidth = if (isBackdropExpanded) expandedCardWidth else baseCardWidth
            val width by animateDpAsState(targetValue =
