package com.capstone.nik.mixology.ui.components

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.capstone.nik.mixology.BuildConfig
import com.capstone.nik.mixology.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

internal fun bannerAdUnitRes(debug: Boolean = BuildConfig.DEBUG, details: Boolean): Int = when {
    debug -> R.string.banner_test
    details -> R.string.banner_ad_unit_id_details
    else -> R.string.banner_ad_unit_id
}

@Composable
fun BannerAd(
    details: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) return
    val adUnitId = stringResource(bannerAdUnitRes(details = details))
    val context = LocalContext.current
    key(adUnitId) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .testTag("banner_ad"),
            factory = {
                AdView(context).apply {
                    val density = resources.displayMetrics.density
                    val adWidth = (resources.displayMetrics.widthPixels / density).toInt().coerceAtLeast(320)
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth))
                    this.adUnitId = adUnitId
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                    )
                    contentDescription = context.getString(R.string.content_desc_banner_ad)
                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { it.destroy() },
        )
    }
}
