package com.emilsjolander.components.StickyListHeaders;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
/**
 * 
 * @author Emil Sj�lander
 * 
 * 
Copyright 2012 Emil Sj�lander

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 *
 */
public class StickyListHeadersListView extends ListView implements OnScrollListener {
	
	private static final String HEADER_HEIGHT = "headerHeight";
	private static final String SUPER_INSTANCE_STATE = "superInstanceState";
	
	private OnScrollListener scrollListener;
	private boolean areHeadersSticky;
	private int headerBottomPosition;
	private int headerHeight = -1;
	private View header;
	private int dividerHeight;
	private Drawable divider;
	private boolean clippingToPadding;
	private boolean clipToPaddingHasBeenSet;
	private long oldHeaderId = -1;
	private boolean headerHasChanged = true;
	private boolean setupDone;
	private View lastWatchedViewHeader;
	private Rect clippingRect = new Rect();

	public StickyListHeadersListView(Context context) {
		super(context);
		setup();
	}

	public StickyListHeadersListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.StickyListHeadersListView);
		setAreHeadersSticky(a.getBoolean(0, false));
		a.recycle();
		setup();
	}

	public StickyListHeadersListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.StickyListHeadersListView);
		setAreHeadersSticky(a.getBoolean(0, true));
		a.recycle();
		setup();
	}
	
	private void setup() {
		if(!setupDone){
			setupDone = true;
			super.setOnScrollListener(this);
			setDivider(getDivider());
			setDividerHeight(getDividerHeight());
			//null out divider, dividers are handled by adapter so they look good with headers
			super.setDivider(null);
			super.setDividerHeight(0);
			setVerticalFadingEdgeEnabled(false);
		}
	}

	boolean headerTouch = false;

//// HACK HACK HACK UGLY HACK
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
	   if (header != null && clippingRect.contains((int)ev.getX(), (int)ev.getY())) {
	      headerTouch = true;
	      return true;
       }
	   return super.onInterceptTouchEvent(ev);
	}

	Point down;
	boolean headerPressed = false;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
	   if (!headerTouch || header == null) {
	      headerTouch = false;
	      headerPressed = false;
	      return super.onTouchEvent(ev);
	   }
	   switch (ev.getAction()) {
	   case MotionEvent.ACTION_DOWN: {
	      if (clippingRect.contains((int)ev.getX(), (int)ev.getY())) {
	         headerPressed = true;
	         down = new Point((int)ev.getX(), (int)ev.getY());
	      } else {
	         headerPressed = false;
	         down = null;
	      }
	   }
	   break;
	   case MotionEvent.ACTION_MOVE: {
	      headerPressed = clippingRect.contains((int)ev.getX(), (int)ev.getY());
	   }
	   break;
	   case MotionEvent.ACTION_UP: {
	      if (down != null && clippingRect.contains((int)ev.getX(), (int)ev.getY())) {
	    	  ((FrameLayout)header).getChildAt(1).performClick();
	      }
	   }
	   case MotionEvent.ACTION_CANCEL:
	      headerTouch = false;
	      headerPressed = false;
	      break;

	   default:
	      break;
	   }

	   invalidate();
	   return true;
	}
