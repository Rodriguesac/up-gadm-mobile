package com.rodriguesacai.gadm.data

data class AdminCategory(
    val id: String,
    val name: String,
    val description: String,
    val active: Boolean,
    val order: Int,
    val imageUrl: String,
    val icon: String
)

data class AdminCoupon(
    val id: String,
    val code: String,
    val description: String,
    val type: String,
    val value: Double,
    val minValue: Double,
    val active: Boolean,
    val customerUid: String,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val uses: Int,
    val maxUses: Int,
    val startsAt: String,
    val endsAt: String
) {
    val targeted: Boolean
        get() = customerUid.isNotBlank() || customerEmail.isNotBlank() || customerPhone.isNotBlank()
}

data class AdminBanner(
    val id: String,
    val title: String,
    val imageUrl: String,
    val actionType: String,
    val active: Boolean,
    val order: Int
)

data class AdminNotice(
    val id: String,
    val title: String,
    val body: String,
    val imageUrl: String,
    val active: Boolean,
    val audience: String,
    val severity: String,
    val order: Int
)

data class AdminCatalogSnapshot(
    val categories: List<AdminCategory> = emptyList(),
    val coupons: List<AdminCoupon> = emptyList(),
    val banners: List<AdminBanner> = emptyList(),
    val notices: List<AdminNotice> = emptyList()
)

data class CouponDraft(
    val id: String = "",
    val code: String,
    val description: String = "",
    val type: String = "percentual",
    val value: Double,
    val minValue: Double = 0.0,
    val active: Boolean = true,
    val customerUid: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = ""
)
