package com.nuvio.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

@Immutable
data class NuvioSurfaceColors(
    val background: Color,
    val raised: Color,
    val card: Color,
    val default: Color,
    val variant: Color,
    val panel: Color,
    val overlay: Color,
    val field: Color,
    val menu: Color,
    val modal: Color,
    val playerOverlay: Color,
    val divider: Color
)

@Immutable
data class NuvioTextColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val disabled: Color,
    val inverse: Color,
    val onAccent: Color,
    val onOverlay: Color,
    val metadata: Color
)

@Immutable
data class NuvioFocusColors(
    val ring: Color,
    val background: Color,
    val content: Color,
    val accent: Color,
    val scrim: Color
)

@Immutable
data class NuvioSelectionColors(
    val background: Color,
    val foreground: Color,
    val border: Color,
    val mutedBackground: Color,
    val mutedForeground: Color
)

@Immutable
data class NuvioMediaColors(
    val heroScrim: Color,
    val imageScrim: Color,
    val posterFallback: Color,
    val videoControlsScrim: Color,
    val glassPanelTop: Color,
    val glassPanelMiddle: Color,
    val glassPanelBottom: Color,
    val glow: Color
)

@Immutable
data class NuvioStatusColors(
    val rating: Color,
    val error: Color,
    val warning: Color,
    val success: Color,
    val info: Color,
    val watched: Color,
    val unwatched: Color,
    val cached: Color,
    val torrent: Color,
    val premium: Color
)

@Immutable
data class NuvioDisabledColors(
    val container: Color,
    val content: Color,
    val border: Color,
    val overlay: Color
)

@Immutable
data class NuvioSourceColors(
    val trakt: Color,
    val tmdb: Color,
    val imdb: Color,
    val mdblist: Color
)

@Immutable
data class NuvioContrastPair(
    val foreground: Color,
    val background: Color
)

