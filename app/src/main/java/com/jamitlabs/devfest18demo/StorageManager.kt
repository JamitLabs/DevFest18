/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jamitlabs.devfest18demo

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.jamitlabs.devfest18demo.customarcomponents.AnchorInfo
import timber.log.Timber

/** Helper class for Firebase storage of cloud anchor IDs.  */
internal class StorageManager(context: Context) {

    companion object {
        private val KEY_ROOT_DIR = "shared_anchor_codelab_root"
        private val KEY_NEXT_SHORT_CODE = "next_short_code"
        private val KEY_PREFIX = "anchor;"
        private val INITIAL_SHORT_CODE = 142
    }

    private val rootRef: DatabaseReference

    init {
        val firebaseApp = FirebaseApp.initializeApp(context)
        rootRef = FirebaseDatabase.getInstance(firebaseApp!!).reference.child(KEY_ROOT_DIR)
        DatabaseReference.goOnline()
    }

    /** Gets a new short code that can be used to store the anchor ID.  */
    fun nextShortCode(onShortCodeAvailable: (Int?) -> Unit) {
        // Run a transaction on the node containing the next short code available. This increments the
        // value in the database and retrieves it in one atomic all-or-nothing operation.
        rootRef
                .child(KEY_NEXT_SHORT_CODE)
                .runTransaction(
                        object : Transaction.Handler {
                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                var shortCode = currentData.getValue(Int::class.java)
                                if (shortCode == null) {
                                    // Set the initial short code if one did not exist before.
                                    shortCode = INITIAL_SHORT_CODE - 1
                                }
                                currentData.value = shortCode + 1
                                return Transaction.success(currentData)
                            }

                            override fun onComplete(
                                    error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                                if (!committed) {
                                    Timber.e(error?.message, "Firebase Error")
                                    onShortCodeAvailable(null)
                                } else {
                                    onShortCodeAvailable(currentData!!.getValue(Int::class.java))
                                }
                            }
                        })
    }

    fun storeForNextShortCode(anchorInfo: AnchorInfo, onStoredBlock: (Int) -> Unit) {
        nextShortCode { nextShortCode ->
            val shortCode = nextShortCode ?: return@nextShortCode

            rootRef.child(KEY_PREFIX + shortCode).setValue(anchorInfo)
            onStoredBlock(shortCode)
        }
    }

    fun onAnchorReceived(onChange: (AnchorInfo) -> Unit) {
        rootRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {

                var id: String? = null
                var name: String? = null

                dataSnapshot.children.forEach {
                    if (it.key == "id" && it.value is String) {
                        id = it.value as String
                    }

                    if (it.key == "objectFile" && it.value is String) {
                        name = it.value as String
                    }
                }

                if (id != null && name != null) {
                    val dataBaseAnchor = AnchorInfo(id!!, name!!)
                    onChange(dataBaseAnchor)
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                //
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                //
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                //
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                //
            }
        })
    }
}
