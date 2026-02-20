package zdrowy.senior.io.ui.patient

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.ui.databinding.ViewPatientHistoryHeaderBinding

class PatientHistoryHeaderAdapter(
    private val binding: ViewPatientHistoryHeaderBinding
) : RecyclerView.Adapter<PatientHistoryHeaderAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val root = binding.root
        val currentParent = root.parent
        if (currentParent is ViewGroup) {
            currentParent.removeView(root)
        }
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = Unit

    override fun getItemCount(): Int = 1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
