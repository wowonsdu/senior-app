package zdrowy.senior.io.ui.visit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import zdrowy.senior.io.databinding.FragmentVisitsBinding

class VisitsFragment : Fragment() {
    private var binding: FragmentVisitsBinding? = null
    private val viewModel: VisitsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVisitsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.start()
        val viewBinding = binding ?: return

        viewModel.visitsText.observe(viewLifecycleOwner) { text ->
            viewBinding.visitsList.text = text
        }

        viewBinding.addVisitButton.setOnClickListener {
            val title = viewBinding.visitTitleInput.text.toString().trim()
            val dateTime = viewBinding.visitDateTimeInput.text.toString().trim()
            val notes = viewBinding.visitNotesInput.text.toString().trim().ifBlank { null }
            if (title.isNotBlank() && dateTime.isNotBlank()) {
                viewModel.addVisit(title, dateTime, notes)
                viewBinding.visitTitleInput.text?.clear()
                viewBinding.visitDateTimeInput.text?.clear()
                viewBinding.visitNotesInput.text?.clear()
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}

