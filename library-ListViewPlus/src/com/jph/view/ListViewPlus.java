package com.jph.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.jph.lp.R;

/** 
 * 基于ListView的自定义控件实现上拉加载下拉刷新
 * @author JPH
 * @date 2015-3-10 下午1:01:56
 */
public class ListViewPlus extends ListView implements OnScrollListener {
	/**区分当前操作是刷新还是加载**/
	public static final int REFRESH = 0;
	public static final int LOAD = 1;
	private float mLastY = -1; // save event y
	private Scroller mScroller; // used for scroll back
	private OnScrollListener mScrollListener; // user's scroll listener

	// the interface to trigger refresh and load more.
	private ListViewPlusListener mListViewListener;

	// -- header view
	private ListViewPlusHeader mHeaderView;
	// header view content, use it to calculate the Header's height. And hide it
	// when disable pull refresh.
	private RelativeLayout mHeaderViewContent;
	private TextView mHeaderTimeView;
	private int mHeaderViewHeight; // header view's height
	private boolean mEnablePullRefresh = true;
	private boolean mPullRefreshing = false; // is refreashing.

	// -- footer view
	private ListViewPlusFooter mFooterView;
	private boolean mEnablePullLoad;
	private boolean mPullLoading;
	private boolean mIsFooterReady = false;

	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;

	// for mScroller, scroll back from header or footer.
	private int mScrollBack;
	private int minItemCount=3;
	private final static int SCROLLBACK_HEADER = 0;
	private final static int SCROLLBACK_FOOTER = 1;

	private final static int SCROLL_DURATION = 400; // scroll back duration
	private final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >= 50px
														// at bottom, trigger
														// load more.
	private final static float OFFSET_RADIO = 1.8f; // support iOS like pull
													// feature.

	/**
	 * @param context
	 */
	public ListViewPlus(Context context) {
		super(context);
		initWithContext(context);
	}

	public ListViewPlus(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWithContext(context);
	}

	public ListViewPlus(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWithContext(context);
	}

	private void initWithContext(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());
		// listview_plus need the scroll event, and it will dispatch the event to
		// user's listener (as a proxy).
		super.setOnScrollListener(this);

		// init header view
		mHeaderView = new ListViewPlusHeader(context);
		mHeaderViewContent = (RelativeLayout) mHeaderView
				.findViewById(R.id.listview_plus_header_content);
		mHeaderTimeView = (TextView) mHeaderView
				.findViewById(R.id.listview_plus_header_time);
		addHeaderView(mHeaderView);

		// init footer view
		mFooterView = new ListViewPlusFooter(context);

