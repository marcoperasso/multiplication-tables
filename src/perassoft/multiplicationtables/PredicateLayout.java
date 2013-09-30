package perassoft.multiplicationtables;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * ViewGroup that arranges child views in a similar way to text, with them laid
 * out one line at a time and "wrapping" to the next line as needed.
 * 
 * Code licensed under CC-by-SA
 * 
 * @author Henrik Gustafsson
 * @see http 
 *      ://stackoverflow.com/questions/549451/line-breaking-widget-layout-for
 *      -android
 * @license http://creativecommons.org/licenses/by-sa/2.5/
 * 
 */
public class PredicateLayout extends ViewGroup {

	private int line_height;
	private int lOffsets[] = {0, 0, 0, 0, 0, 0};
	public PredicateLayout(Context context) {
		super(context);
	}

	public PredicateLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		for (int i = 0; i < lOffsets.length; i++)
			lOffsets[i] = 0;
		assert (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED);

		final int width = MeasureSpec.getSize(widthMeasureSpec);

		// The next line is WRONG!!! Doesn't take into account requested
		// MeasureSpec mode!
		int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()
				- getPaddingBottom();
		final int count = getChildCount();
		int line_height = 0;

		int xpos = getPaddingLeft();
		int ypos = getPaddingTop();
		int nRow = 0;
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();
				child.measure(MeasureSpec.makeMeasureSpec(width,
						MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(
						height, MeasureSpec.AT_MOST));

				final int childw = child.getMeasuredWidth();
				line_height = Math.max(line_height, child.getMeasuredHeight()
						+ lp.height);

				if (xpos + childw > width) {
					lOffsets[nRow] = (width - xpos) / 2;
					xpos = getPaddingLeft();
					ypos += line_height;
					nRow++;
				}

				xpos += childw + lp.width;
			}
		}
		if (lOffsets[nRow] == 0)
			lOffsets[nRow] = (width - xpos) / 2;
		this.line_height = line_height;

		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
			height = ypos + line_height;

		} else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
			if (ypos + line_height < height) {
				height = ypos + line_height;
			}
		}
		setMeasuredDimension(width, height);
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(1, 1); // default of 1px spacing
	}

	@Override
	protected boolean checkLayoutParams(LayoutParams p) {
		return (p instanceof LayoutParams);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();
		final int width = r - l;
		int nRow = 0;
		int xpos = getPaddingLeft() + lOffsets[nRow];
		int ypos = getPaddingTop();

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				final int childw = child.getMeasuredWidth();
				final int childh = child.getMeasuredHeight();
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();
				if (xpos + childw > width) {
					nRow++;
					xpos = getPaddingLeft() + lOffsets[nRow];
					ypos += line_height;
				}
				child.layout(xpos, ypos, xpos + childw, ypos + childh);
				xpos += childw + lp.width;
			}
		}
	}
}