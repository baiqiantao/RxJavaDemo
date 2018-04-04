package rx.test.bqt.com.rxjavademo.net;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.Locale;

//https://github.com/woxingxiao/DashboardView/tree/master/app/src/main/java/com/xw/sample/dashboardviewdemo
public class DashboardView extends View {
	
	private static final int M_START_ANGLE = 180; // 起始角度
	private static final int M_SWEEP_ANGLE = 180; // 绘制角度
	private static final float mMin = 0; // 最小值
	private static final float mMax = 5; // 最大值，对应5MB/S
	private static final int M_SECTION = 10; // 值域（mMax-mMin）等分份数
	private static final int M_PORTION = 10; // 一个mSection等分份数
	private static final String M_HEADER_TEXT = "MB/S"; // 表头
	private static final int SWEEP_ANGLE_COLOR = 0x880000ff;//圆弧颜色
	private static final int REAL_TIME_VALUE_COLOR = 0xffff0000;//实时读数的颜色
	private static final boolean IS_SHOW_VALUE = true; // 是否显示实时读数
	private int mRadius; // 扇形半径
	private float mRealTimeValue = mMin; // 实时读数
	private int mStrokeWidth; // 画笔宽度
	private int mLength1; // 长刻度的相对圆弧的长度
	private int mLength2; // 刻度读数顶部的相对圆弧的长度
	private int mPLRadius; // 指针长半径
	private int mPSRadius; // 指针短半径
	
	private int mPadding;
	private float mCenterX, mCenterY; // 圆心坐标
	private Paint mPaint;
	private RectF mRectFArc;
	private Path mPath;
	private RectF mRectFInnerArc;
	private Rect mRectText;
	private String[] mTexts;
	
	public DashboardView(Context context) {
		this(context, null);
	}
	
