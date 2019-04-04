package tfm.uniovi.pirateseas.utils.approach2d;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Utils class with Drawable related methods
 */
public class DrawableHelper{


	/**
	 * Returns a bitmap with the upper half of the current sheet
	 * @return Upper half bitmap
	 */
	public Bitmap getFirstHalf(Drawable image){
		Bitmap bmp = ((BitmapDrawable)image).getBitmap();
		return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight() / 2);
	}

	/**
	 * Returns a bitmap with the lower half of the current sheet
	 * @return Lower half bitmap
	 */
	public Bitmap getLastHalf(Drawable image){
		Bitmap bmp = ((BitmapDrawable)image).getBitmap();
		return Bitmap.createBitmap(bmp, 0, bmp.getHeight() / 2, bmp.getWidth(), bmp.getHeight() / 2);
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
		return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
				bmp.getHeight(), matrix, true);
	}

	/**
	 * Returns the width of a drawable resource
	 * @param r Resource object
	 * @param drawableValue Drawable resource index
	 * @return Width of the image
	 */
	public static double getWidth(Resources r, int drawableValue) {
		BitmapFactory.Options dimensions = new BitmapFactory.Options();
		dimensions.inJustDecodeBounds = true;
		@SuppressWarnings("unused")
		Bitmap mBitmap = BitmapFactory.decodeResource(r, drawableValue, dimensions);
		return dimensions.outWidth;
	}

	/**
	 * Returns the height of a drawable resource
	 * @param r Resource object
	 * @param drawableValue Drawable resource index
	 * @return Height of the image
	 */
	public static double getHeight(Resources r, int drawableValue) {
		BitmapFactory.Options dimensions = new BitmapFactory.Options();
		dimensions.inJustDecodeBounds = true;
		@SuppressWarnings("unused")
		Bitmap mBitmap = BitmapFactory.decodeResource(r, drawableValue, dimensions);
		return dimensions.outHeight;
	}

	/**
	 * Returns the screen width
	 * @param context Calling Activity
	 * @return Screen width
	 */
	public static int getScreenWidth(Context context){
		Point size = new Point();
		((Activity)context).getWindowManager().getDefaultDisplay().getSize(size);
		return size.x;
	}

	/**
	 * Returns the screen height
	 * @param context Calling Activity
	 * @return Screen height
	 */
	public static int getScreenHeight(Context context){
		Point size = new Point();
		((Activity)context).getWindowManager().getDefaultDisplay().getSize(size);
		return size.y;
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

	/**
	 * Overlap one Bitmap over the other
	 * @param back  Background Bitmap
	 * @param front Top Bitmap
	 * @return Merged Bitmap
	 */
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

	/**
	 * Returns Bitmap from a Resource within the specified size
	 * @param res
	 * @param resId
	 * @param reqWidth
	 * @param reqHeight
	 * @return Bitmap
	 */
	public static Bitmap decodeBitmapFromResource(Resources res, int resId,
														 int reqWidth, int reqHeight) {
		Bitmap bitmap = null;
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		try {
			bitmap = BitmapFactory.decodeResource(res, resId, options);
		} catch(OutOfMemoryError e){
			System.gc();
			bitmap = BitmapFactory.decodeResource(res, resId, options);
		}
		return bitmap;
	}

	/**
	 * Calculate the size multiplier for an image to fit on the specified bounds
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return Size multiplier
	 */
	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) >= reqHeight
					&& (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}