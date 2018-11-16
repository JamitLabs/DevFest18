package com.jamitlabs.devfest18demo.customarcomponents

import android.app.Application
import android.graphics.Point
import android.net.Uri
import android.util.Size
import androidx.lifecycle.AndroidViewModel
import com.google.ar.core.Anchor
import timber.log.Timber
import java.lang.Exception

open class CustomArViewModel(application: Application): AndroidViewModel(application) {

    interface NotifyListener {
        fun hostAnchor(anchor: Anchor, objectFile: String): Anchor?
        fun requestSharedObject(modelUri: Uri, point: Point, anchorResponse: ((Anchor) -> Unit)?)
        fun clearScene()
        fun viewSize(): Size
    }

    var listener: NotifyListener? = null

    fun notifyHostAnchor(anchor: Anchor, objectFile: String): Anchor? {
        return listener?.hostAnchor(anchor, objectFile)
    }

    fun requestSharedObject(modelFileName: String, point: Point, anchorResponse: ((Anchor) -> Unit)? = null) {
        try {
            val modelUri = Uri.parse(modelFileName)
            listener?.requestSharedObject(modelUri, point, anchorResponse)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun requestNeedViewSize(): Size? = listener?.viewSize()
}