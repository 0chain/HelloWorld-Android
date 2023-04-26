package org.zus.helloworld.ui.mainactivity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.coroutines.runBlocking
import org.zus.helloworld.R
import org.zus.helloworld.databinding.MainActivityBinding
import org.zus.helloworld.utils.Utils
import java.io.File

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

        navController.addOnDestinationChangedListener() { _, destination, _ ->
            if (destination.id == R.id.selectAppFragment) {
                binding.toolbar.visibility = View.GONE
            } else {
                binding.toolbar.visibility = View.VISIBLE
            }
        }

        /* Setting wallet json. */
        viewModel.wallet = utils.getWalletModel()
        viewModel.setWalletJson(utils.readWalletFromFileJSON())
        Log.i("WalletDetails", "json: ${viewModel.wallet?.walletJson}")

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

    override fun onDestroy() {
        /* Deletes cache files after app is closed. */
        val fileDir: File? = applicationContext.cacheDir
        deleteDir(fileDir)

        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children: Array<String> = dir.list() as Array<String>
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }
}
