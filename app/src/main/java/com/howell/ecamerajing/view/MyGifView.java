package com.howell.ecamerajing.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamjing.R;

public class MyGifView extends View implements Constable{


	private static final int DEFAULT_MOVIE_DURATION = 1000;  

	private int mMovieResourceId;  

	private Movie mMovie;  

	private long mMovieStart;  

	private int mCurrentAnimationTime = 0;  

	private float mLeft;  

	private float mTop;  

	private float mScale;  

	private int mMeasuredMovieWidth;  

	private int mMeasuredMovieHeight;  

	private boolean mVisible = true;  

	private volatile boolean mPaused = false;  

	private Handler handler;

	private Bitmap bitmap;
	
	private long movieStart = 0;
	private Movie [] movie = new Movie[2];
	private int movieIndex = 0;


	public int getMovieIndex() {
		return movieIndex;
	}

	public void setHandler(Handler handler){
		this.handler = handler;
	}
	
	public void setMovieIndex(int movieIndex) {
		this.movieIndex = movieIndex;
	}

	public MyGifView(Context context){
		super(context);
	}
	
	public MyGifView(Context context,int movieIndex) {
		super(context);
		this.movieIndex = movieIndex;

		movie[0] = Movie.decodeStream(getResources().openRawResource(R.raw.step_1_view)) ;
		movie[1] = Movie.decodeStream(getResources().openRawResource(R.raw.step_2_view)) ;
		// TODO Auto-generated constructor stub

		requestLayout();

	}

    public MyGifView(Context context, AttributeSet attrs) {  
        this(context, attrs, R.styleable.CustomTheme_gifViewStyle);  
    }  

	public MyGifView(Context context, AttributeSet attrs, int defStyle) {  
		super(context, attrs, defStyle);  
		setViewAttributes(context, attrs, defStyle);  
	}  

	private void setViewAttributes(Context context, AttributeSet attrs,  
			int defStyle) {  
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {  
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);  
		}  
		// 从描述文件中读出gif的值，创建出Movie实例  
		final TypedArray array = context.obtainStyledAttributes(attrs,  
				R.styleable.GifView, defStyle, R.style.Widget_GifView);  
		mMovieResourceId = array.getResourceId(R.styleable.GifView_gif, -1);  
		mPaused = array.getBoolean(R.styleable.GifView_paused, false);  
		array.recycle();  
		if (mMovieResourceId != -1) {  
			mMovie = Movie.decodeStream(getResources().openRawResource(  
					mMovieResourceId));  
		}  
	}  

	public void setMovieResource(int movieResId) {  
		this.mMovieResourceId = movieResId;  
		mMovie = Movie.decodeStream(getResources().openRawResource(  
				mMovieResourceId));  
		requestLayout();  
	}  

	public void setMovie(Movie movie) {  
		this.mMovie = movie;  
		requestLayout();  
	}  

	public Movie getMovie() {  
		return mMovie;  
	}  

	public void setMovieTime(int time) {  
		mCurrentAnimationTime = time;  
		invalidate();  
	}  

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		requestLayout(); 
//		Log.i("123", "set bitmap");
		
//		new Runnable() {
//			public void run() {
//				invalidate();
//			}
//		}.run();
	}

	public void showBitmap(){
		invalidate();  
	}
	
	/** 
	 * 设置暂停 
	 *  
	 * @param paused 
	 */  
	public void setPaused(boolean paused) {  
		this.mPaused = paused;  
		if (!paused) {  
			mMovieStart = android.os.SystemClock.uptimeMillis()  
					- mCurrentAnimationTime;  
		}  
		invalidate();  
	}  

	/** 
	 * 判断gif图是否停止了 
	 *  
	 * @return 
	 */  
	public boolean isPaused() {  
		return this.mPaused;  
	}  

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
		if (mMovie != null) {  
			int movieWidth = mMovie.width();  
			int movieHeight = mMovie.height();  
			int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);  
			float scaleW = (float) movieWidth / (float) maximumWidth;  
			mScale = 1f / scaleW;  
			mMeasuredMovieWidth = maximumWidth;  
			mMeasuredMovieHeight = (int) (movieHeight * mScale);  
