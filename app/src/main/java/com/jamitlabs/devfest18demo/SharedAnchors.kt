package com.jamitlabs.devfest18demo

import android.content.Context
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.Anchor.CloudAnchorState.*
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.jamitlabs.devfest18demo.customarcomponents.AnchorInfo
import com.jamitlabs.devfest18demo.customarcomponents.ObjectAnchor
import java.util.concurrent.ConcurrentHashMap

typealias Provider<T> = () -> T

class SharedAnchors(private val context: Context,
                    val provideSession: Provider<Session?>): Scene.OnUpdateListener {

    private var anchorsToHost = ConcurrentIntHashMap<ObjectAnchor>()
    private var anchorsToResolve = ConcurrentIntHashMap<ObjectAnchor>()
    private var pendingAnchors = ConcurrentIntHashMap<AnchorInfo>()
    private var anchorReceivedBlock = { _: Anchor, _: String -> }

    private var trackingState = TrackingState.TRACKING
    private val storageManager = StorageManager(context).apply {
        onAnchorReceived { anchorInfo ->
            pendingAnchors.add(anchorInfo)
        }
    }

    fun host(session: Session, anchor: Anchor, objectName: String): Anchor {
        val hostingAnchor = session.hostCloudAnchor(anchor)
        val objectAnchor = ObjectAnchor(hostingAnchor, objectName)
        anchorsToHost.add(objectAnchor)

        return anchor
    }

    fun onAnchorReceived(block: (anchor: Anchor, objectName: String) -> Unit) {
        anchorReceivedBlock = block
    }

    override fun onUpdate(frameTime: FrameTime?) {
        val session = provideSession() ?: return

        trackingState = session.update().camera.trackingState
        if (trackingState != TrackingState.TRACKING) {
            return
        }

        pendingAnchors.forEach { resolveOnNextTrackingState(it.value) }
        anchorsToHost.forEach { checkHostAnchorState(it.value) }
        anchorsToResolve.forEach { checkResolveAnchorState(it.value) }
    }

    private fun resolveOnNextTrackingState(anchorInfo: AnchorInfo) {

            val session = provideSession() ?: return
            val anchor = session.resolveCloudAnchor(anchorInfo.id)
            val objectAnchor = ObjectAnchor(anchor, anchorInfo.objectFile)

            anchorsToResolve.add(objectAnchor)
            pendingAnchors.remove(anchorInfo)
    }

    private fun checkResolveAnchorState(objectAnchor: ObjectAnchor) {
        when {
            objectAnchor.anchor.cloudAnchorState == SUCCESS -> {
                anchorReceivedBlock(objectAnchor.anchor, objectAnchor.objectName)
                anchorsToResolve.remove(objectAnchor)
            }

            objectAnchor.anchor.cloudAnchorState == NONE -> {
                Toast.makeText(context, "NONE State", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkHostAnchorState(objectAnchor: ObjectAnchor) {
        when {
            objectAnchor.anchor.cloudAnchorState == SUCCESS -> {

                storageManager.storeForNextShortCode(objectAnchor.info) { shortCode ->
                    val toastMsg = "Short code: $shortCode - ${objectAnchor.info.id}"
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                }

                anchorsToHost.remove(objectAnchor)
                Toast.makeText(context, "Anchor successful hosted", Toast.LENGTH_SHORT).show()
            }

            objectAnchor.anchor.cloudAnchorState == NONE -> {
                Toast.makeText(context, "NONE State", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class ConcurrentIntHashMap<V>: ConcurrentHashMap<Int, V>() {

    fun add(value: V) {
        var maxIndex = size

        for (entry in this) {
            if (entry.key > maxIndex) {
                maxIndex = entry.key.inc()
            }
        }

        put(maxIndex, value)
    }

    fun remove(value: V) {
        var keyToRemove: Int? = null

        for (entry in this) {
            if (entry.value == value) {
                keyToRemove = entry.key
                break
            }
        }

        keyToRemove?.let(::remove)
    }
}