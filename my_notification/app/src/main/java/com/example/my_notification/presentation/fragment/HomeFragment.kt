package com.example.my_notification.presentation.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.my_notification.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        loadFcmToken()
    }
    
    private fun setupViews() {
        binding.buttonCopyToken.setOnClickListener {
            copyTokenToClipboard()
        }
        
        binding.buttonRefreshToken.setOnClickListener {
            loadFcmToken()
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadFcmToken()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
    
    private fun loadFcmToken() {
        binding.progressBarToken.visibility = View.VISIBLE
        
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            binding.progressBarToken.visibility = View.GONE
            
            if (!task.isSuccessful) {
                binding.textViewToken.text = "토큰을 가져올 수 없습니다."
                return@addOnCompleteListener
            }

            // FCM 토큰 표시
            val token = task.result
            binding.textViewToken.text = token
            
            // 통계 정보 업데이트 (임시 데이터)
            binding.textViewNotificationCount.text = "총 0개의 알림"
            binding.textViewUnreadCount.text = "읽지 않음: 0개"
        }
    }
    
    private fun copyTokenToClipboard() {
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("FCM Token", binding.textViewToken.text)
        clipboardManager.setPrimaryClip(clip)
        
        Snackbar.make(binding.root, "토큰이 클립보드에 복사되었습니다.", Snackbar.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 