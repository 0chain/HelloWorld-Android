package org.zus.bolt.helloworld.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

class Utils(var applicationContext: Context) {
    companion object {
        private val configJsonString = """
            {
              "config": {
                "chain_id": "0afc093ffb509f059c55478bc1a60351cef7b4e9c008a53a6cc8241ca8617dfe",
                "signature_scheme": "bls0chain",
                "block_worker": "https://demo.0chain.net/dns",
                "min_submit": 50,
                "min_confirmation": 50,
                "confirmation_chain_length": 3,
                "num_keys": 1,
                "eth_node": "https://ropsten.infura.io/v3/f0a254d8d18b4749bd8540da63b3292b"
              },
              "data_shards": 2,
              "parity_shards": 2,
              "zbox_url": "https://0box.demo.0chain.net/",
              "block_worker": "https://demo.0chain.net",
              "domain_url": "demo.0chain.net",
              "network_fee_url": "https://demo.0chain.net/miner01/v1/block/get/fee_stats",
              "explorer_url": "https://demo.0chain.net/"
            }
        """.trimIndent()

        val config: String = JSONObject(configJsonString).get("config").toString()
    }

    fun getConfigFromAssets(fileName: String): String? {
        val jsonString: String = try {
            val file: InputStream = applicationContext.getAssets().open(fileName)
            val size = file.available()
            val buffer = ByteArray(size)
            file.read(buffer)
            file.close()
            val json = JSONObject(String(buffer, StandardCharsets.UTF_8))
            json.getJSONObject("config").toString()

        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return jsonString
    }
}

