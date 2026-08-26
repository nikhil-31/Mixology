package com.capstone.nik.mixology.ui.components

import com.capstone.nik.mixology.R
import org.junit.Assert.assertEquals
import org.junit.Test

class BannerAdTest {

    @Test
    fun debug_usesGoogleTestBanner() {
        assertEquals(R.string.banner_test, bannerAdUnitRes(debug = true, details = false))
        assertEquals(R.string.banner_test, bannerAdUnitRes(debug = true, details = true))
    }

    @Test
    fun release_usesProductionBanners() {
        assertEquals(R.string.banner_ad_unit_id, bannerAdUnitRes(debug = false, details = false))
        assertEquals(R.string.banner_ad_unit_id_details, bannerAdUnitRes(debug = false, details = true))
    }
}