class NuvioColorScheme(
    palette: ThemeColorPalette,
    amoledMode: Boolean = false,
    amoledSurfacesMode: Boolean = false
) {
    // Apple TV + Netflix Karışımı Sinematik Saf Siyah ve Derin Gri Tonları
    private val pureBlack = Color(0xFF000000)
    private val cinematicSurface = Color(0xFF121216) // Kart ve yüzeyler için lüks koyu gri
    private val cinematicCard = Color(0xFF18181C)    // Kart arka planı

    val Background = pureBlack
    val BackgroundElevated = cinematicSurface
    val BackgroundCard = cinematicCard
    val Surface = cinematicSurface
    val SurfaceVariant = Color(0xFF1F1F24)
    val Panel = cinematicSurface
    val Overlay = palette.overlay
    val Field = Color(0xFF1A1A1E)
    val Menu = cinematicSurface
    val Modal = cinematicSurface
    val PlayerOverlay = palette.playerOverlay
    val Divider = Color(0xFF2C2C35)

    val Primary = NuvioPrimitives.neutral500
    val PrimaryVariant = NuvioPrimitives.neutral650
    val OnPrimary = NuvioPrimitives.white
    val Secondary = palette.secondary
    val SecondaryVariant = palette.secondaryVariant
    val OnSecondary = palette.onSecondary
    val OnSecondaryVariant = palette.onSecondaryVariant

    val TextPrimary = NuvioPrimitives.white
    val TextSecondary = Color(0xFFA0A0AB) // Apple tarzı daha okunabilir yumuşak gri
    val TextTertiary = NuvioPrimitives.neutral600
    val TextDisabled = NuvioPrimitives.neutral700
    val TextInverse = NuvioPrimitives.neutral925

    val FocusRing = palette.focusRing
    val FocusBackground = palette.focusBackground
    val FocusContent = NuvioPrimitives.white
    val FocusScrim = NuvioPrimitives.black.copy(alpha = 0.32f)

    val Rating = NuvioPrimitives.rating
    val Error = NuvioPrimitives.error
    val Warning = NuvioPrimitives.warning
    val Success = NuvioPrimitives.success
    val Info = NuvioPrimitives.info
    val Watched = NuvioPrimitives.success
    val Unwatched = NuvioPrimitives.neutral600
    val Cached = NuvioPrimitives.blue300
    val Torrent = NuvioPrimitives.torrent
    val Premium = NuvioPrimitives.premium

    val Border = Color(0xFF2C2C35)
    val BorderFocused = FocusRing
    val BorderMuted = Color(0xFF2C2C35).copy(alpha = 0.58f)

    val Scrim = NuvioPrimitives.black.copy(alpha = 0.75f)
    val ImageScrim = NuvioPrimitives.black.copy(alpha = 0.65f)
    val VideoControlsScrim = NuvioPrimitives.black.copy(alpha = 0.82f)
    val PosterFallback = BackgroundCard

    val DisabledContainer = SurfaceVariant.copy(alpha = 0.42f)
    val DisabledContent = TextDisabled
    val DisabledBorder = Border.copy(alpha = 0.48f)
    val DisabledOverlay = NuvioPrimitives.black.copy(alpha = 0.42f)

    val surfaces = NuvioSurfaceColors(
        background = Background,
        raised = BackgroundElevated,
        card = BackgroundCard,
        default = Surface,
        variant = SurfaceVariant,
        panel = Panel,
        overlay = Overlay,
        field = Field,
        menu = Menu,
        modal = Modal,
        playerOverlay = PlayerOverlay,
        divider = Divider
    )

    val text = NuvioTextColors(
        primary = TextPrimary,
        secondary = TextSecondary,
        tertiary = TextTertiary,
        disabled = TextDisabled,
        inverse = TextInverse,
        onAccent = OnSecondary,
        onOverlay = NuvioPrimitives.white,
        metadata = TextSecondary
    )

    val focus = NuvioFocusColors(
        ring = FocusRing,
        background = FocusBackground,
        content = FocusContent,
        accent = Secondary,
        scrim = FocusScrim
    )

    val selection = NuvioSelectionColors(
        background = Secondary,
        foreground = OnSecondary,
        border = SecondaryVariant,
        mutedBackground = FocusBackground,
        mutedForeground = TextPrimary
    )

    val media = NuvioMediaColors(
        heroScrim = Scrim,
        imageScrim = ImageScrim,
        posterFallback = PosterFallback,
        videoControlsScrim = VideoControlsScrim,
        glassPanelTop = Color(0xD62A2E35),
        glassPanelMiddle = Color(0xCC22252B),
        glassPanelBottom = Color(0xC61B1E23),
        glow = FocusRing.copy(alpha = 0.32f)
    )

    val status = NuvioStatusColors(
        rating = Rating,
        error = Error,
        warning = Warning,
        success = Success,
        info = Info,
        watched = Watched,
        unwatched = Unwatched,
        cached = Cached,
        torrent = Torrent,
        premium = Premium
    )

    val disabled = NuvioDisabledColors(
        container = DisabledContainer,
        content = DisabledContent,
        border = DisabledBorder,
        overlay = DisabledOverlay
    )

    val source = NuvioSourceColors(
        trakt = NuvioPrimitives.trakt,
        tmdb = NuvioPrimitives.tmdb,
        imdb = NuvioPrimitives.imdb,
        mdblist = NuvioPrimitives.mdblist
    )

    val contrastPairs = listOf(
        NuvioContrastPair(TextPrimary, Background),
        NuvioContrastPair(TextPrimary, BackgroundCard),
        NuvioContrastPair(TextSecondary, Background),
        NuvioContrastPair(OnSecondary, Secondary),
        NuvioContrastPair(FocusContent, FocusBackground),
        NuvioContrastPair(NuvioPrimitives.white, PlayerOverlay)
    )
}

object NuvioColors {
    val Background: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.Background

    val BackgroundElevated: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.BackgroundElevated

    val BackgroundCard: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.BackgroundCard

    val Surface: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.Surface

    val SurfaceVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.SurfaceVariant

    val Primary = NuvioPrimitives.neutral500
    val PrimaryVariant = NuvioPrimitives.neutral650
    val OnPrimary = NuvioPrimitives.white
    val TextPrimary = NuvioPrimitives.white
    val TextSecondary = Color(0xFFA0A0AB)
    val TextTertiary = NuvioPrimitives.neutral600
    val TextDisabled = NuvioPrimitives.neutral700
    val Rating = NuvioPrimitives.rating
    val Error = NuvioPrimitives.error
    val Success = NuvioPrimitives.success
    val Warning = NuvioPrimitives.warning
    val Info = NuvioPrimitives.info
    val Border = Color(0xFF2C2C35)

    val Secondary: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.Secondary

    val SecondaryVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.SecondaryVariant

    val OnSecondary: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.OnSecondary

    val OnSecondaryVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.OnSecondaryVariant

    val FocusRing: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.FocusRing

    val FocusBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.FocusBackground

    val BorderFocused: Color
        @Composable
        @ReadOnlyComposable
        get() = NuvioTheme.colors.BorderFocused
}
