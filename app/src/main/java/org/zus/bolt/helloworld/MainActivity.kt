package org.zus.bolt.helloworld

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import org.zus.bolt.helloworld.databinding.ActivityMainBinding
import org.zus.bolt.helloworld.models.WalletModel
import zcncore.Zcncore

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = MainViewModel()
        binding.btCreateWallet.setOnClickListener {
            Zcncore.createWallet { status, walletJson, error ->
                if (status == 0L && error == null) {
                    Gson().fromJson(walletJson, WalletModel::class.java).let {
                        viewModel.wallet = it
                    }
                } else {
                    error("Error: $error")
                }
            }
        }
        binding.btRecoverWallet.setOnClickListener {

        }
    }
}
