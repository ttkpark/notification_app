package com.example.my_notification.presentation.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.my_notification.databinding.FragmentHomeBinding
import com.example.my_notification.presentation.state.NotificationUiEvent
import com.example.my_notification.presentation.viewmodel.NotificationViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: NotificationViewModel by viewModels()
    
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
        observeViewModel()
    }
    
    private fun setupViews() {
        binding.buttonCopyToken.setOnClickListener {
            copyTokenToClipboard()
        }
        
        binding.buttonRefreshToken.setOnClickListener {
            viewModel.handleEvent(NotificationUiEvent.RefreshToken)
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.handleEvent(NotificationUiEvent.LoadNotifications)
            viewModel.handleEvent(NotificationUiEvent.RefreshToken)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }
    
    private fun updateUI(state: com.example.my_notification.presentation.state.NotificationUiState) {
        // 토큰 로딩 상태
        binding.progressBarToken.visibility = if (state.isTokenLoading) View.VISIBLE else View.GONE
        
        // FCM 토큰 표시
        binding.textViewToken.text = state.fcmToken ?: "토큰을 가져오는 중..."
        
        // 알림 통계 업데이트
        val totalCount = state.notifications.size
        val unreadCount = state.notifications.count { !it.isRead }
        
        binding.textViewNotificationCount.text = "총 ${totalCount}개의 알림"
        binding.textViewUnreadCount.text = "읽지 않음: ${unreadCount}개"
        
        // 에러 메시지 표시
        state.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            viewModel.handleEvent(NotificationUiEvent.ClearError)
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