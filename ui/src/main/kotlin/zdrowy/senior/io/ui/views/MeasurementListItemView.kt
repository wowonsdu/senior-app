package zdrowy.senior.io.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.ViewMeasurementListItemBinding

class MeasurementListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewMeasurementListItemBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MeasurementListItemView)
        val title = typedArray.getString(R.styleable.MeasurementListItemView_measurementTitle)
        val subtitle = typedArray.getString(R.styleable.MeasurementListItemView_measurementSubtitle)
        val iconRes = typedArray.getResourceId(R.styleable.MeasurementListItemView_measurementIcon, 0)
        val iconTint = typedArray.getColor(
            R.styleable.MeasurementListItemView_measurementIconTint,
            ContextCompat.getColor(context, R.color.senior_primary)
        )
        typedArray.recycle()

        setTitle(title)
        setSubtitle(subtitle)
        if (iconRes != 0) {
            binding.measurementIcon.setImageResource(iconRes)
        }
        setIconTint(iconTint)
    }

    fun setTitle(title: String?) {
        binding.measurementTitle.text = title ?: ""
    }

    fun setSubtitle(subtitle: String?) {
        binding.measurementSubtitle.text = subtitle ?: ""
    }

    fun setIconRes(iconRes: Int) {
        if (iconRes != 0) {
            binding.measurementIcon.setImageResource(iconRes)
        }
    }

    fun setIconTint(@ColorInt color: Int) {
        binding.measurementIcon.setColorFilter(color)
    }
}
