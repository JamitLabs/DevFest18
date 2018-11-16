package com.jamitlabs.devfest18demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.jamitlabs.devfest18demo.customarcomponents.CustomArFragment
import com.jamitlabs.devfest18demo.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity: AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    private var arSession: Session? = null
    private var arFragment: CustomArFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            viewModel = mainViewModel
            setLifecycleOwner(this@MainActivity)
        }

        arFragment = CustomArFragment().apply {
            supportFragmentManager
                    .beginTransaction()
                    .add(binding.arFragmentContainer.id, this)
                    .commit()
        }

        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        if (arSession == null) {
            createSession()?.let { newSession ->
                arSession = newSession
                arFragment?.arSceneView?.setupSession(newSession)
            }
        }

        arFragment?.setViewModel(mainViewModel)
    }

    override fun onPause() {
        super.onPause()

        arSession?.pause()
    }

    private fun createSession(): Session? {
        var newSession: Session? = null

        try {
            newSession = Session(baseContext)
        } catch (e: UnavailableArcoreNotInstalledException) {
            Timber.e(e, "ArCore unavailable on this device")
        } catch (e: UnavailableApkTooOldException) {
            Timber.e(e, "APK tool to old")
        } catch (e: UnavailableSdkTooOldException) {
            Timber.e(e, "SDK tool to old")
        }

        // Create default config and check if supported.
        val config = Config(newSession).apply {
            cloudAnchorMode = Config.CloudAnchorMode.ENABLED
            updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        }

        newSession?.configure(config)

        return newSession
    }
}