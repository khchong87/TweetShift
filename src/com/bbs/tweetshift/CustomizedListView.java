package com.bbs.tweetshift;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class CustomizedListView extends ListView {

	private static final int MAX_Y_OVERSCROLL_DISTANCE = 200;

	private Context mContext;
	private int mMaxYOverscrollDistance;
	private CustomizedArrayAdapter adapter;

	private boolean toLoadTop;
	private boolean toLoadBottom;
	
//	private View headerView;
	private View footerView;

	public CustomizedListView(Context context) {
		super(context);
		mContext = context;
		initBounceListView();
	}
	
	public CustomizedListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initBounceListView();
	}

	public CustomizedListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initBounceListView();
	}

	public void setAdapter(CustomizedArrayAdapter adapter) {
		this.adapter = adapter;
		super.setAdapter(adapter);
	}

	private void initBounceListView() {
		// get the density of the screen and do some maths with it on the max
		// overscroll distance
		// variable so that you get similar behaviors no matter what the screen
		// size

		final DisplayMetrics metrics = mContext.getResources()
				.getDisplayMetrics();
		final float density = metrics.density;
		LayoutInflater vi = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footerView = vi.inflate(R.layout.loadingrow, null);
		addFooterView(footerView);
		// headerView = vi.inflate(R.layout.pulltorefresh, null);
		// addFooterView(headerView);
		mMaxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
	}

	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		
		// This is where the magic happens, we have replaced the incoming
		// maxOverScrollY with our own custom variable mMaxYOverscrollDistance;
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
				scrollRangeX, scrollRangeY, maxOverScrollX,
				mMaxYOverscrollDistance, isTouchEvent);
	}

	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
			boolean clampedY) {
		
		if (clampedY && scrollY > 0) {
			toLoadBottom = true;
		}else if (clampedY && scrollY < 0) {
			toLoadTop = true;
		}else {
			toLoadBottom = false;
			toLoadTop = false;
		}
		
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
	}

	public View getFooterView() {
		return footerView;
	}
	
	public void removeFooter(){
		this.removeFooterView(footerView);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if( MotionEvent.ACTION_UP == ev.getAction() ){
			if(toLoadBottom){
				if (View.GONE == footerView.getVisibility()) {
					footerView.setVisibility(View.VISIBLE);
				}
				adapter.onScrollBottom();
			}else if( toLoadTop ){
				if (View.GONE == footerView.getVisibility()) {
					footerView.setVisibility(View.VISIBLE);
				}
				adapter.onScrollTop();
			}
		}
		return super.onTouchEvent(ev);
	}
	
}
