package com.example.my_notification.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my_notification.databinding.FragmentNotificationListBinding
import com.example.my_notification.presentation.adapter.NotificationAdapter
import com.example.my_notification.presentation.state.NotificationUiEvent
import com.example.my_notification.presentation.viewmodel.NotificationViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationListFragment : Fragment() {
    
    private var _binding: FragmentNotificationListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupViews()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(
            onItemClick = { notification ->
                if (!notification.isRead) {
                    viewModel.handleEvent(NotificationUiEvent.MarkAsRead(notification.id))
                }
            },
            onDeleteClick = { notification ->
                viewModel.handleEvent(NotificationUiEvent.DeleteNotification(notification.id))
            }
        )
        
        binding.recyclerViewNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }
    
    private fun setupViews() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.handleEvent(NotificationUiEvent.LoadNotifications)
            binding.swipeRefreshLayout.isRefreshing = false
        }
        
        binding.fabClearAll.setOnClickListener {
            showClearAllConfirmation()
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
        // 로딩 상태
        binding.swipeRefreshLayout.isRefreshing = state.isLoading
        
        // 알림 목록 업데이트
        notificationAdapter.submitList(state.notifications)
        
        // 빈 상태 표시
        if (state.notifications.isEmpty()) {
            binding.textViewEmpty.visibility = View.VISIBLE
            binding.recyclerViewNotifications.visibility = View.GONE
        } else {
            binding.textViewEmpty.visibility = View.GONE
            binding.recyclerViewNotifications.visibility = View.VISIBLE
        }
        
        // 에러 메시지 표시
        state.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            viewModel.handleEvent(NotificationUiEvent.ClearError)
        }
    }
    
    private fun showClearAllConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("모든 알림 삭제")
            .setMessage("모든 알림을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                Snackbar.make(binding.root, "모든 알림이 삭제되었습니다.", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 