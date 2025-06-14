package com.example.my_notification.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my_notification.databinding.FragmentNotificationListBinding
import com.google.android.material.snackbar.Snackbar

class NotificationListFragment : Fragment() {
    
    private var _binding: FragmentNotificationListBinding? = null
    private val binding get() = _binding!!
    
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
        showEmptyState()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerViewNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            // 임시로 어댑터 없이 설정
        }
    }
    
    private fun setupViews() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // 새로고침 기능 (임시)
            binding.swipeRefreshLayout.isRefreshing = false
            Snackbar.make(binding.root, "알림 목록을 새로고침했습니다.", Snackbar.LENGTH_SHORT).show()
        }
        
        binding.fabClearAll.setOnClickListener {
            showClearAllConfirmation()
        }
    }
    
    private fun showEmptyState() {
        // 임시로 빈 상태 표시
        binding.textViewEmpty.visibility = View.VISIBLE
        binding.recyclerViewNotifications.visibility = View.GONE
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