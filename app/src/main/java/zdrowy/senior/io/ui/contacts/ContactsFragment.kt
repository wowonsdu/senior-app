package zdrowy.senior.io.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import zdrowy.senior.io.databinding.FragmentContactsBinding
import zdrowy.senior.io.domain.contact.ContactType

class ContactsFragment : Fragment() {
    private var binding: FragmentContactsBinding? = null
    private val viewModel: ContactsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.start()
        val viewBinding = binding ?: return

        viewModel.contactsText.observe(viewLifecycleOwner) { text ->
            viewBinding.contactsList.text = text
        }

        viewBinding.addContactButton.setOnClickListener {
            val name = viewBinding.contactNameInput.text.toString().trim()
            val phone = viewBinding.contactPhoneInput.text.toString().trim().ifBlank { null }
            val specialization = viewBinding.contactSpecializationInput.text.toString().trim().ifBlank { null }
            val type = when (viewBinding.contactTypeSpinner.selectedItemPosition) {
                0 -> ContactType.CAREGIVER
                else -> ContactType.DOCTOR
            }
            if (name.isNotBlank()) {
                viewModel.addContact(type, name, phone, specialization)
                viewBinding.contactNameInput.text?.clear()
                viewBinding.contactPhoneInput.text?.clear()
                viewBinding.contactSpecializationInput.text?.clear()
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}

