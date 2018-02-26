package tfm.uniovi.pirateseas.utils.approach2d;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class DrawableHelper{


	/**
	 * Returns a bitmap with the upper half of the current sheet
	 * @return Upper half bitmap
	 */
	public Bitmap getFirstHalf(Drawable image){
		Bitmap bmp = ((BitmapDrawable)image).getBitmap();
		Bitmap croppedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight() / 2);
		return croppedBmp;
	}

	/**
	 * Returns a bitmap with the lower half of the current sheet
	 * @return Lower half bitmap
	 */
	public Bitmap getLastHalf(Drawable image){
		Bitmap bmp = ((BitmapDrawable)image).getBitmap();
		Bitmap croppedBmp = Bitmap.createBitmap(bmp, 0, bmp.getHeight() / 2, bmp.getWidth(), bmp.getHeight() / 2);
		return croppedBmp;
	}

	/**
	 * Returns the received Drawable rotated x degrees
	 * @param d
	 * @return x Degrees rotated drawable
	 */
	public static Drawable rotateDrawable(Drawable d, Context context, float angle) {
		Drawable rotatedDrawable = null;
		BitmapDrawable bd = (BitmapDrawable) d;
		Bitmap rotatedBitmap = rotateBitmap(bd.getBitmap(), angle);
		BitmapDrawable tmp = new BitmapDrawable(context.getResources(),
				rotatedBitmap);
		rotatedDrawable = tmp.getCurrent();
		return rotatedDrawable;
	}

	/**
	 * Returns the received bitmap rotated the received degrees 
	 * @param bmp
	 * @param degrees
	 * @return Rotated bitmap
	 */
	public static Bitmap rotateBitmap(Bitmap bmp, float degrees) {
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
				bmp.getHeight(), matrix, true);
		return rotatedBitmap;
	}

	public static double getWidth(Resources r, int drawableValue) {
		BitmapFactory.Options dimensions = new BitmapFactory.Options();
		dimensions.inJustDecodeBounds = true;
		@SuppressWarnings("unused")
		Bitmap mBitmap = BitmapFactory.decodeResource(r, drawableValue, dimensions);
		int width =  dimensions.outWidth;
		return width;
	}

	public static double getHeight(Resources r, int drawableValue) {
		BitmapFactory.Options dimensions = new BitmapFactory.Options();
		dimensions.inJustDecodeBounds = true;
		@SuppressWarnings("unused")
		Bitmap mBitmap = BitmapFactory.decodeResource(r, drawableValue, dimensions);
		int height =  dimensions.outHeight;
		return height;
	}



	/**
	 * Merge all bitmaps send as param onto one big bitmap, setting one part side to side with the previous
	 * @param parts
	 * @param height
	 * @param width
	 * @return
	 */
	public static Bitmap mergeBitmaps(Bitmap[] parts, int height, int width){
		int partsWidth = parts[0].getWidth();
		int horizontalParts = width / partsWidth;
		int rawWidthMargin = width - (horizontalParts * partsWidth);
		int leftMargin = rawWidthMargin / 2;
		int partsHeight = parts[0].getHeight();
		int verticalParts = height / partsHeight;
		int rawHeightMargin = height - (verticalParts * partsHeight);
		int topMargin = rawHeightMargin / 2;

		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		Paint paint = new Paint();
		int length = parts.length;

		for (int i = 0; i < length; i++) {
			int left = leftMargin + parts[i].getWidth() * (i % horizontalParts);
			int top = topMargin + parts[i].getHeight() * (i / horizontalParts);
			canvas.drawBitmap(parts[i], left, top, paint);
		}
		return result;
	}

	public static Bitmap overlapBitmaps(Bitmap back, Bitmap front) {
		Bitmap result = Bitmap.createBitmap(back.getWidth(), back.getHeight(), back.getConfig());
		Canvas canvas = new Canvas(result);
		int widthBack = back.getWidth();
		int widthFront = front.getWidth();
		float move = (widthBack - widthFront) / 2;
		canvas.drawBitmap(back, 0f, 0f, null);
		canvas.drawBitmap(front, move, move, null);
		return result;
	}

}