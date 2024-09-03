package pl.lambada.songsync.ui.screens.about.components

import pl.lambada.songsync.R

@Suppress("SpellCheckingInspection")
enum class Contributor(
    val devName: String,
    val contributionLevel: ContributionLevel,
    val github: String? = null,
    val telegram: String? = null
) {
    LAMBADA10(
        "Lambada10", ContributionLevel.LEAD_DEVELOPER,
        github = "https://github.com/Lambada10", telegram = "https://t.me/Lambada10"
    ),
    NIFT4(
        "Nick", ContributionLevel.DEVELOPER,
        github = "https://github.com/nift4", telegram = "https://t.me/nift4"
    ),
    BOBBYESP(
        "BobbyESP", ContributionLevel.DEVELOPER,
        github = "https://github.com/BobbyESP"
    ),
    PXEEMO(
        "Pxeemo", ContributionLevel.CONTRIBUTOR,
        github = "https://github.com/pxeemo"
    ),
    AKANETAN(
        "AkaneTan", ContributionLevel.CONTRIBUTOR,
        github = "https://github.com/AkaneTan"
    ),
    NXOIM(
        devName = "nxoim", ContributionLevel.CONTRIBUTOR,
        github = "https://github.com/nxoim"
    )
}

/**
 * Defines the contribution level of a contributor.
 */
enum class ContributionLevel(val stringResource: Int) {
    CONTRIBUTOR(R.string.contributor),
    DEVELOPER(R.string.developer),
    LEAD_DEVELOPER(R.string.lead_developer)
}

