package org.zus.bolt.helloworld.ui.mainactivity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.coroutines.runBlocking
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.MainActivityBinding
import org.zus.bolt.helloworld.utils.Utils

class MainActivity : AppCompatActivity() {
    private val READ_AND_WRITE_STORAGE_PERMISSION: Int = 1
    private lateinit var binding: MainActivityBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: MainViewModel
    private lateinit var utils: Utils
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        /* ViewModel to store the created wallet and allocation until the app is running. */
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        utils = Utils(this)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        /* Setting app bar for the back button navigation. */
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        /* Setting wallet json. */
        runBlocking {
            viewModel.wallet = utils.getWalletModel()
            viewModel.setWalletJson(utils.readWalletFromFileJSON())
            Log.i("WalletDetails", "json: ${viewModel.wallet?.walletJson}")
        }

        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        ActivityCompat.requestPermissions(
            this,
            permissions,
            READ_AND_WRITE_STORAGE_PERMISSION
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
