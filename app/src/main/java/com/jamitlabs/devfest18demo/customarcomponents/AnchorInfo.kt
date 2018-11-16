package com.jamitlabs.devfest18demo.customarcomponents

import com.google.ar.core.Anchor

data class AnchorInfo (val id: String, val objectFile: String)

class ObjectAnchor(val anchor: Anchor, val objectName: String) {

    val info: AnchorInfo
        get() = AnchorInfo(anchor.cloudAnchorId, objectName)
}