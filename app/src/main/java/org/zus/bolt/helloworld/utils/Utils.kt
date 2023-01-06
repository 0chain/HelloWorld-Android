package org.zus.bolt.helloworld.utils

import android.content.Context
import com.google.gson.Gson
import org.json.JSONObject
import org.zus.bolt.helloworld.models.WalletModel
import java.io.FileNotFoundException
import java.io.IOException

class Utils(var applicationContext: Context) {
    companion object {
        private val configJsonString = """
            {
              "config": {
                "chain_id": "0afc093ffb509f059c55478bc1a60351cef7b4e9c008a53a6cc8241ca8617dfe",
                "signature_scheme": "bls0chain",
                "block_worker": "https://demo.zus.network/dns",
                "min_submit": 50,
                "min_confirmation": 50,
                "confirmation_chain_length": 3,
                "num_keys": 1,
                "eth_node": "https://ropsten.infura.io/v3/f0a254d8d18b4749bd8540da63b3292b"
              },
              "data_shards": 2,
              "parity_shards": 2,
              "zbox_url": "https://0box.demo.zus.network/",
              "block_worker": "https://demo.zus.network",
              "domain_url": "demo.zus.network",
              "network_fee_url": "https://demo.zus.network/miner01/v1/block/get/fee_stats",
              "explorer_url": "https://demo.zus.network/"
            }
        """.trimIndent()

        val config: String = JSONObject(configJsonString).get("config").toString()
    }

    fun getConfigFromAssets(configFileName: String): String? {
        val jsonString: String = try {
            val configJSON = JSONObject(readFile(configFileName))
            configJSON.getJSONObject("config").toString()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return jsonString
    }

    fun saveWalletAsFile(walletJson: String) {
        createFile("wallet.json", walletJson)
    }

    fun getWalletModel(): WalletModel? {
        if (isWalletExist()) {
            var wallet = Gson().fromJson(readWalletFromFileJSON(), WalletModel::class.java)
            wallet.walletJson = readWalletFromFileJSON()
            return wallet
        } else
            return null
    }

    fun readWalletFromFileJSON(): String {
        return readFile("wallet.json")
    }

    fun isWalletExist(): Boolean {
        return !(readWalletFromFileJSON().isBlank() || readWalletFromFileJSON().isEmpty())
    }

    private fun createFile(fileName: String, content: String) {
        applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(content.toByteArray())
        }
    }

    private fun readFile(fileName: String): String {
        return try {
            applicationContext.openFileInput(fileName).bufferedReader().use {
                it.readText()
            }
        } catch (e: FileNotFoundException) {
            return ""
        }
    }
}