		// init header height
		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						mHeaderViewHeight = mHeaderViewContent.getHeight();
						getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
					}
				});
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// make sure ListViewPlusFooter is the last footer view, and only add once.
		if (mIsFooterReady == false) {
			mIsFooterReady = true;
			addFooterView(mFooterView);
		}
		super.setAdapter(adapter);
	}

	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderViewContent.setVisibility(View.INVISIBLE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * enable or disable pull up load more feature.
	 * 
	 * @param enable
	 */
	public void setLoadEnable(boolean enable) {
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
		} else {
			mPullLoading = false;
			mFooterView.show();
			mFooterView.setState(ListViewPlusFooter.STATE_NORMAL);
			// both "pull up" and "click" will invoke load more.
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startLoadMore();
				}
			});
		}
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
			setRefreshTime(getCurrentTime());
		}
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(ListViewPlusFooter.STATE_NORMAL);
		}
	}

	/**
	 * set last refresh time
	 * 
	 * @param time
	 */
	public void setRefreshTime(String time) {
		mHeaderTimeView.setText(time);
	}

	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnXScrollListener) {
			OnXScrollListener l = (OnXScrollListener) mScrollListener;
			l.onXScrolling(this);
		}
	}

	private void updateHeaderHeight(float delta) {
		mHeaderView.setVisiableHeight((int) delta
				+ mHeaderView.getVisiableHeight());
		if (mEnablePullRefresh && !mPullRefreshing) { // 未处于刷新状态，更新箭头
			if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
				mHeaderView.setState(ListViewPlusHeader.STATE_READY);
			} else {
				mHeaderView.setState(ListViewPlusHeader.STATE_NORMAL);
			}
		}
		setSelection(0); // scroll to top each time
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0) // not visible.
			return;
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int finalHeight = 0; // default: scroll back to dismiss header.
		// is refreshing, just scroll back to show all the header.
		if (mPullRefreshing && height > mHeaderViewHeight) {
			finalHeight = mHeaderViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height,
				SCROLL_DURATION);
		// trigger computeScroll
		invalidate();
	}

	private void updateFooterHeight(float delta) {
		int height = mFooterView.getBottomMargin() + (int) delta;
		if (mEnablePullLoad && !mPullLoading) {
			if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load
													// more.
				mFooterView.setState(ListViewPlusFooter.STATE_READY);
			} else {
				mFooterView.setState(ListViewPlusFooter.STATE_NORMAL);
			}
		}
		mFooterView.setBottomMargin(height);

		// setSelection(mTotalItemCount - 1); // scroll to bottom
	}

	private void resetFooterHeight() {
		int bottomMargin = mFooterView.getBottomMargin();
		if (bottomMargin > 0) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
					SCROLL_DURATION);
			invalidate();
		}
	}

	private void startLoadMore() {
		mPullLoading = true;
		mFooterView.setState(ListViewPlusFooter.STATE_LOADING);
		if (mListViewListener != null) {
			mListViewListener.onLoadMore();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			System.out.println("数据监测：" + getFirstVisiblePosition() + "---->"
					+ getLastVisiblePosition());
			if (getFirstVisiblePosition() == 0
					&& (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
				// the first item is showing, header has shown or pull down.
				updateHeaderHeight(deltaY / OFFSET_RADIO);
				invokeOnScrolling();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1
					&& (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
				// last item, already pulled up or want to pull up.
				updateFooterHeight(-deltaY / OFFSET_RADIO);
			}
			break;
		default:
			mLastY = -1; // reset
			if (getFirstVisiblePosition() == 0) {
				// invoke refresh
				if (mEnablePullRefresh
						&& mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
					mPullRefreshing = true;
					mHeaderView.setState(ListViewPlusHeader.STATE_REFRESHING);
					if (mListViewListener != null) {
						mListViewListener.onRefresh();
					}
				}
				resetHeaderHeight();
			}
			if (getLastVisiblePosition() == mTotalItemCount - 1) {
				// invoke load more.
				if (mEnablePullLoad
						&& mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
					startLoadMore();
				}
				resetFooterHeight();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
			} else {
				mFooterView.setBottomMargin(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// send to user's listener
		if(mFooterView!=null){
			if (totalItemCount<minItemCount+2) {//如果ListView中只包含头布局和尾布局（及ListView的adapter没有数据），则不显示脚布局文本
				mFooterView.getmHintView().setText("");
			}else {
				if(mFooterView.getmHintView().getText().toString().equals(""))mFooterView.getmHintView().
					setText(getResources().getString(R.string.listview_plus_footer_hint_normal));
			}
		}
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}
	public void setListViewPlusListener(ListViewPlusListener l) {
		mListViewListener = l;
	}

	/**
	 * you can listen ListView.OnScrollListener or this one. it will invoke
	 * onXScrolling when header/footer scroll back.
	 */
	public interface OnXScrollListener extends OnScrollListener {
		public void onXScrolling(View view);
	}

	/**
	 * 实现这个接口可以，获取下拉加载上拉刷新事件
	 */
	public interface ListViewPlusListener {
		public void onRefresh();
		public void onLoadMore();
	}
	public String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.getDefault());
		String currentTime = sdf.format(new Date());
		return currentTime;
	}
	/**
	 * ListView的Footer类
	 * @author JPH
	 */
	class ListViewPlusFooter extends LinearLayout {
		public final static int STATE_NORMAL = 0;
		public final static int STATE_READY = 1;
		public final static int STATE_LOADING = 2;
		private Context mContext;
		private View mContentView;
		private View mProgressBar;
		private TextView mHintView;
		
		public ListViewPlusFooter(Context context) {
			super(context);
			initView(context);
		}
		public ListViewPlusFooter(Context context, AttributeSet attrs) {
			super(context, attrs);
			initView(context);
		}
		public void setState(int state) {
			mHintView.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.INVISIBLE);
			mHintView.setVisibility(View.INVISIBLE);
			if (state == STATE_READY) {
				mHintView.setVisibility(View.VISIBLE);
				mHintView.setText(R.string.listview_plus_footer_hint_ready);
			} else if (state == STATE_LOADING) {
				mProgressBar.setVisibility(View.VISIBLE);
			} else {
				mHintView.setVisibility(View.VISIBLE);
				mHintView.setText(R.string.listview_plus_footer_hint_normal);
			}
		}
		
		public void setBottomMargin(int height) {
			if (height < 0) return ;
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
			lp.bottomMargin = height;
			mContentView.setLayoutParams(lp);
		}
		
		public int getBottomMargin() {
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
			return lp.bottomMargin;
		}
		
		/**
		 * normal status
		 */
		public void normal() {
			mHintView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
		}
		
		
		/**
		 * loading status 
		 */
		public void loading() {
			mHintView.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
		}
		
		/**
		 * hide footer when disable pull load more
		 */
		public void hide() {
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
			lp.height = 0;
			mContentView.setLayoutParams(lp);
		}
		
		/**
		 * show footer
		 */
		public void show() {
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
			lp.height = LayoutParams.WRAP_CONTENT;
			mContentView.setLayoutParams(lp);
		}
		
		private void initView(Context context) {
			mContext = context;
			LinearLayout moreView = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.listview_plus_footer, null);
			addView(moreView);
			moreView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			
			mContentView = moreView.findViewById(R.id.listview_plus_footer_content);
			mProgressBar = moreView.findViewById(R.id.listview_plus_footer_progressbar);
			mHintView = (TextView)moreView.findViewById(R.id.listview_plus_footer_hint_textview);
		}
		public TextView getmHintView() {
			return mHintView;
		}
	}
	/**
	 * listView的头布局
	 * @author JPH
	 */
	class ListViewPlusHeader extends LinearLayout {
		private LinearLayout mContainer;
		private ImageView mArrowImageView;
		private ProgressBar mProgressBar;
		private TextView mHintTextView;
		private int mState = STATE_NORMAL;
		private Animation mRotateUpAnim;
		private Animation mRotateDownAnim;	
		private final int ROTATE_ANIM_DURATION = 180;	
		public final static int STATE_NORMAL = 0;
		public final static int STATE_READY = 1;
		public final static int STATE_REFRESHING = 2;

		public ListViewPlusHeader(Context context) {
			super(context);
			initView(context);
		}

		/**
		 * @param context
		 * @param attrs
		 */
		public ListViewPlusHeader(Context context, AttributeSet attrs) {
			super(context, attrs);
			initView(context);
		}

		private void initView(Context context) {
			// 初始情况，设置下拉刷新view高度为0
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0);
			mContainer = (LinearLayout) LayoutInflater.from(context).inflate(
					R.layout.listview_plus_header, null);
			addView(mContainer, lp);
			setGravity(Gravity.BOTTOM);

			mArrowImageView = (ImageView)findViewById(R.id.listview_plus_header_arrow);
			mHintTextView = (TextView)findViewById(R.id.listview_plus_header_hint_textview);
			mProgressBar = (ProgressBar)findViewById(R.id.listview_plus_header_progressbar);
			
			mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
					0.5f);
			mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
			mRotateUpAnim.setFillAfter(true);
			mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
					0.5f);
			mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
			mRotateDownAnim.setFillAfter(true);
		}

		public void setState(int state) {
			if (state == mState) return ;
			
			if (state == STATE_REFRESHING) {	// 显示进度
				mArrowImageView.clearAnimation();
				mArrowImageView.setVisibility(View.INVISIBLE);
				mProgressBar.setVisibility(View.VISIBLE);
			} else {	// 显示箭头图片
				mArrowImageView.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.INVISIBLE);
			}
			
			switch(state){
			case STATE_NORMAL:
				if (mState == STATE_READY) {
					mArrowImageView.startAnimation(mRotateDownAnim);
				}
				if (mState == STATE_REFRESHING) {
					mArrowImageView.clearAnimation();
				}
				mHintTextView.setText(R.string.listview_plus_header_hint_normal);
				break;
			case STATE_READY:
				if (mState != STATE_READY) {
					mArrowImageView.clearAnimation();
					mArrowImageView.startAnimation(mRotateUpAnim);
					mHintTextView.setText(R.string.listview_plus_header_hint_ready);
				}
				break;
			case STATE_REFRESHING:
				mHintTextView.setText(R.string.listview_plus_header_hint_loading);
				break;
				default:
			}
			
			mState = state;
		}
		
		public void setVisiableHeight(int height) {
			if (height < 0)
				height = 0;
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer
					.getLayoutParams();
			lp.height = height;
			mContainer.setLayoutParams(lp);
		}
		public int getVisiableHeight() {
			return mContainer.getHeight();
		}
	}
}
