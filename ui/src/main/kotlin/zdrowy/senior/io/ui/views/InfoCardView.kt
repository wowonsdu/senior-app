package zdrowy.senior.io.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.ViewInfoCardBinding

class InfoCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding = ViewInfoCardBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        radius = resources.getDimension(R.dimen.radius_m)
        useCompatPadding = true
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.senior_surface))

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.InfoCardView)
        val title = typedArray.getString(R.styleable.InfoCardView_cardTitle)
        val subtitle = typedArray.getString(R.styleable.InfoCardView_cardSubtitle)
        val iconRes = typedArray.getResourceId(R.styleable.InfoCardView_cardIcon, 0)
        typedArray.recycle()

        setTitle(title)
        setSubtitle(subtitle)
        if (iconRes != 0) {
            binding.cardIcon.setImageResource(iconRes)
        }
    }

    fun setTitle(title: String?) {
        binding.cardTitle.text = title ?: ""
    }

    fun setSubtitle(subtitle: String?) {
        binding.cardSubtitle.text = subtitle ?: ""
    }
}
