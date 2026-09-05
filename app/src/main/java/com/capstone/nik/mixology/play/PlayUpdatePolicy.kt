package com.capstone.nik.mixology.play

data class PlayUpdateSnapshot(
    val available: Boolean,
    val inProgress: Boolean,
    val downloaded: Boolean,
    val flexibleAllowed: Boolean,
    val immediateAllowed: Boolean,
    val priority: Int = 0,
    val stalenessDays: Int? = null,
)

enum class PlayUpdateAction {
    FLEXIBLE,
    IMMEDIATE,
    COMPLETE,
}

object PlayUpdatePolicy {
    const val IMMEDIATE_PRIORITY = 4
    const val IMMEDIATE_STALENESS_DAYS = 14

    fun action(snapshot: PlayUpdateSnapshot): PlayUpdateAction? {
        if (snapshot.downloaded) return PlayUpdateAction.COMPLETE
        if (!snapshot.available && !snapshot.inProgress) return null
        val forceImmediate = snapshot.priority >= IMMEDIATE_PRIORITY ||
            (snapshot.stalenessDays ?: 0) >= IMMEDIATE_STALENESS_DAYS
        return when {
            snapshot.inProgress && snapshot.immediateAllowed -> PlayUpdateAction.IMMEDIATE
            forceImmediate && snapshot.immediateAllowed -> PlayUpdateAction.IMMEDIATE
            snapshot.flexibleAllowed -> PlayUpdateAction.FLEXIBLE
            snapshot.immediateAllowed -> PlayUpdateAction.IMMEDIATE
            else -> null
        }
    }
}
