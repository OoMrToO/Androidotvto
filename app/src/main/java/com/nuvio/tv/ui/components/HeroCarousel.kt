package com.nuvio.tv.ui.components

import com.nuvio.tv.ui.theme.NuvioTheme

import android.graphics.ColorSpace
import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nuvio.tv.ui.util.StableList
import com.nuvio.tv.ui.util.recompositionHighlighter
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.colorSpace
import coil3.request.crossfade
import com.nuvio.tv.domain.model.MetaPreview
import com.nuvio.tv.ui.util.contentTextDirection
import com.nuvio.tv.ui.util.formatHeroRuntime
import com.nuvio.tv.ui.util.LocalRecompositionHighlighterEnabled
import com.nuvio.tv.ui.util.localizedContentType
import com.nuvio.tv.ui.util.localizedGenreLabel
import kotlinx.coroutines.delay

private const val AUTO_ADVANCE_INTERVAL_MS = 10000L
private val YEAR_REGEX = Regex("""\b\d{4}\b""")

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroCarousel(
    items: StableList<MetaPreview>,
    onItemClick: (MetaPreview) -> Unit,
    onItemFocus: (MetaPreview) -> Unit = {},
    onActiveItemChanged: (MetaPreview) -> Unit = {},
    focusRequester: FocusRequester? = null,
    showImdbRatings: Boolean = true,
    showBackdrop: Boolean = true,
    fullWidth: Dp = Dp.Unspecified,
    initialActiveIndex: Int = 0,
    mdbListShowOnHero: Boolean = false,
    mdbListRatingOrder: List<String> = com.nuvio.tv.domain.model.MDBListSettings.DEFAULT_RATING_ORDER,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val currentOnItemClick by rememberUpdatedState(onItemClick)
    val currentOnItemFocus by rememberUpdatedState(onItemFocus)
    val currentOnActiveItemChanged by rememberUpdatedState(onActiveItemChanged)
    var activeIndex by remember { mutableIntStateOf(initialActiveIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))) }
    var isFocused by remember { mutableStateOf(false) }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LaunchedEffect(activeIndex, isFocused) {
        if (!isFocused) return@LaunchedEffect
        items.getOrNull(activeIndex)?.let { currentOnItemFocus(it) }
    }

    LaunchedEffect(activeIndex, items) {
        items.getOrNull(activeIndex)?.let { currentOnActiveItemChanged(it) }
    }

    LaunchedEffect(isFocused, items.size) {
        if (items.size <= 1) return@LaunchedEffect
        delay(AUTO_ADVANCE_INTERVAL_MS * 2)
        while (true) {
            delay(AUTO_ADVANCE_INTERVAL_MS)
            if (!isFocused) {
                activeIndex = (activeIndex + 1) % items.size
            }
        }
    }

    Box(
        modifier = modifier
            .then(
                if (fullWidth != Dp.Unspecified)
                    Modifier.requiredWidth(fullWidth)
                else
                    Modifier.fillMaxWidth()
            )
            .height(420.dp) // Apple TV tarzı biraz daha görkemli yükseklik
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged {
                isFocused = it.hasFocus || it.isFocused
            }
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionLeft -> {
                            if (isRtl) {
                                if (activeIndex < items.size - 1) { activeIndex++; true } else false
                            } else {
                                if (activeIndex > 0) { activeIndex--; true } else false
                            }
                        }
                        Key.DirectionRight -> {
                            if (isRtl) {
                                if (activeIndex > 0) { activeIndex--; true } else false
                            } else {
                                if (activeIndex < items.size - 1) { activeIndex++; true } else false
                            }
                        }
                        else -> false
                    }
                } else if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) {
                    currentOnItemClick(items[activeIndex])
                    true
                } else {
                    false
                }
            }
    ) {
        Crossfade(
            targetState = activeIndex,
            animationSpec = tween(400), // Daha akıcı sinematik geçiş
            label = "heroSlide"
        ) { index ->
            val item = items.getOrNull(index) ?: return@Crossfade
            HeroCarouselSlide(
                item = item,
                showImdbRatings = showImdbRatings,
                showBackdrop = showBackdrop,
                mdbListShowOnHero = mdbListShowOnHero,
                mdbListRatingOrder = mdbListRatingOrder
            )
        }

        val focusRingBrush = NuvioTheme.focusRing.brush()
        val dotColorInactive = remember { Color.White.copy(alpha = 0.25f) }
        val dotShape = remember { RoundedCornerShape(4.dp) }
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd) // Apple TV tarzı sağ alt hizalama
                .padding(end = NuvioTheme.spacing.xxxl, bottom = NuvioTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(NuvioTheme.spacing.sm)
        ) {
            repeat(items.size) { index ->
                val isActive = index == activeIndex
                val useGradient = isActive
                val dotColor = when {
                    isActive -> null
                    else -> dotColorInactive
                }
                val dotWidth = when {
                    isActive -> NuvioTheme.spacing.xxl
                    else -> NuvioTheme.spacing.md
                }
                val dotHeight = if (isActive) 6.dp else NuvioTheme.spacing.xs
                
                Box(
                    modifier = Modifier
                        .size(width = dotWidth, height = dotHeight)
                        .clip(dotShape)
                        .then(
                            if (useGradient) Modifier.background(focusRingBrush)
                            else Modifier.background(dotColor!!)
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HeroCarouselSlide(
    item: MetaPreview,
    showImdbRatings: Boolean,
    showBackdrop: Boolean,
    mdbListShowOnHero: Boolean = false,
    mdbListRatingOrder: List<String> = com.nuvio.tv.domain.model.MDBListSettings.DEFAULT_RATING_ORDER
) {
    val highlighterEnabled = LocalRecompositionHighlighterEnabled.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val logoRequestWidthPx = remember(density) {
        with(density) { 240.dp.roundToPx() }.coerceAtLeast(1)
    }
    val logoRequestHeightPx = remember(density) { with(density) { 110.dp.roundToPx() }.coerceAtLeast(1) }

    val logoModel = remember(context, item.logo, logoRequestWidthPx, logoRequestHeightPx) {
        item.logo?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(true)
                .size(width = logoRequestWidthPx, height = logoRequestHeightPx)
                .build()
        }
    }
    var logoLoadFailed by remember(item.logo) { mutableStateOf(false) }
    val showLogo = !item.logo.isNullOrBlank() && !logoLoadFailed
    val contentTypeText = remember(context, item.apiType) {
        localizedContentType(context, item.apiType).takeIf { it.isNotBlank() }
    }
    val primaryGenreText = remember(context, item.genres) {
        item.genres.firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?.let { localizedGenreLabel(context, it) }
    }
    val runtimeText = remember(item.runtime) { formatHeroRuntime(item.runtime) }
    val releaseYear = remember(item.releaseInfo) {
        item.releaseInfo?.let { releaseInfo ->
            YEAR_REGEX.find(releaseInfo)?.value ?: releaseInfo.split("-").firstOrNull()
        }?.trim()?.takeIf { it.isNotEmpty() }
    }
    val leadingMetaText = remember(contentTypeText, primaryGenreText) {
        listOfNotNull(contentTypeText, primaryGenreText).joinToString(separator = " • ")
    }
    val trailingMetadata = remember(runtimeText, releaseYear) {
        listOfNotNull(runtimeText, releaseYear)
    }
    val ratingText = remember(item.imdbRating, showImdbRatings) {
        item.imdbRating
            ?.takeIf { showImdbRatings }
            ?.let { String.format("%.1f", it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (highlighterEnabled) Modifier.recompositionHighlighter() else Modifier)
    ) {
        if (showBackdrop) {
            HeroCarouselBackdrop(
                item = item,
                fullPage = false,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = NuvioTheme.spacing.xxxl, bottom = NuvioTheme.spacing.xxxl, end = NuvioTheme.spacing.xxxl)
                .fillMaxWidth(0.48f),
            verticalArrangement = Arrangement.spacedBy(NuvioTheme.spacing.sm)
        ) {
            if (showLogo) {
                AsyncImage(
                    model = logoModel,
                    contentDescription = item.name,
                    onError = { logoLoadFailed = true },
                    modifier = Modifier
                        .height(110.dp)
                        .widthIn(min = 120.dp, max = 240.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.CenterStart
                )
            } else {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = NuvioTheme.colors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (leadingMetaText.isNotBlank() || trailingMetadata.isNotEmpty() || ratingText != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NuvioTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hasTrailingMeta = trailingMetadata.isNotEmpty() || ratingText != null
                    if (leadingMetaText.isNotBlank()) {
                        Text(
                            text = leadingMetaText,
                            style = MaterialTheme.typography.labelMedium,
                            color = NuvioTheme.colors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (hasTrailingMeta) {
                                Modifier.weight(1f, fill = false)
                            } else {
                                Modifier
                            }
                        )
                    }
                    if (hasTrailingMeta) {
                        if (leadingMetaText.isNotBlank()) HeroCarouselMetaDivider()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(NuvioTheme.spacing.sm)
                        ) {
                            trailingMetadata.forEachIndexed { index, value ->
                                if (index > 0) HeroCarouselMetaDivider()
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NuvioTheme.colors.TextSecondary,
                                    maxLines = 1
                                )
                            }
                            if (ratingText != null) {
                                val mdbRatings = item.mdbListRatings
                                if (mdbListShowOnHero && mdbRatings != null && !mdbRatings.isEmpty()) {
                                } else {
                                    if (trailingMetadata.isNotEmpty()) HeroCarouselMetaDivider()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(NuvioTheme.spacing.xs)
                                    ) {
                                        ImdbRatingSourceLabel(
                                            logoModifier = Modifier.size(NuvioTheme.spacing.xl),
                                            textStyle = MaterialTheme.typography.labelMedium,
                                            textColor = NuvioTheme.colors.TextSecondary
                                        )
                                        Text(
                                            text = ratingText,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = NuvioTheme.colors.TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (mdbListShowOnHero && item.mdbListRatings != null && !item.mdbListRatings.isEmpty()) {
                MDBListRatingsRow(
                    ratings = item.mdbListRatings,
                    order = mdbListRatingOrder,
                    maxItems = 6
                )
            }

            item.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDirection = description.contentTextDirection()
                    ),
                    color = NuvioTheme.colors.TextPrimary.copy(alpha = 0.9f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HeroCarouselMetaDivider() {
    Box(
