package zdrowy.senior.io.ui.designbook

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import zdrowy.senior.io.ui.databinding.ActivityDesignBookBinding

class DesignBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDesignBookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDesignBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
