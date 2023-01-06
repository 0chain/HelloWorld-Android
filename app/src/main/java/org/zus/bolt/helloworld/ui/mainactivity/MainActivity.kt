package org.zus.bolt.helloworld.ui.mainactivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.MainActivityBinding
import org.zus.bolt.helloworld.utils.Utils

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        // viewModel to store the temporarliy created wallet until the app is running.
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        viewModel.wallet = Utils(applicationContext).getWalletModel()
        viewModel.setWalletJson(Utils(applicationContext).readWalletFromFileJSON())
        // Setting app bar for the back button navigation.
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
