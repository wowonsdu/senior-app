package zdrowy.senior.io.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RoleSelectionViewModel : ViewModel() {
    private val _selectedRole = MutableLiveData<AuthRole>()
    val selectedRole: LiveData<AuthRole> = _selectedRole

    fun selectRole(role: AuthRole) {
        _selectedRole.value = role
    }
}
