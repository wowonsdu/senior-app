package zdrowy.senior.io.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.ViewAlertOverviewItemBinding

class AlertOverviewItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewAlertOverviewItemBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AlertOverviewItemView)
        val title = typedArray.getString(R.styleable.AlertOverviewItemView_alertTitle)
        val subtitle = typedArray.getString(R.styleable.AlertOverviewItemView_alertSubtitle)
        val color = typedArray.getColor(
            R.styleable.AlertOverviewItemView_alertColor,
            ContextCompat.getColor(context, R.color.senior_primary)
        )
        typedArray.recycle()

        setTitle(title)
        setSubtitle(subtitle)
        setAlertColor(color)
    }

    fun setTitle(title: String?) {
        binding.alertTitle.text = title ?: ""
    }

    fun setSubtitle(subtitle: String?) {
        binding.alertSubtitle.text = subtitle ?: ""
    }

    fun setAlertColor(@ColorInt color: Int) {
        binding.alertCardRoot.setCardBackgroundColor(color)
    }
}
