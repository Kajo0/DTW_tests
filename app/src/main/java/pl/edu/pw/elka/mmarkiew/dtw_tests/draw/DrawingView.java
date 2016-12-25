package pl.edu.pw.elka.mmarkiew.dtw_tests.draw;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class DrawingView extends View {

    private Path mPatternPath;
    private Path mDrawPath;
    private Paint mDrawPaint;
    private Paint mPatternPaint;
    private Paint mPointPaint;
    private Paint mCanvasPaint;
    private Canvas mDrawCanvas;
    private Bitmap mCanvasBitmap;
    private Pair<Float, Float> mPoint = new Pair<Float, Float>(0f, 0f);

    private List<Pair<Float, Float>> mPatternPoints;

    public DrawingView(final Context context, final AttributeSet attrs,
            final List<Pair<Float, Float>> patternPath) {
        super(context, attrs);

        this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        setupDrawing();

        mPatternPoints = patternPath;
        for (int i = 0; i < mPatternPoints.size(); ++i) {
            Pair<Float, Float> p = mPatternPoints.get(i);

            if (i > 0) {
                mPatternPath.lineTo(p.first, p.second);
            } else {
                mPoint = p;
                mPatternPath.moveTo(p.first, p.second);
            }
        }
    }

    private void setupDrawing() {
        mDrawPath = new Path();
        mDrawPaint = new Paint();
        mDrawPaint.setColor(Color.BLACK);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStrokeWidth(20);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mCanvasPaint = new Paint(Paint.DITHER_FLAG);

        mPatternPath = new Path();
        mPatternPaint = new Paint();
        mPatternPaint.setColor(Color.RED);
        mPatternPaint.setAntiAlias(true);
        mPatternPaint.setStrokeWidth(5);
        mPatternPaint.setStyle(Paint.Style.STROKE);
        mPatternPaint.setStrokeJoin(Paint.Join.ROUND);
        mPatternPaint.setStrokeCap(Paint.Cap.ROUND);

        mPointPaint = new Paint();
        mPointPaint.setColor(Color.GREEN);
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStrokeWidth(30);
        mPointPaint.setStyle(Paint.Style.STROKE);
        mPointPaint.setStrokeJoin(Paint.Join.ROUND);
        mPointPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mDrawCanvas = new Canvas(mCanvasBitmap);

        mDrawCanvas.drawPoint(mPoint.first, mPoint.second, mPointPaint);
        mDrawCanvas.drawPath(mPatternPath, mPatternPaint);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        canvas.drawBitmap(mCanvasBitmap, 0, 0, mCanvasPaint);
        canvas.drawPath(mDrawPath, mDrawPaint);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mDrawPath.moveTo(touchX, touchY);
            break;
        case MotionEvent.ACTION_MOVE:
            mDrawPath.lineTo(touchX, touchY);
            break;
        case MotionEvent.ACTION_UP:
            mDrawPath.lineTo(touchX, touchY);
            mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
            mDrawPath.reset();
            break;

        default:
            return false;
        }

        invalidate();

        return true;
    }

    public void setPatternPath(final List<Pair<Float, Float>> patternPath) {
        mPatternPoints = patternPath;
    }

}
