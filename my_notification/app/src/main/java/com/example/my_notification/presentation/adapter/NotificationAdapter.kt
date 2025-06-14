package com.example.my_notification.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.my_notification.databinding.ItemNotificationBinding
import com.example.my_notification.domain.model.NotificationMessage
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val onItemClick: (NotificationMessage) -> Unit,
    private val onDeleteClick: (NotificationMessage) -> Unit
) : ListAdapter<NotificationMessage, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationMessage) {
            binding.apply {
                textViewTitle.text = notification.title
                textViewBody.text = notification.body
                textViewTimestamp.text = formatTimestamp(notification.timestamp)
                textViewSender.text = "발신자: ${notification.senderId}"
                
                // 읽음/읽지 않음 상태 표시
                root.alpha = if (notification.isRead) 0.6f else 1.0f
                
                // 클릭 이벤트
                root.setOnClickListener {
                    onItemClick(notification)
                }
                
                buttonDelete.setOnClickListener {
                    onDeleteClick(notification)
                }
            }
        }
        
        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}

class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationMessage>() {
    override fun areItemsTheSame(oldItem: NotificationMessage, newItem: NotificationMessage): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NotificationMessage, newItem: NotificationMessage): Boolean {
        return oldItem == newItem
    }
} 