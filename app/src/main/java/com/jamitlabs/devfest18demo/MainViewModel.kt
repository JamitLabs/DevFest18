package com.jamitlabs.devfest18demo

import android.app.Application
import android.graphics.Point
import android.view.View
import android.widget.Toast
import com.jamitlabs.devfest18demo.customarcomponents.CustomArViewModel

class MainViewModel(application: Application): CustomArViewModel(application) {

    companion object {
        const val HEART = "Heart.sfb"
        const val ANDROID = "andy.sfb"
        const val JAMIT_LABS = "jamitlabs.sfb"
    }

    /**
     * The click listener for the add heart object fab.
     */
    val heartClick = View.OnClickListener {
        requestSharedObject(HEART, getCenter()) { anchor ->
            notifyHostAnchor(anchor, HEART)
            showStartHosting()
        }
    }

    /**
     * The click listener for the add android object fab.
     */
    val androidClick = View.OnClickListener {
        requestSharedObject(ANDROID, getCenter()) { anchor ->
            notifyHostAnchor(anchor, ANDROID)
            showStartHosting()
        }
    }

    /**
     * The click listener for the add jamit labs logo object fab.
     */
    val jamitLabsClick = View.OnClickListener {
        requestSharedObject(JAMIT_LABS, getCenter()) { anchor ->
            notifyHostAnchor(anchor, JAMIT_LABS)
            showStartHosting()
        }
    }

    /**
     * Show Toast an start hosting the cloud anchor.
     */
    private fun showStartHosting() {
        Toast.makeText(getApplication(), "Start hosting anchor", Toast.LENGTH_SHORT).show()
    }

    /**
     * Retrieves the center of the ar scene view.
     */
    private fun getCenter(): Point {
        val size = requestNeedViewSize() ?: return Point(0,0)

        return Point(size.width / 2, size.height / 2)
    }
}