package org.zus.bolt.helloworld.ui

import android.app.Application
import android.util.Log
import org.zus.bolt.helloworld.utils.Utils
import zcncore.Zcncore

private const val TAG_APP = "HelloWorldApplication"

class ZusExampleApplication : Application() {

    init {
        /* Adding .so files/native libraries in the jNi libs for proper functioning of the app with gosdk. */
        try {
            System.loadLibrary("c++_shared")
            System.loadLibrary("bls384")
            System.loadLibrary("gojni")
            Log.i(TAG_APP, "successfully initialized native libraries")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG_APP, "failed to initialize native libraries UnstatisfiedLinkError", e)
        } catch (e: Exception) {
            Log.e(TAG_APP, "failed to initialize native libraries Exception", e)
        }
    }

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize Zcncore with chain config  at the start of the Application.
            Zcncore.init(Utils.config)
            Log.i(TAG_APP, "onCreate: sdk initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG_APP, "onCreate: sdk initialization failed Exception", e)
        }
    }
}
