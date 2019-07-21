package com.gooldy.georeminder.data

import com.google.android.gms.maps.model.Circle
import java.util.*

class AreaHolder private constructor() {

    val data: WeakHashMap<String, Circle> = WeakHashMap()

    private object Holder { val INSTANCE = AreaHolder() }

    companion object {
        val instance: AreaHolder by lazy { Holder.INSTANCE }
    }

}