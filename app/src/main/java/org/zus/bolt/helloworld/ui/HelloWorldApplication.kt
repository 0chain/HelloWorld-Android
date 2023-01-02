package org.zus.bolt.helloworld.ui

import android.app.Application
import org.zus.bolt.helloworld.utils.Utils
import zcncore.Zcncore

class HelloWorldApplication : Application() {
    init {
        /* Adding .so files/native libraries in the jNi libs for proper functioning of the app with gosdk. */
        try {
            System.loadLibrary("c++_shared")
            System.loadLibrary("bls384")
            System.loadLibrary("gojni")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize Zcncore with chain config.
//            BoltViewModel.initZcncore()
            Zcncore.init(Utils(applicationContext).getConfigFromAssets("config.json"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
