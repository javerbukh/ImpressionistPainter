package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;

import java.text.MessageFormat;
import java.util.Random;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();
    private Path path = new Path();


    private int _alpha = 150;
    private int _defaultRadius = 25;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;

    private VelocityTracker mVelocityTracker = null;
    private int mActivePointerId;

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeWidth(10);


        _paintBorder.setColor(Color.GRAY);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.FILL);
        _paintBorder.setAlpha(50);


        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    public Bitmap getBitmapFromIV(){
        return _offScreenBitmap;
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){

        _imageView = imageView;
        //invalidate();
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
        invalidate();
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        //TODO
        //_offScreenCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
        _offScreenCanvas.drawColor(Color.WHITE);
        //Paint _Temppaint = new Paint();
        //_Temppaint.setColor(Color.WHITE);
        //_offScreenCanvas.drawRect(0, 0,_imageView.getMaxHeight(), _imageView.getMaxWidth(), _Temppaint);

        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
        canvas.drawPath(path, _paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //TODO
        //Basically, the way this works is to liste for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location
        float touchX = motionEvent.getX();
        float touchY = motionEvent.getY();
        int x = 0;
        int y = 0;
        //Bitmap bitmap = ((BitmapDrawable)_imageView.getDrawable()).getBitmap();
        Bitmap imageViewBitmap = _imageView.getDrawingCache();

        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                //path.moveTo(touchX, touchY);
                //int pixel = imageViewBitmap.getPixel((int)touchX, (int)touchY);
                //_paint.setColor(pixel);
                if(mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                }
                else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(motionEvent);

                break;
            case MotionEvent.ACTION_MOVE:
                if(touchX<imageViewBitmap.getWidth() && touchY<imageViewBitmap.getHeight() && touchX > 0 && touchY>0) {

                    int pixel = imageViewBitmap.getPixel((int) touchX, (int) touchY);
                    _paint.setColor(pixel);
                    if (_brushType == BrushType.Square) {
                        _paint.setStrokeWidth(20);
                        _offScreenCanvas.drawPoint(touchX, touchY, _paint);
                    }

                    if (_brushType == BrushType.Line) {
                        _paint.setStrokeWidth(5);
                        if(touchY>20 && touchY<imageViewBitmap.getHeight()-20){
                            _offScreenCanvas.drawLine(touchX,touchY-20,touchX,touchY+20,_paint);
                        }
                    }
                    if (_brushType == BrushType.Circle) {
                        _paint.setStrokeWidth(20);
                        _offScreenCanvas.drawPoint(touchX, touchY, _paint);
                        _offScreenCanvas.drawCircle(touchX, touchY, 15, _paint);
                    }
                    if (_brushType == BrushType.CircleVelcoity) {
                        _paint.setStrokeWidth(5);
                        mVelocityTracker.addMovement(motionEvent);
                        mVelocityTracker.computeCurrentVelocity(1);

                        float xVelocity = mVelocityTracker.getXVelocity();
                        float yVelocity = mVelocityTracker.getYVelocity();

                        //_offScreenCanvas.drawCircle(20, 20, 20, _paint);
                        //path.addCircle(touchX,touchY,10, Path.Direction.CW);
                        float combinedVelocity = xVelocity+yVelocity;
                        //_offScreenCanvas.drawPoint(touchX, touchY, _paint);
                        _offScreenCanvas.drawCircle(touchX, touchY, (float)(15*combinedVelocity), _paint);


                    }


                    //path.lineTo(touchX, touchY);

                    //_offScreenCanvas.drawPath(path, _paint);
                    //path.reset();
                }
                break;
            case MotionEvent.ACTION_UP:
                _offScreenCanvas.drawPath(path, _paint);
                path.reset();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //mActivePointerId = motionEvent.getPointerId(0);

                // ... Many touch events later...

                // Use the pointer ID to find the index of the active pointer
                // and fetch its position
                int pointerIndex = motionEvent.findPointerIndex(0);
                // Get the pointer's current position
                float x2 = motionEvent.getX(pointerIndex);
                float y2 = motionEvent.getY(pointerIndex);
                path.moveTo(x2, y2);
                //mActivePointerId = motionEvent.getPointerId(1);

                // ... Many touch events later...

                // Use the pointer ID to find the index of the active pointer
                // and fetch its position
                pointerIndex = motionEvent.findPointerIndex(1);
                // Get the pointer's current position
                x2 = motionEvent.getX(pointerIndex);
                y2 = motionEvent.getY(pointerIndex);
                path.lineTo(x2, y2);
            case MotionEvent.ACTION_POINTER_UP:
                _offScreenCanvas.drawPath(path, _paint);
                path.reset();
                break;
            case MotionEvent.ACTION_CANCEL:
                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker.recycle();
                break;
            default:
                return false;

        }

        invalidate();
        return true;
    }




    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual) / 2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }

}

