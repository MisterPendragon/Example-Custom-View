package com.example.examplecustomview

import android.content.res.Resources

fun Float.dp2px(): Float {
    return this * Resources.getSystem().displayMetrics.density
}

fun Float.sp2px(): Float {
    return this * Resources.getSystem().displayMetrics.scaledDensity
}

