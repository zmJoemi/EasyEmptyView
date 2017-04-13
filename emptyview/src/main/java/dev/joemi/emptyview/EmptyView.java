package dev.joemi.emptyview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by kinnyo-imac-24 on 17/4/11.
 */

public class EmptyView extends View implements View.OnClickListener {
    //默认的动画时间
    private final static int DEFULT_DURATION = 660;

    //隐藏当前view
    public final static int HIDE=-2;
    //默认，加载中的
    public final static int LOADING = -1;
    //网络问题
    public final static int OFFLINE = 0;
    //数据为空
    public final static int EMPTY = 1;
    //出现错误
    public final static int ERROR = 2;

    private final static float DEFULT_MAX_ANGLE = -305f;
    private final static float DEFULT_MIN_ANGLE = -19f;

    //对应的图片
    private final static int[] IMGS = {R.mipmap.offline, R.mipmap.empty, R.mipmap.fail};

    private Context mContext;
    private Resources resources;
    private int viewWidth;
    private int viewHeight;

    private Paint circlePaint;
    private Paint bmpPaint;
    private Paint textPaint;

    //当前绘制的view状态
    private int currentStatus = LOADING;
    private String message;

    private AnimatorSet animatorSet;
    private float startAngle = -45f;
    private float sweepAngle = -19f;
    private float incrementAngele = 0;
    private int circleColor;
    private RectF circleRectF;

    private float marginTop;
    private float dp26;
    private float baselineGap;
    private float textXCenter;

    private EmptyView.OnClickListener emptyClick;

    public EmptyView(Context context) {
        this(context, null);
    }

    public EmptyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(this);
        this.mContext=context;
        resources = context.getResources();
        baselineGap = resources.getDimension(R.dimen.dp_56);
        dp26 = resources.getDimension(R.dimen.dp_26);
        message = resources.getString(R.string.view_loading);
        initAttrs(context,attrs);
        initPaint();
    }

    private void initAttrs(Context context,AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.emptyview);
        circleColor=typedArray.getColor(R.styleable.emptyview_circleColor, Color.parseColor("#cccccc"));
        typedArray.recycle();
    }


    private void initPaint() {
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(resources.getDimension(R.dimen.dp_2));
        circlePaint.setColor(circleColor);

        bmpPaint = new Paint();
        bmpPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(resources.getColor(R.color.gray));
        textPaint.setTextSize(resources.getDimension(R.dimen.sp_14));
    }

    public void setOnViewClick(OnClickListener onViewClick) {
        this.emptyClick = onViewClick;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        textXCenter = viewWidth / 2f;

        marginTop = viewHeight / 4f;
        float left = (viewWidth - dp26) / 2;
        circleRectF = new RectF(left, marginTop, left + dp26, marginTop + dp26);
    }

    public void setStatus(int status) {
        if (!hasInternet()){
            currentStatus=OFFLINE;
            message = resources.getString(R.string.view_network_error_click_to_refresh);
        }else if (status==HIDE){
            currentStatus = status;
            if (animatorSet!=null&&animatorSet.isRunning())
                animatorSet.cancel();
            setVisibility(View.GONE);
        } else {
            currentStatus = status;
            switch (status) {
                case LOADING:
                    message = resources.getString(R.string.view_loading);
                    break;
                case OFFLINE:
                    message = resources.getString(R.string.view_network_error_click_to_refresh);
                    break;
                case EMPTY:
                    message = resources.getString(R.string.view_no_data);
                    break;
                case ERROR:
                    message = resources.getString(R.string.view_click_to_refresh);
                    break;
            }
            if (getVisibility()==View.GONE)
                setVisibility(VISIBLE);
        }
        invalidate();
    }

    private boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable() && info.isConnected();
    }


    private void startAnimation() {
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.cancel();  //   取消动画
        }
        circuAnimator();  // 创建运行一圈动画的AnimatorSet
        animatorSet.addListener(new Animator.AnimatorListener() {
            private boolean isCancel = false;

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // 循环动画
                if (!isCancel)
                    startAnimation();

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isCancel = true;
            }
        });

        animatorSet.start();
    }

    private AnimatorSet circuAnimator() {

        //从小圈到大圈
        ValueAnimator holdAnimator1 = ValueAnimator.ofFloat(incrementAngele + DEFULT_MIN_ANGLE, incrementAngele + 115f);
        holdAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                incrementAngele = (float) animation.getAnimatedValue();
            }
        });
        holdAnimator1.setDuration(DEFULT_DURATION);
        holdAnimator1.setInterpolator(new LinearInterpolator());


        ValueAnimator expandAnimator = ValueAnimator.ofFloat(DEFULT_MIN_ANGLE, DEFULT_MAX_ANGLE);
        expandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                sweepAngle = (float) animation.getAnimatedValue();
                incrementAngele -= sweepAngle;
                invalidate();
            }
        });
        expandAnimator.setDuration(DEFULT_DURATION);
        expandAnimator.setInterpolator(new DecelerateInterpolator());


        //从大圈到小圈
        ValueAnimator holdAnimator = ValueAnimator.ofFloat(startAngle, startAngle + 115f);
        holdAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startAngle = (float) animation.getAnimatedValue();
            }
        });

        holdAnimator.setDuration(DEFULT_DURATION);
        holdAnimator.setInterpolator(new LinearInterpolator());

        ValueAnimator narrowAnimator = ValueAnimator.ofFloat(DEFULT_MAX_ANGLE, DEFULT_MIN_ANGLE);
        narrowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                sweepAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        narrowAnimator.setDuration(DEFULT_DURATION);
        narrowAnimator.setInterpolator(new DecelerateInterpolator());

        animatorSet = new AnimatorSet();  //设置一个动画集合
        animatorSet.play(holdAnimator1).with(expandAnimator);
        animatorSet.play(holdAnimator).with(narrowAnimator).after(holdAnimator1);
        return animatorSet;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentStatus == LOADING) {
            canvas.drawArc(circleRectF, startAngle + incrementAngele, sweepAngle, false, circlePaint);
            float textYCenter = marginTop + dp26 + baselineGap;
            canvas.drawText(message, textXCenter, textYCenter, textPaint);
            if (animatorSet == null || !animatorSet.isRunning())
                startAnimation();
        } else {
            if (animatorSet != null && animatorSet.isRunning())
                animatorSet.cancel();
            Bitmap bitmap = BitmapFactory.decodeResource(resources, IMGS[currentStatus]);
            float left = (viewWidth - bitmap.getWidth()) / 2f;
            canvas.drawBitmap(bitmap, left, marginTop, bmpPaint);
            float textYCenter = marginTop + bitmap.getHeight() + baselineGap;
            canvas.drawText(message, textXCenter, textYCenter, textPaint);
        }

    }

    @Override
    public void onClick(View v) {
        if (currentStatus == LOADING)
            return;
        if (emptyClick != null)
            emptyClick.onViewClick();
        setStatus(LOADING);
    }

    public interface OnClickListener {
        void onViewClick();
    }
}
