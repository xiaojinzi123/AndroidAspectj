package com.xiaojinzi.aspectj.lib.resource

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String
) : Parcelable, java.io.Serializable
