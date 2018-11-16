package com.jamitlabs.devfest18demo.customarcomponents

import android.app.AlertDialog
import android.graphics.Point
import android.net.Uri
import android.util.Size
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.Sun
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.jamitlabs.devfest18demo.SharedAnchors
import java.util.function.Consumer


open class CustomArFragment: ArFragment(), CustomArViewModel.NotifyListener {
    private var customViewModel: CustomArViewModel? = null
    private var sharedAnchors: SharedAnchors? = null

    override fun onResume() {
        super.onResume()

        context?.let {
            sharedAnchors = SharedAnchors(it) { arSceneView.session }
            arSceneView.scene.addOnUpdateListener(sharedAnchors)
            sharedAnchors?.onAnchorReceived { anchor, objectName ->
                placeObject(objectName, anchor)
            }
        }
    }

    /**
     * Hosts the given anchor and returns the hosting anchor. Otherwise returns null.
     */
    override fun hostAnchor(anchor: Anchor, objectFile: String): Anchor? {
        return sharedAnchors?.host(arSceneView.session, anchor, objectFile)
    }

    /**
     * Request to host a 3D object at the given view position. If allowed the [anchorResponse]
     * block will be called.
     */
    override fun requestSharedObject(modelUri: Uri, point: Point, anchorResponse: ((Anchor) -> Unit)?) {
        val anchor = createAnchorFor(point) ?: return
        anchorResponse?.invoke(anchor)
    }

    /**
     * Clears the scene.
     */
    override fun clearScene() {
        ArrayList(arSceneView.scene.children).forEach {

            if (it is AnchorNode) {
                it.anchor.detach()
            }

            if (it !is Camera && it !is Sun) {
                it.setParent(null)
            }

        }
    }

    /**
     * Retrieves the size of the ar scene view.
     *
     * @return the size
     */
    override fun viewSize(): Size {
        return Size(arSceneView.width, arSceneView.height)
    }

    /**
     * Sets the view model.
     */
    fun setViewModel(viewModelCustom: CustomArViewModel) {
        this.customViewModel = viewModelCustom
        this.customViewModel?.listener = this
    }

    /**
     * Adds a object to the ar scene view at the given anchor.
     */
    private fun placeObject(objectFile: String, anchor: Anchor) {
        val modelUri = Uri.parse(objectFile)

        val onAccept = Consumer<ModelRenderable> {
            val (anchorNode, _) = createNodeFor(anchor, it)
            addNodeToScene(anchorNode)
        }

        ModelRenderable.builder()
                .setSource(context, modelUri).build()
                .thenAccept(onAccept)
                .exceptionally {
                    AlertDialog.Builder(context)
                            .setMessage(it.message)
                            .setTitle("error!")
                            .create()
                            .show()

                    return@exceptionally null
                }
    }

    /**
     * Converts the given 2D [point] into a 3D anchor.
     *
     * @return the anchor if point hits a plane, otherwise null.
     */
    private fun createAnchorFor(point: Point): Anchor? {
        val frame = arSceneView.arFrame
        val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())

        for (hit in hits) {
            val plane = hit.trackable as? Plane ?: continue
            if (!plane.isPoseInPolygon(hit.hitPose)) continue

            return hit.createAnchor()
        }

        return null
    }

    /**
     * Creates a [TransformableNode] and a [AnchorNode] as its root and returns the as a [Pair].
     *
     * @return pair of [AnchorNode] and [TransformableNode]
     */
    private fun createNodeFor(anchor: Anchor, renderable: ModelRenderable): Pair<AnchorNode, TransformableNode> {
        val transformationSystem = transformationSystem
        val anchorNode = AnchorNode(anchor)
        val transformableNode = TransformableNode(transformationSystem)
        transformableNode.renderable = renderable
        transformableNode.setParent(anchorNode)

        return Pair(anchorNode, transformableNode)
    }

    private fun addNodeToScene(anchorNode: AnchorNode) {
        arSceneView.scene.addChild(anchorNode)
    }
}