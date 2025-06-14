package com.example.my_notification

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.my_notification.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
            // 알림 권한이 허용되면 배터리 최적화 무시 요청
            requestBatteryOptimizationExemption()
        } else {
            Log.d(TAG, "알림 권한이 거부되었습니다.")
            showPermissionDeniedMessage()
        }
    }
    
    private val batteryOptimizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (isBatteryOptimizationIgnored()) {
            Log.d(TAG, "배터리 최적화가 무시되었습니다.")
            Snackbar.make(
                binding.root,
                "백그라운드 알림 수신이 최적화되었습니다.",
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            Log.d(TAG, "배터리 최적화 무시가 거부되었습니다.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        askNotificationPermission()
        
        // 알림에서 앱을 열었는지 확인
        handleNotificationIntent()
    }
    
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setupNavigation()
    }

    private fun setupNavigation() {
        try {
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
            binding.bottomNavigation.setupWithNavController(navController)
        } catch (e: Exception) {
            Log.e(TAG, "Navigation 설정 중 오류 발생: ${e.message}", e)
        }
    }

    private fun askNotificationPermission() {
        // Android 13 (API level 33) 이상에서만 알림 권한이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == 
                PackageManager.PERMISSION_GRANTED) {
                // 권한이 이미 허용됨
                Log.d(TAG, "알림 권한이 이미 허용되어 있습니다.")
                // 배터리 최적화 확인
                requestBatteryOptimizationExemption()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // 권한 요청 이유를 설명해야 함
                showPermissionRationale()
            } else {
                // 직접 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 13 미만에서는 배터리 최적화만 확인
            requestBatteryOptimizationExemption()
        }
    }
    
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                showBatteryOptimizationDialog()
            } else {
                Log.d(TAG, "배터리 최적화가 이미 무시되고 있습니다.")
            }
        }
    }
    
    private fun showBatteryOptimizationDialog() {
        AlertDialog.Builder(this)
            .setTitle("백그라운드 알림 수신")
            .setMessage("백그라운드에서도 알림을 안정적으로 받기 위해 배터리 최적화를 무시하도록 설정해주세요.")
            .setPositiveButton("설정하기") { _, _ ->
                openBatteryOptimizationSettings()
            }
            .setNegativeButton("나중에") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun openBatteryOptimizationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                batteryOptimizationLauncher.launch(intent)
            } catch (e: Exception) {
                Log.e(TAG, "배터리 최적화 설정 화면을 열 수 없습니다.", e)
                // 대안으로 일반 배터리 설정 화면 열기
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(intent)
                } catch (e2: Exception) {
                    Log.e(TAG, "배터리 설정 화면을 열 수 없습니다.", e2)
                }
            }
        }
    }
    
    private fun isBatteryOptimizationIgnored(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(packageName)
        } else {
            true
        }
    }
    
    private fun handleNotificationIntent() {
        val fromNotification = intent.getBooleanExtra("from_notification", false)
        if (fromNotification) {
            Log.d(TAG, "앱이 알림을 통해 열렸습니다.")
            // 필요시 특정 화면으로 이동하는 로직 추가
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