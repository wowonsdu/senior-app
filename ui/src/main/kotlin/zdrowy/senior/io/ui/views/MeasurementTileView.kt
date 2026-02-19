package zdrowy.senior.io.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.ViewMeasurementTileBinding

class MeasurementTileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewMeasurementTileBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MeasurementTileView)
        val title = typedArray.getString(R.styleable.MeasurementTileView_tileTitle)
        val iconRes = typedArray.getResourceId(R.styleable.MeasurementTileView_tileIcon, 0)
        val color = typedArray.getColor(
            R.styleable.MeasurementTileView_tileColor,
            ContextCompat.getColor(context, R.color.senior_primary)
        )
        typedArray.recycle()

        setTitle(title)
        if (iconRes != 0) {
            binding.tileIcon.setImageResource(iconRes)
        }
        setTileColor(color)
    }

    fun setTitle(title: String?) {
        binding.tileTitle.text = title ?: ""
    }

    fun setTileColor(@ColorInt color: Int) {
        binding.cardRoot.setCardBackgroundColor(color)
    }
}
