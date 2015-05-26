package com.samuel;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 图片右下角单点控制图片做旋转,移动，缩放
 * 
 * @author samuel
 *
 */
public class ControlMaskImageView extends View {

	private static final int NONE = 0;
	//拖动，改变大小和角度， 中心点不变
	private static final int DRAG = 1;
	//移动
	private static final int TRANSLATE = 2;
	//图片外部点击
	private static final int OUTSIDE = 3;

	private int mCurrentMode = NONE;

	private Matrix mMatrix = new Matrix();
	private Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private RectF mContentDstRect, mContentSrcRect;
	private RectF mCloseBtnDstRect;
	private RectF mDragBtnDstRect;

	private PointF mContentDstLeftTopPoint, mContentDstRightTopPoint, mContentDstLeftBottomPoint, mContentDstRigintBottomPoint;

	private Bitmap mContentBitmap, mCloseBitmap, mDragBitmap;

	private float mLastX, mLastY;
	private float mCenterX, mCenterY;
	/**
	 * 旋转和缩放围绕的中心点
	 */
	private PointF mOriginPoint;

	private boolean mIsTouchMode;

	/**
	 * 图片旋转的累计角度
	 */
	private float mDegreesRotate;

	private int mBitmapWidth, mBitmapHeight;

	public ControlMaskImageView(Context context) {
		super(context);
		initialize(context);
	}

