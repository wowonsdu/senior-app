package zdrowy.senior.io.ui.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpacingItemDecoration(
    private val gapPx: Int,
    private val includeTop: Boolean = false
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        if (position > 0 || includeTop) {
            outRect.top = gapPx
        }
    }
}

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val gapPx: Int,
    private val includeEdge: Boolean = false
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION || spanCount <= 0) return

        val column = position % spanCount
        if (includeEdge) {
            outRect.left = gapPx - column * gapPx / spanCount
            outRect.right = (column + 1) * gapPx / spanCount
            if (position < spanCount) {
                outRect.top = gapPx
            }
            outRect.bottom = gapPx
        } else {
            outRect.left = column * gapPx / spanCount
            outRect.right = gapPx - (column + 1) * gapPx / spanCount
            if (position >= spanCount) {
                outRect.top = gapPx
            }
        }
    }
}