// END HACK HACK HACK UGLY HACK

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		headerHeight = ((Bundle)state).getInt(HEADER_HEIGHT);
		headerHasChanged = true;
		super.onRestoreInstanceState(((Bundle)state).getParcelable(SUPER_INSTANCE_STATE));
	}
	
	@Override
	public Parcelable onSaveInstanceState() {
		Bundle instanceState = new Bundle();
		instanceState.putInt(HEADER_HEIGHT, headerHeight);
		instanceState.putParcelable(SUPER_INSTANCE_STATE, super.onSaveInstanceState());
		return instanceState;
	}
	
	/**
	 * can only be set to false if headers are sticky, not compatible with fading edges
	 */
	@Override
	public void setVerticalFadingEdgeEnabled(boolean verticalFadingEdgeEnabled) {
		if(areHeadersSticky){
			super.setVerticalFadingEdgeEnabled(false);
		}else{
			super.setVerticalFadingEdgeEnabled(verticalFadingEdgeEnabled);
		}
	}
	
	@Override
	public void setDivider(Drawable divider) {
		if(setupDone){
			this.divider = divider;
			if(getAdapter()!=null){
				((StickyListHeadersAdapter)getAdapter()).setDivider(divider);
			}
		}else{
			super.setDivider(divider);
		}
	}
	
	@Override
	public void setDividerHeight(int height) {
		if(setupDone){
			dividerHeight = height;
			if(getAdapter()!=null){
				((StickyListHeadersAdapter)getAdapter()).setDividerHeight(height);
			}
		}else{
			super.setDividerHeight(height);
		}
	}
	
	@Override
	public void setOnScrollListener(OnScrollListener l) {
		scrollListener = l;
	}
	
	public void setAreHeadersSticky(boolean areHeadersSticky) {
		if(areHeadersSticky){
			super.setVerticalFadingEdgeEnabled(false);
		}
		this.areHeadersSticky = areHeadersSticky;
	}

	public boolean areHeadersSticky() {
		return areHeadersSticky;
	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		//TODO: if headers are not sticky dont care?
		if(areHeadersSticky){
			if(!clipToPaddingHasBeenSet){
				clippingToPadding = true;
			}
			if(!(adapter instanceof StickyListHeadersAdapter)) throw new IllegalArgumentException("Adapter must be a subclass of StickyListHeadersAdapter");
			((StickyListHeadersAdapter)adapter).setDivider(divider);
			((StickyListHeadersAdapter)adapter).setDividerHeight(dividerHeight);
		}
		
		super.setAdapter(adapter);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if(header != null && areHeadersSticky){
			if(headerHasChanged){
				int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
				int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
				header.measure(widthMeasureSpec, heightMeasureSpec);
				header.layout(getLeft()+getPaddingLeft(), 0, getRight()-getPaddingRight(), headerHeight);
				headerHasChanged = false;
			}
			int top = headerBottomPosition - headerHeight;
			clippingRect.left = getPaddingLeft();
			clippingRect.right = getWidth()-getPaddingRight();
			clippingRect.bottom = top+headerHeight;
			if(clippingToPadding){
				clippingRect.top = getPaddingTop();
			}else{
				clippingRect.top = 0;
			}
			
			int tmp[] = null;
			if (headerPressed) {
			   //canvas.drawRect(clippingRect, headerBgMap.get(viewType));
				tmp = header.getDrawableState();
			}
			canvas.save();
			canvas.clipRect(clippingRect);
			canvas.translate(getPaddingLeft(), top);
			
			if (tmp != null && tmp.length > 0){
				int[] backup = new int[tmp.length];
				for (int i = 0; i < backup.length; i++) {
					backup[i] = android.R.attr.state_pressed;
				}
				((RelativeLayout)((FrameLayout)header).getChildAt(1)).getBackground().setState(backup);
			}
			header.draw(canvas);
			if(tmp != null)
				((RelativeLayout)((FrameLayout)header).getChildAt(1)).getBackground().setState(tmp);
			canvas.restore();
		}
	}

	@Override
	public void setClipToPadding(boolean clipToPadding) {
		super.setClipToPadding(clipToPadding);
		clippingToPadding  = clipToPadding;
		clipToPaddingHasBeenSet = true;
	}

	
	//called on pull to refresh
	public void resetHeader(){
		if(header != null){
			header.setVisibility(View.GONE);
			header = null;
			headerBottomPosition = 0;
	        headerHeight = -1;
	        headerHasChanged = true;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if(scrollListener!=null){
			scrollListener.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
		}
		if(getAdapter()==null || getAdapter().getCount() == 0) return;
		if(areHeadersSticky){
			if(getChildCount()!=0){
				if(lastWatchedViewHeader!=null){
					lastWatchedViewHeader.setVisibility(View.VISIBLE);
				}
				
				//changes viewToWatch and i from 1 to 0
				View viewToWatch = getChildAt(0);
				for(int i = 0;i<getChildCount();i++){
					
					int firstChildDistance;
					if(clippingToPadding){
						firstChildDistance = Math.abs((viewToWatch.getTop() - getPaddingTop()));
					}else{
						firstChildDistance = Math.abs(viewToWatch.getTop());
					}
					
					int secondChildDistance;
					if(clippingToPadding){
						secondChildDistance = Math.abs((getChildAt(i).getTop() - getPaddingTop()) - headerHeight);
					}else{
						secondChildDistance = Math.abs(getChildAt(i).getTop() - headerHeight);
					}

					try{
						if(!(Boolean)viewToWatch.getTag() || ((Boolean)getChildAt(i).getTag() && secondChildDistance<firstChildDistance)){
							viewToWatch = getChildAt(i);
						}
					}catch (NullPointerException e) {
						// TODO: deal with it?
					}
				}
				try{
					if((Boolean)viewToWatch.getTag()){
						if(headerHeight<0) headerHeight=viewToWatch.findViewById(R.id.__stickylistheaders_header_view).getHeight();
						
						if(firstVisibleItem == 0 && getChildAt(0).getTop()>0 && !clippingToPadding){
							headerBottomPosition = 0;
						}else{
							if(clippingToPadding){
								headerBottomPosition = Math.min(viewToWatch.getTop(), headerHeight+getPaddingTop());
								headerBottomPosition = headerBottomPosition<getPaddingTop() ? headerHeight+getPaddingTop() : headerBottomPosition;
							}else{
								headerBottomPosition = Math.min(viewToWatch.getTop(), headerHeight);
								headerBottomPosition = headerBottomPosition<0 ? headerHeight : headerBottomPosition;
							}
						}
						lastWatchedViewHeader = viewToWatch.findViewById(R.id.__stickylistheaders_header_view);
						if(headerBottomPosition == (clippingToPadding ? headerHeight+getPaddingTop() : headerHeight) && viewToWatch.getTop()<(clippingToPadding ? headerHeight+getPaddingTop() : headerHeight)){
							lastWatchedViewHeader.setVisibility(View.INVISIBLE);
						}else{
							lastWatchedViewHeader.setVisibility(View.VISIBLE);
						}
					}else{
						headerBottomPosition = headerHeight;
						if(clippingToPadding){
							headerBottomPosition += getPaddingTop();
						}
					}
				}catch (Exception e) {
					// TODO: deal with it?
				}
			}
			if(Build.VERSION.SDK_INT < 11){//work around to fix bug with firstVisibleItem being to high because listview does not take clipToPadding=false into account
				if(!clippingToPadding && getPaddingTop()>0){
					if(getChildAt(0).getTop() > 0){
						if(firstVisibleItem>0) firstVisibleItem -= 1;
					}
				}
			}

			//get sticky header from pull to refresh listview
			//coudlnt just cast it, so used reflection
			Field mAdapter = null;
			try {
				mAdapter = HeaderViewListAdapter.class.getDeclaredField("mAdapter");
			} catch (NoSuchFieldException e) {}
			mAdapter.setAccessible(true);
			StickyListHeadersAdapter adapter = null;
			try {
				adapter = (StickyListHeadersAdapter) mAdapter.get(view.getAdapter());
			} catch (Exception e) {}
			
			if(oldHeaderId != adapter.getHeaderId(firstVisibleItem)){
				headerHasChanged = true;
				header = adapter.getHeaderView(Math.max(0, firstVisibleItem-1), header);
				if(header != null)
					header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, headerHeight));
			}
			oldHeaderId = adapter.getHeaderId(firstVisibleItem);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollListener!=null){
			scrollListener.onScrollStateChanged(view, scrollState);
		}
	}

}