	public ControlMaskImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	public ControlMaskImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	private void initialize(Context context) {
		mContentBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_mask);
		mCloseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bt2);
		mDragBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bt1);

		mBitmapWidth = mContentBitmap.getWidth();
		mBitmapHeight = mContentBitmap.getHeight();
		mContentDstRect = new RectF();
		mCloseBtnDstRect = new RectF();
		mDragBtnDstRect = new RectF();

		mContentSrcRect = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
		mContentDstLeftTopPoint = new PointF();
		mContentDstRightTopPoint = new PointF();
		mContentDstLeftBottomPoint = new PointF();
		mContentDstRigintBottomPoint = new PointF();

		mMatrix = null;

		mLinePaint.setColor(Color.WHITE);
		mLinePaint.setStrokeWidth(3);
		mLinePaint.setStyle(Paint.Style.STROKE);
	}

	public void setBitmap(Bitmap bitmap) {
		mContentBitmap = bitmap;

		mBitmapWidth = mContentBitmap.getWidth();
		mBitmapHeight = mContentBitmap.getHeight();
		mContentDstRect = new RectF();
		mCloseBtnDstRect = new RectF();
		mDragBtnDstRect = new RectF();

		mContentSrcRect = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
		mContentDstLeftTopPoint = new PointF();
		mContentDstRightTopPoint = new PointF();
		mContentDstLeftBottomPoint = new PointF();
		mContentDstRigintBottomPoint = new PointF();
		mMatrix = null;

		invalidate();
	}

	protected void onDraw(Canvas canvas) {

		if (mMatrix == null) {// 初始化位置,默认控件所占区域的中心
			int width = getWidth();
			int height = getHeight();
			mMatrix = new Matrix();
			float initX = (width - mBitmapWidth) / 2;
			float initY = (height - mBitmapHeight) / 2;
			mMatrix.postTranslate(initX, initY);
			matrixCheck();
		}

		canvas.save();
		canvas.drawBitmap(mContentBitmap, mMatrix, null);
		canvas.restore();

		if (mIsTouchMode) {
			drawLine(canvas);
		} else {
			if (mCurrentMode != OUTSIDE) {
				Log.e("simon", "mDegreesRotate>>" + mDegreesRotate);
				drawLine(canvas);
				canvas.save();
				canvas.rotate(mDegreesRotate, mContentDstLeftTopPoint.x, mContentDstLeftTopPoint.y);
				canvas.drawBitmap(mCloseBitmap, mCloseBtnDstRect.left, mCloseBtnDstRect.top, null);
				canvas.restore();

				canvas.save();
				canvas.rotate(mDegreesRotate, mContentDstRigintBottomPoint.x, mContentDstRigintBottomPoint.y);
				canvas.drawBitmap(mDragBitmap, mDragBtnDstRect.left, mDragBtnDstRect.top, null);
				canvas.restore();
			}
		}
	}

	/**
	 * 画图片实际区域矩形框
	 * @param canvas
	 */
	private void drawLine(Canvas canvas) {
		canvas.drawLine(mContentDstLeftTopPoint.x, mContentDstLeftTopPoint.y, mContentDstRightTopPoint.x, mContentDstRightTopPoint.y, mLinePaint);
		canvas.drawLine(mContentDstRightTopPoint.x, mContentDstRightTopPoint.y, mContentDstRigintBottomPoint.x, mContentDstRigintBottomPoint.y, mLinePaint);
		canvas.drawLine(mContentDstRigintBottomPoint.x, mContentDstRigintBottomPoint.y, mContentDstLeftBottomPoint.x, mContentDstLeftBottomPoint.y, mLinePaint);
		canvas.drawLine(mContentDstLeftBottomPoint.x, mContentDstLeftBottomPoint.y, mContentDstLeftTopPoint.x, mContentDstLeftTopPoint.y, mLinePaint);
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mIsTouchMode = true;
			mCurrentMode = NONE;
			mLastX = event.getX();
			mLastY = event.getY();
			mCenterX = mContentDstRect.centerX();
			mCenterY = mContentDstRect.centerY();
			Log.e("simon", "centerX>>" + mCenterX + "  centerY" + mCenterY);
			mOriginPoint = new PointF(mCenterX, mCenterY);
			if (isInsideRF(mLastX, mLastY, mCloseBtnDstRect)) {//清除
				if (getParent() != null && getParent() instanceof ViewGroup) {
					((ViewGroup) getParent()).removeView(this);
				}
				return false;
			} else if (isInsideRF(mLastX, mLastY, mDragBtnDstRect)) {//准备拖动
				mCurrentMode = DRAG;
			} else if (isInsideContent(mLastX, mLastY)) {//移动
				mCurrentMode = TRANSLATE;
			} else {//不在范围内
				mCurrentMode = OUTSIDE;
			}
		case MotionEvent.ACTION_MOVE:
			if (mCurrentMode == DRAG) {
				Log.e("simon", "mContentDstRect>>" + mContentDstRect.toString());
				PointF lastPoint = new PointF(mLastX, mLastY);
				PointF currentPoint = new PointF(event.getX(), event.getY());
				float scale = getScale(mOriginPoint, lastPoint, currentPoint);
				mMatrix.postScale(scale, scale, mCenterX, mCenterY);// 縮放
				float angle = getAngle(mOriginPoint, lastPoint, currentPoint);
				mDegreesRotate += angle;
				mMatrix.postRotate(angle, mCenterX, mCenterY);// 旋轉
				matrixCheck();
				invalidate();
			} else if (mCurrentMode == TRANSLATE) {
				mMatrix.postTranslate(event.getX() - mLastX, event.getY() - mLastY);// 平移
				matrixCheck();
				invalidate();
			}
			mLastX = event.getX();
			mLastY = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			mIsTouchMode = false;
			invalidate();
			break;
		}
		return true;
	}

	/**
	 * 矩阵变换后改变参数，参数检查
	 * 
	 * @return
	 */
	private boolean matrixCheck() {
		reSetDstRect(mMatrix, mContentDstRect, mContentSrcRect);
		refreshBitmapVerticesPoint();
		reSetCloseBtnRect();
		reSetDragBtnRect();
		return true;//可以在这里做一些检查
	}

	/**
	 * 获取矩阵变换后，图片实际的四个顶点
	 */
	private void refreshBitmapVerticesPoint() {
		float[] f = new float[9];
		mMatrix.getValues(f);
		mContentDstLeftTopPoint.x = f[0] * 0 + f[1] * 0 + f[2];
		mContentDstLeftTopPoint.y = f[3] * 0 + f[4] * 0 + f[5];
		mContentDstRightTopPoint.x = f[0] * mBitmapWidth + f[1] * 0 + f[2];
		mContentDstRightTopPoint.y = f[3] * mBitmapWidth + f[4] * 0 + f[5];
		mContentDstLeftBottomPoint.x = f[0] * 0 + f[1] * mBitmapHeight + f[2];
		mContentDstLeftBottomPoint.y = f[3] * 0 + f[4] * mBitmapHeight + f[5];
		mContentDstRigintBottomPoint.x = f[0] * mBitmapWidth + f[1] * mBitmapHeight + f[2];
		mContentDstRigintBottomPoint.y = f[3] * mBitmapWidth + f[4] * mBitmapHeight + f[5];
	}

	/**
	 * 刷新图片矩阵变换后，实际占有的矩形区域大小
	 * @param matrix
	 * @param dstRect
	 * @param srcRect
	 */
	public void reSetDstRect(Matrix matrix, RectF dstRect, RectF srcRect) {
		matrix.mapRect(dstRect, srcRect);
	}

	/**
	 * 重新获取关闭按钮实际所在区域
	 */
	private void reSetCloseBtnRect() {
		mCloseBtnDstRect = new RectF(mContentDstLeftTopPoint.x - (mCloseBitmap.getWidth() * 1.0f / 2), mContentDstLeftTopPoint.y - (mCloseBitmap.getHeight() * 1.0f / 2), mContentDstLeftTopPoint.x + (mCloseBitmap.getWidth() * 1.0f / 2), mContentDstLeftTopPoint.y + (mCloseBitmap.getHeight() * 1.0f / 2));
	}

	/**
	 * 重新获取拖动按钮实际所在区域
	 */
	private void reSetDragBtnRect() {
		mDragBtnDstRect = new RectF(mContentDstRigintBottomPoint.x - (mDragBitmap.getWidth() * 1.0f / 2), mContentDstRigintBottomPoint.y - (mDragBitmap.getHeight() * 1.0f / 2), mContentDstRigintBottomPoint.x + (mDragBitmap.getWidth() * 1.0f / 2), mContentDstRigintBottomPoint.y + (mDragBitmap.getHeight() * 1.0f / 2));
	}

	/**
	 * 矩形区间内是否包含某一个点
	 * @param x
	 * @param y
	 * @param rect
	 * @return
	 */
	public boolean isInsideRF(float x, float y, RectF rect) {
		if (rect.contains(x, y))
			return true;
		return false;
	}

	/**
	 * 图片实际内容区域，是否包含某一个坐标
	 * @param pointX
	 * @param pointY
	 * @return
	 */
	private boolean isInsideContent(float pointX, float pointY) {
		PointF pointF = new PointF(pointX, pointY);
		PointF[] vertexPointFs = new PointF[] { mContentDstLeftTopPoint, mContentDstRightTopPoint, mContentDstRigintBottomPoint, mContentDstLeftBottomPoint };
		int nCross = 0;
		for (int i = 0; i < vertexPointFs.length; i++) {
			PointF p1 = vertexPointFs[i];
			PointF p2 = vertexPointFs[(i + 1) % vertexPointFs.length];
			if (p1.y == p2.y)
				continue;
			if (pointF.y < Math.min(p1.y, p2.y))
				continue;
			if (pointF.y >= Math.max(p1.y, p2.y))
				continue;
			double x = (double) (pointF.y - p1.y) * (double) (p2.x - p1.x) / (double) (p2.y - p1.y) + p1.x;
			if (x > pointF.x)
				nCross++;
		}
		return (nCross % 2 == 1);
	}

	/**
	 * 求角∠P2P1P3 (带正负角度(顺时针逆时针))
	 * @param p1    圆心 (围绕旋转的点，这里为Rect的中心点)
	 * @param p2    老坐标
	 * @param p3    新坐标 
	 * @return
	 */
	private float getAngle(PointF p1, PointF p2, PointF p3) {

		float dx = p3.x - p1.x;
		float dy = p3.y - p1.y;
		double a = Math.atan2(dy, dx);

		float dpx = p2.x - p1.x;
		float dpy = p2.y - p1.y;
		double b = Math.atan2(dpy, dpx);

		double diff = a - b;
		return (float) Math.toDegrees(diff);
	}

	/**
	 * 求角∠P2P1P3 (单纯角度)
	 * @param p1    圆心 (围绕旋转的点，这里为Rect的中心点)
	 * @param p2    老坐标
	 * @param p3    新坐标 
	 * @return
	 */
	private float getAngle2(PointF p1, PointF p2, PointF p3) {

		double p1p2 = Math.hypot(p1.x - p2.x, p1.y - p2.y);// p2->p1 
		double p1p3 = Math.hypot(p1.x - p3.x, p1.y - p3.y);// p3->p1
		double p2p3 = Math.hypot(p3.x - p2.x, p3.y - p2.y);// p2->p3 
		double diff = Math.acos((Math.pow(p1p2, 2) + Math.pow(p1p3, 2) - Math.pow(p2p3, 2)) / (2 * p1p3 * p1p2));
		return (float) Math.toDegrees(diff);
		//公式  =  arcos((P12*P12 + P13*P13 - P23*P23) / (2 * P12 * P13))
		//P12 就是 p1->p2的距离。两点距离算法为 sqrt((P1x - P2x)2 + (P1y - P2y)2)
		//		double p1p2 = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));  
		//		double p1p3 = Math.sqrt(Math.pow(p1.x - p3.x, 2) + Math.pow(p1.y - p3.y, 2)); 
		//		double p2p3 = Math.sqrt(Math.pow(p3.x - p2.x, 2) + Math.pow(p3.y - p2.y, 2));
		//		double diff = Math.acos((p1p3 * p1p3 + p1p2 * p1p2 - p2p3 * p2p3) / (2 * p1p3 * p1p2));
		//		return Math.toDegrees(diff);
	}

	/**
	 *  求以p1p3为半径的圆 /以p1p2为半径的圆，得到缩放的比例
	 *   
	 * @param p1    圆心 (围绕旋转的点，这里为Rect的中心点)
	 * @param p2    老坐标
	 * @param p3    新坐标 
	 * @return
	 */
	private float getScale(PointF p1, PointF p2, PointF p3) {
		double p1p2 = Math.hypot(p1.x - p2.x, p1.y - p2.y);
		double p1p3 = Math.hypot(p1.x - p3.x, p1.y - p3.y);
		//新半径/老半径
		return (float) (p1p3 / p1p2);
	}

	//	// 将移动，缩放以及旋转后的图层保存为新图片
	//	// 本例中沒有用到該方法，需要保存圖片的可以參考
	//	public Bitmap CreatNewPhoto() {
	//		Bitmap bitmap = Bitmap.createBitmap(widthScreen, heightScreen, Config.ARGB_8888); // 背景图片
	//		Canvas canvas = new Canvas(bitmap); // 新建画布
	//		canvas.drawBitmap(mContentBitmap, matrix, null); // 画图片
	//		canvas.save(Canvas.ALL_SAVE_FLAG); // 保存画布
	//		canvas.restore();
	//		return bitmap;
	//	}
	//
	//	public boolean isInsideContent(float pointX, float pointY) {
	//
	//		PointF pointToCheck = new PointF(x, y);
	//		Line line1 = Line.getLine(mContentDstLeftTopPoint, mContentDstRightTopPoint), line2 = Line.getLine(mContentDstRightTopPoint, mContentDstRigintBottomPoint), line3 = Line.getLine(mContentDstRigintBottomPoint, mContentDstLeftBottomPoint), line4 = Line.getLine(mContentDstLeftBottomPoint, mContentDstLeftTopPoint);
	//		if (Line.getValue(line1, pointToCheck) >= 0 && Line.getValue(line2, pointToCheck) >= 0 && Line.getValue(line3, pointToCheck) <= 0 && Line.getValue(line4, pointToCheck) <= 0) {
	//			return true;
	//		} else
	//			return false;
	//	}
	//
	//	private static class Line {
	//		float a;
	//		float b;
	//		float c;
	//
	//		Line(float a, float b, float c) {
	//			this.a = a;
	//			this.b = b;
	//			this.c = c;
	//		}
	//
	//		static Line getLine(PointF p1, PointF p2) {
	//			//cy=ax+b
	//			float a = (p1.y - p2.y) / (p1.x - p2.x), b, c;
	//			if (Float.isNaN(a))
	//				throw new NumberFormatException("输入的两点有重合");
	//			else if (Float.isInfinite(a)) {
	//				a = 1;
	//				b = p1.x;
	//				c = 0;
	//			} else {
	//				b = (p1.x * p2.y - p2.x * p1.y) / (p1.x - p2.x);
	//				c = 1;
	//			}
	//			return new Line(a, b, c);
	//		}
	//
	//		static float getValue(Line l, PointF p) {
	//			return l.a * p.x + l.b - l.c * p.y;
	//		}
	//	}

}