//			Log.e("123","mw="+mMeasuredMovieWidth+" mh="+mMeasuredMovieHeight+" w="+movieWidth+ " h="+movieHeight
//					+" wms"+widthMeasureSpec+ " hms"+heightMeasureSpec);
			setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);  
		} else {  
			if(bitmap==null){
			setMeasuredDimension(getSuggestedMinimumWidth(),  
					getSuggestedMinimumHeight());  
			}else{
				int movieWidth = bitmap.getWidth();
				
				int movieHeight = bitmap.getHeight();
				int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);  
				float scaleW = (float) movieWidth / (float) maximumWidth;  
				mScale = 1f / scaleW;  
				mMeasuredMovieWidth = maximumWidth;  
				mMeasuredMovieHeight = (int) (movieHeight * mScale);  
//				Log.e("123","mw="+mMeasuredMovieWidth+" mh="+mMeasuredMovieHeight+" w="+movieWidth+ " h="+movieHeight
//						+" wms"+widthMeasureSpec+ " hms"+heightMeasureSpec);
				setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);  
				
			}
		}  
	}  

	 @Override  
	    protected void onLayout(boolean changed, int l, int t, int r, int b) {  
	        super.onLayout(changed, l, t, r, b);  
	        mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;  
	        mTop = (getHeight() - mMeasuredMovieHeight) / 2f;  
	        mVisible = getVisibility() == View.VISIBLE;  
	    }  
	  
	    @Override  
	    protected void onDraw(Canvas canvas) {  
	    	
	        if (mMovie != null) {  
	        
	            if (!mPaused) {  
	                updateAnimationTime();  
	                drawMovieFrame(canvas);  
	                invalidateView();  
	            } else {  
	                drawMovieFrame(canvas);  
	            }  
	        }  else{
//	        	super.onDraw(canvas);
//	        	Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.course_wifi); 
//	        	canvas.setBitmap(bitmap);
	        	if(bitmap!=null){
	        		Log.i("123", "on draw   set bitmap");
	        		//canvas.setBitmap(bitmap);
	        		
//	        		canvas.save(Canvas.MATRIX_SAVE_FLAG);  
//	       	        canvas.scale(mScale, mScale);  
//	       	        Paint paint = new Paint(); 
//	        		canvas.drawBitmap(bitmap, 0, 0,paint);
	        		Paint paint = new Paint();
	        		paint.setColor(Color.BLACK);
	        		paint.setStyle(Style.FILL);
	        		canvas.drawBitmap(bitmap, 0, 0,null);
//	        		canvas.restore();  
	        	}else{
	        		Log.i("123", "bit == null");
	        	}
	        }
	    }  

	    @SuppressLint("NewApi")  
	    private void invalidateView() {  
	        if (mVisible) {  
	            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {  
	                postInvalidateOnAnimation();  
	            } else {  
	                invalidate();  
	            }  
	        }  
	    }  
	  
	    private void updateAnimationTime() {  
	        long now = android.os.SystemClock.uptimeMillis();  
	        // 如果第一帧，记录起始时间  
	        if (mMovieStart == 0) {  
	            mMovieStart = now;  
	        }  
	        // 取出动画的时长  
	        int dur = mMovie.duration();  
	       
	        if (dur == 0) {  
	            dur = DEFAULT_MOVIE_DURATION;  
	        }  
	        // 算出需要显示第几帧  
	        mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);  
//	        Log.i("123", "dur="+dur + " cur="+mCurrentAnimationTime + "time="+(now - mMovieStart));
	        if ((now - mMovieStart) > dur) {
//				Log.e("123", "over");
	        	handler.sendEmptyMessage(COURSE_MSG_GIF_FINISH);
				mPaused = true;
			}
	    }  
	  
	    private void drawMovieFrame(Canvas canvas) {  
	        // 设置要显示的帧，绘制即可  
	        mMovie.setTime(mCurrentAnimationTime);  
	        canvas.save(Canvas.MATRIX_SAVE_FLAG);  
	        canvas.scale(mScale, mScale);  
	        mMovie.draw(canvas, mLeft / mScale, mTop / mScale);  
	        canvas.restore();  
	    }  

	    @SuppressLint("NewApi")  
	    @Override  
	    public void onScreenStateChanged(int screenState) {  
	        super.onScreenStateChanged(screenState);  
	        mVisible = screenState == SCREEN_STATE_ON;  
	        invalidateView();  
	    }  
	  
	    @SuppressLint("NewApi")  
	    @Override  
	    protected void onVisibilityChanged(View changedView, int visibility) {  
	        super.onVisibilityChanged(changedView, visibility);  
	        mVisible = visibility == View.VISIBLE;  
	        invalidateView();  
	    }  
	  
	    @Override  
	    protected void onWindowVisibilityChanged(int visibility) {  
	        super.onWindowVisibilityChanged(visibility);  
	        mVisible = visibility == View.VISIBLE;  
	        invalidateView();  
	    }  

	


}
