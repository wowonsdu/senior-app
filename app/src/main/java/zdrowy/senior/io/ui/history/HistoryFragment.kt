package zdrowy.senior.io.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import zdrowy.senior.io.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    private var binding: FragmentHistoryBinding? = null
    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.start()
        viewModel.historyText.observe(viewLifecycleOwner) { text ->
            binding?.historyText?.text = text
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}

