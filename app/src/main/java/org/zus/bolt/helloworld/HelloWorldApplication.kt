package org.zus.bolt.helloworld

import android.app.Application
import zcncore.Zcncore
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

class HelloWorldApplication : Application() {
    init {
        /* Adding .so files in the jNi libs for proper functioning of the app with gosdk. */
        System.loadLibrary("c++_shared")
        System.loadLibrary("bls384")
        System.loadLibrary("gojni")
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Zcncore with chain config.
        Zcncore.init(getConfigFromAssets("config.json"))
    }

    private fun getConfigFromAssets(fileName: String): String? {
        val jsonString: String
        jsonString = try {
            val `is`: InputStream = applicationContext.getAssets().open(fileName)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return jsonString
    }
}
