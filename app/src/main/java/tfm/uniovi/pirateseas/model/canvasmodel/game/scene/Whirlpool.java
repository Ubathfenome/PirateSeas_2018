package tfm.uniovi.pirateseas.model.canvasmodel.game.scene;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.model.canvasmodel.game.BasicModel;
import tfm.uniovi.pirateseas.model.canvasmodel.game.Parallax;

/**
 * Class for the whirlpool element that appears on screen when a maelstorm is triggered
 *
 * @see: http://gamecodeschool.com/android/coding-android-sprite-sheet-animations/
 */
public class Whirlpool extends BasicModel {
    private int frameWidth = 67;
    private int frameHeight = 67;
    private int currentFrame;
    private Rect frameToDraw = new Rect(
            0,
            0,
            frameWidth,
            frameHeight);
    private RectF whereToDraw = new RectF(
            xLeft, 0,
            xLeft + frameWidth,
            frameHeight);
    private Bitmap bitmap;
    private Paint paint = new Paint();

    // What time was it when we last changed frames
    private long lastFrameChangeTime = 0;


    /**
     * Constructor
     *
     * @param context Context
     * @param x X Coordinate of the Whirlpool
     * @param y Y Coordinate of the Whirlpool
     * @param mCanvasWidth Canvas Width
     * @param mCanvasHeight Canvas Height
     * @param parallax Parallax (Nullable)
     */
    public Whirlpool(Context context, double x, double y, double mCanvasWidth, double mCanvasHeight, Parallax parallax) {
        super(context, x, y, mCanvasWidth, mCanvasHeight, parallax);
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.whirlpool_sprite);
        currentFrame = 0;
    }

    /**
     * Set the next frame to draw
     */
    public void getCurrentFrame(){
        long timestamp = System.currentTimeMillis();
        // How long should each frame last
        int frameLengthInMilliseconds = 100;
        if ( timestamp > lastFrameChangeTime + frameLengthInMilliseconds) {
            lastFrameChangeTime = timestamp;
            currentFrame++;
            int frameCount = 8;
            if (currentFrame >= frameCount) {
                currentFrame = 0;
            }
        }

        //update the left and right values of the source of
        //the next frame on the sprite sheet
        frameToDraw.left = currentFrame * frameWidth;
        frameToDraw.right = frameToDraw.left + frameWidth;

    }

    /**
     * Move the whirlpool through the screen
     */
    public void move(){
        if (x+frameWidth<mCanvasWidth){
            int frameAcceleration = 23;
            x+= frameAcceleration;
        }
    }

    /**
     * Draw the current frame on the Canvas screen
     * @param canvas Screen canvas
     */
    public void drawOnScreen(Canvas canvas){
        getCurrentFrame();
        if (x+frameWidth<mCanvasWidth) {
            canvas.drawBitmap(bitmap,
                    frameToDraw,
                    whereToDraw, paint);
        }
    }

}