	public DashboardView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public DashboardView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init();
	}
	
	private void init() {
		mStrokeWidth = dp2px(1);
		mLength1 = dp2px(8) + mStrokeWidth;
		mLength2 = mLength1 + dp2px(2);
		mPSRadius = dp2px(10);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		
		mRectFArc = new RectF();
		mPath = new Path();
		mRectFInnerArc = new RectF();
		mRectText = new Rect();
		
		mTexts = new String[M_SECTION + 1]; // 需要显示mSection + 1个刻度读数
		for (int i = 0; i < mTexts.length; i++) {
			float n = (mMax - mMin) / M_SECTION;
			mTexts[i] = String.valueOf(mMin + i * n);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		mPadding = Math.max(
				Math.max(getPaddingLeft(), getPaddingTop()),
				Math.max(getPaddingRight(), getPaddingBottom())
		);
		setPadding(mPadding, mPadding, mPadding, mPadding);
		
		int width = resolveSize(dp2px(200), widthMeasureSpec);
		mRadius = (width - mPadding * 2 - mStrokeWidth * 2) / 2;
		
		mPaint.setTextSize(sp2px(16));
		if (IS_SHOW_VALUE) { // 显示实时读数，View高度增加字体高度3倍
			mPaint.getTextBounds("0", 0, "0".length(), mRectText);
		} else {
			mPaint.getTextBounds("0", 0, 0, mRectText);
		}
		// 由半径+指针短半径+实时读数文字高度确定的高度
		int height1 = mRadius + mStrokeWidth * 2 + mPSRadius + mRectText.height() * 3;
		// 由起始角度确定的高度
		float[] point1 = getCoordinatePoint(mRadius, M_START_ANGLE);
		// 由结束角度确定的高度
		float[] point2 = getCoordinatePoint(mRadius, M_START_ANGLE + M_SWEEP_ANGLE);
		// 取最大值
		int max = (int) Math.max(height1, Math.max(point1[1] + mRadius + mStrokeWidth * 2, point2[1] + mRadius + mStrokeWidth * 2));
		setMeasuredDimension(width, max + getPaddingTop() + getPaddingBottom());
		
		mCenterX = mCenterY = getMeasuredWidth() / 2f;
		mRectFArc.set(
				getPaddingLeft() + mStrokeWidth,
				getPaddingTop() + mStrokeWidth,
				getMeasuredWidth() - getPaddingRight() - mStrokeWidth,
				getMeasuredWidth() - getPaddingBottom() - mStrokeWidth
		);
		
		mPaint.setTextSize(sp2px(10));
		mPaint.getTextBounds("0", 0, "0".length(), mRectText);
		mRectFInnerArc.set(
				getPaddingLeft() + mLength2 + mRectText.height(),
				getPaddingTop() + mLength2 + mRectText.height(),
				getMeasuredWidth() - getPaddingRight() - mLength2 - mRectText.height(),
				getMeasuredWidth() - getPaddingBottom() - mLength2 - mRectText.height()
		);
		
		mPLRadius = mRadius - (mLength2 + mRectText.height() + dp2px(5));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		//画圆弧
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mStrokeWidth);
		mPaint.setColor(SWEEP_ANGLE_COLOR);
		canvas.drawArc(mRectFArc, M_START_ANGLE, M_SWEEP_ANGLE, false, mPaint);
		
		//画长刻度。画好起始角度的一条刻度后通过canvas绕着原点旋转来画剩下的长刻度
		double cos = Math.cos(Math.toRadians(M_START_ANGLE - 180));
		double sin = Math.sin(Math.toRadians(M_START_ANGLE - 180));
		float x0 = (float) (mPadding + mStrokeWidth + mRadius * (1 - cos));
		float y0 = (float) (mPadding + mStrokeWidth + mRadius * (1 - sin));
		float x1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1) * cos);
		float y1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1) * sin);
		
		canvas.save();
		canvas.drawLine(x0, y0, x1, y1, mPaint);
		float angle = M_SWEEP_ANGLE * 1f / M_SECTION;
		for (int i = 0; i < M_SECTION; i++) {
			canvas.rotate(angle, mCenterX, mCenterY);
			canvas.drawLine(x0, y0, x1, y1, mPaint);
		}
		canvas.restore();
		
		//画短刻度。同样采用canvas的旋转原理
		canvas.save();
		mPaint.setStrokeWidth(1);
		float x2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 2f) * cos);
		float y2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 2f) * sin);
		canvas.drawLine(x0, y0, x2, y2, mPaint);
		angle = M_SWEEP_ANGLE * 1f / (M_SECTION * M_PORTION);
		for (int i = 1; i < M_SECTION * M_PORTION; i++) {
			canvas.rotate(angle, mCenterX, mCenterY);
			if (i % M_PORTION == 0) { // 避免与长刻度画重合
				continue;
			}
			canvas.drawLine(x0, y0, x2, y2, mPaint);
		}
		canvas.restore();
		
		//画长刻度读数。添加一个圆弧path，文字沿着path绘制
		mPaint.setTextSize(sp2px(10));
		mPaint.setTextAlign(Paint.Align.LEFT);
		mPaint.setStyle(Paint.Style.FILL);
		for (int i = 0; i < mTexts.length; i++) {
			mPaint.getTextBounds(mTexts[i], 0, mTexts[i].length(), mRectText);
			// 粗略把文字的宽度视为圆心角2*θ对应的弧长，利用弧长公式得到θ，下面用于修正角度
			float degree = (float) (180 * mRectText.width() / 2 /
					(Math.PI * (mRadius - mLength2 - mRectText.height())));
			
			mPath.reset();
			mPath.addArc(mRectFInnerArc,
					M_START_ANGLE + i * (M_SWEEP_ANGLE / M_SECTION) - degree, // 正起始角度减去θ使文字居中对准长刻度
					M_SWEEP_ANGLE);
			canvas.drawTextOnPath(mTexts[i], mPath, 0, 0, mPaint);
		}
		
		//画表头。没有表头就不画
		if (!TextUtils.isEmpty(M_HEADER_TEXT)) {
			mPaint.setTextSize(sp2px(14));
			mPaint.setTextAlign(Paint.Align.CENTER);
			mPaint.getTextBounds(M_HEADER_TEXT, 0, M_HEADER_TEXT.length(), mRectText);
			canvas.drawText(M_HEADER_TEXT, mCenterX, mCenterY / 2f + mRectText.height(), mPaint);
		}
		
		//画指针
		float degree = M_START_ANGLE + M_SWEEP_ANGLE * (mRealTimeValue - mMin) / (mMax - mMin); // 指针与水平线夹角
		int d = dp2px(5); // 指针由两个等腰三角形构成，d为共底边长的一半
		mPath.reset();
		float[] p1 = getCoordinatePoint(d, degree - 90);
		mPath.moveTo(p1[0], p1[1]);
		float[] p2 = getCoordinatePoint(mPLRadius, degree);
		mPath.lineTo(p2[0], p2[1]);
		float[] p3 = getCoordinatePoint(d, degree + 90);
		mPath.lineTo(p3[0], p3[1]);
		float[] p4 = getCoordinatePoint(mPSRadius, degree - 180);
		mPath.lineTo(p4[0], p4[1]);
		mPath.close();
		canvas.drawPath(mPath, mPaint);
		
		//画指针围绕的镂空圆心
		mPaint.setColor(Color.WHITE);
		canvas.drawCircle(mCenterX, mCenterY, dp2px(2), mPaint);
		
		//画实时度数值
		if (IS_SHOW_VALUE) {
			mPaint.setTextSize(sp2px(18));
			mPaint.setTextAlign(Paint.Align.CENTER);
			mPaint.setColor(REAL_TIME_VALUE_COLOR);
			String value = String.format(Locale.getDefault(), "%.2f", mRealTimeValue) /*+ " MB/S"*/;
			mPaint.getTextBounds(value, 0, value.length(), mRectText);
			canvas.drawText(value, mCenterX, mCenterY + mPSRadius + mRectText.height() * 2, mPaint);
		}
	}
	
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				Resources.getSystem().getDisplayMetrics());
	}
	
	private int sp2px(int sp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
				Resources.getSystem().getDisplayMetrics());
	}
	
	public float[] getCoordinatePoint(int radius, float angle) {
		float[] point = new float[2];
		
		double arcAngle = Math.toRadians(angle); //将角度转换为弧度
		if (angle < 90) {
			point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
		} else if (angle == 90) {
			point[0] = mCenterX;
			point[1] = mCenterY + radius;
		} else if (angle > 90 && angle < 180) {
			arcAngle = Math.PI * (180 - angle) / 180.0;
			point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
		} else if (angle == 180) {
			point[0] = mCenterX - radius;
			point[1] = mCenterY;
		} else if (angle > 180 && angle < 270) {
			arcAngle = Math.PI * (angle - 180) / 180.0;
			point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
		} else if (angle == 270) {
			point[0] = mCenterX;
			point[1] = mCenterY - radius;
		} else {
			arcAngle = Math.PI * (360 - angle) / 180.0;
			point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
		}
		
		return point;
	}
	
	public float getRealTimeValue() {
		return mRealTimeValue;
	}
	
	public void setRealTimeValue(float realTimeValue) {
		if (mRealTimeValue == realTimeValue || realTimeValue < mMin || realTimeValue > mMax) {
			return;
		}
		
		mRealTimeValue = realTimeValue;
		postInvalidate();
	}
}