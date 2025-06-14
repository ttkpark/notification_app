package com.example.my_notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.my_notification.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "알림 권한이 허용되었습니다.")
        } else {
            Log.d(TAG, "알림 권한이 거부되었습니다.")
            showPermissionDeniedMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupNavigation()
        askNotificationPermission()
    }

    private fun setupNavigation() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        
        // Bottom Navigation에서 Home, Notifications, Settings 화면으로 구성
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_notifications,
                R.id.navigation_settings
            )
        )
        
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Bottom Navigation과 NavController 연결
        binding.bottomNavigation?.setupWithNavController(navController)
    }

    private fun askNotificationPermission() {
        // Android 13 (API level 33) 이상에서만 알림 권한이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == 
                PackageManager.PERMISSION_GRANTED) {
                // 권한이 이미 허용됨
                Log.d(TAG, "알림 권한이 이미 허용되어 있습니다.")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // 권한 요청 이유를 설명해야 함
                showPermissionRationale()
            } else {
                // 직접 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showPermissionRationale() {
        Snackbar.make(
            binding.root,
            "푸시 알림을 받기 위해 알림 권한이 필요합니다.",
            Snackbar.LENGTH_LONG
        ).setAction("허용") {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }.show()
    }

    private fun showPermissionDeniedMessage() {
        Snackbar.make(
            binding.root,
            "알림 권한이 거부되었습니다. 설정에서 권한을 허용해주세요.",
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}