package zdrowy.senior.io.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.domain.alert.AlertChannel
import zdrowy.senior.io.ui.databinding.ItemAlertCaregiverToggleBinding
import zdrowy.senior.io.ui.databinding.ViewAlertNotificationsConfigBinding

class AlertNotificationsConfigView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding = ViewAlertNotificationsConfigBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private val caregiversAdapter = CaregiversAdapter { id, enabled ->
        onCaregiverToggle?.invoke(id, enabled)
    }

    private var suppressChannelCallbacks = false

    var onChannelToggle: ((AlertChannel, Boolean) -> Unit)? = null
    var onEnableAllChannels: (() -> Unit)? = null
    var onDisableAllChannels: (() -> Unit)? = null

    var onCaregiverToggle: ((String, Boolean) -> Unit)? = null
    var onSelectAllCaregivers: (() -> Unit)? = null
    var onDeselectAllCaregivers: (() -> Unit)? = null

    init {
        orientation = VERTICAL

        binding.alertNotificationsCaregiversList.adapter = caregiversAdapter

        binding.alertNotificationsChannelsEnableAll.setOnClickListener { onEnableAllChannels?.invoke() }
        binding.alertNotificationsChannelsDisableAll.setOnClickListener { onDisableAllChannels?.invoke() }

        binding.alertNotificationsCaregiversSelectAll.setOnClickListener { onSelectAllCaregivers?.invoke() }
        binding.alertNotificationsCaregiversDeselectAll.setOnClickListener { onDeselectAllCaregivers?.invoke() }

        binding.alertNotificationsSmsCard.setOnClickListener { binding.alertNotificationsSmsSwitch.toggle() }
        binding.alertNotificationsEmailCard.setOnClickListener { binding.alertNotificationsEmailSwitch.toggle() }
        binding.alertNotificationsAppCard.setOnClickListener { binding.alertNotificationsAppSwitch.toggle() }

        binding.alertNotificationsSmsSwitch.setOnCheckedChangeListener { _, checked ->
            if (suppressChannelCallbacks) return@setOnCheckedChangeListener
            onChannelToggle?.invoke(AlertChannel.SMS, checked)
        }
        binding.alertNotificationsEmailSwitch.setOnCheckedChangeListener { _, checked ->
            if (suppressChannelCallbacks) return@setOnCheckedChangeListener
            onChannelToggle?.invoke(AlertChannel.EMAIL, checked)
        }
        binding.alertNotificationsAppSwitch.setOnCheckedChangeListener { _, checked ->
            if (suppressChannelCallbacks) return@setOnCheckedChangeListener
            onChannelToggle?.invoke(AlertChannel.APP, checked)
        }
    }

    fun setCaregiversSubtitle(text: String) {
        binding.alertNotificationsCaregiversSubtitle.text = text
    }

    fun renderChannels(channels: Set<AlertChannel>) {
        suppressChannelCallbacks = true
        try {
            binding.alertNotificationsSmsSwitch.isChecked = channels.contains(AlertChannel.SMS)
            binding.alertNotificationsEmailSwitch.isChecked = channels.contains(AlertChannel.EMAIL)
            binding.alertNotificationsAppSwitch.isChecked = channels.contains(AlertChannel.APP)
        } finally {
            suppressChannelCallbacks = false
        }
    }

    fun renderCaregivers(items: List<CaregiverToggleUi>) {
        caregiversAdapter.submit(items)
        binding.alertNotificationsCaregiversEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        binding.alertNotificationsCaregiversList.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }

    data class CaregiverToggleUi(
        val id: String,
        val fullName: String,
        val phone: String,
        val selected: Boolean
    )

    private class CaregiversAdapter(
        private val onToggle: (String, Boolean) -> Unit
    ) : RecyclerView.Adapter<CaregiversAdapter.VH>() {

        private var items: List<CaregiverToggleUi> = emptyList()
        private var suppress = false

        fun submit(newItems: List<CaregiverToggleUi>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemAlertCaregiverToggleBinding.inflate(inflater, parent, false)
            return VH(binding)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.bind(item)
        }

        inner class VH(
            private val binding: ItemAlertCaregiverToggleBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: CaregiverToggleUi) {
                binding.alertCaregiverName.text = item.fullName
                binding.alertCaregiverPhone.text = item.phone

                binding.alertCaregiverSwitch.setOnCheckedChangeListener(null)
                binding.alertCaregiverSwitch.isChecked = item.selected
                binding.alertCaregiverSwitch.setOnCheckedChangeListener { _, checked ->
                    if (suppress) return@setOnCheckedChangeListener
                    onToggle(item.id, checked)
                }

                binding.root.setOnClickListener {
                    suppress = true
                    try {
                        binding.alertCaregiverSwitch.toggle()
                    } finally {
                        suppress = false
                    }
                }
            }
        }
    }
}
