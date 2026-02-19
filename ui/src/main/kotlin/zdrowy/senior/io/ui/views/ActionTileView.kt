package zdrowy.senior.io.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.ViewActionTileBinding

class ActionTileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding = ViewActionTileBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        radius = resources.getDimension(R.dimen.radius_l)
        useCompatPadding = true
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.senior_info))

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ActionTileView)
        val title = typedArray.getString(R.styleable.ActionTileView_actionTitle)
        val iconRes = typedArray.getResourceId(R.styleable.ActionTileView_actionIcon, 0)
        val color = typedArray.getColor(
            R.styleable.ActionTileView_actionColor,
            ContextCompat.getColor(context, R.color.senior_info)
        )
        typedArray.recycle()

        setTitle(title)
        if (iconRes != 0) {
            binding.actionIcon.setImageResource(iconRes)
        }
        setTileColor(color)
    }

    fun setTitle(title: String?) {
        binding.actionTitle.text = title ?: ""
    }

    fun setTileColor(@ColorInt color: Int) {
        setCardBackgroundColor(color)
    }
}
