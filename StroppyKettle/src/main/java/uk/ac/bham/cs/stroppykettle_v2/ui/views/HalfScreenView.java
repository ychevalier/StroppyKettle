package uk.ac.bham.cs.stroppykettle_v2.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class HalfScreenView extends View {

	private int mScreenWidth;

	public HalfScreenView(Context context) {
		this(context, null);
	}

	public HalfScreenView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressLint("NewApi")
	public HalfScreenView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			display.getSize(size);
			mScreenWidth = size.x;
		} else {
			mScreenWidth = display.getWidth();
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mScreenWidth / 2, 1);
	}


}
