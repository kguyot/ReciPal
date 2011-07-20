package guyot.kevin.recipal.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import guyot.kevin.recipal.R;
import guyot.kevin.recipal.data.ReciPalDB;
import guyot.kevin.recipal.tasks.RecipeIconTask;

public class ReciPalCursorAdapter extends SimpleCursorAdapter {

	private static final String debugTag = "ReciPalCursorAdapter";
	private RecipeIconTask imgFetcher;
	private Cursor cursor;
	
	public ReciPalCursorAdapter(RecipeIconTask i, Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.cursor = c;
		this.imgFetcher = i;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Log.d(debugTag, "getview - Position " + position);
		View v = super.getView(position, convertView, parent); //inView;
		this.cursor.moveToPosition(position);   

		ImageView ivImage = (ImageView) v.findViewById(R.id.RecipeImage);
		RatingBar rbRating = (RatingBar) v.findViewById(R.id.RecipeRating);

		if (ivImage != null) {
			String imgFile = cursor.getString(cursor.getColumnIndex(ReciPalDB.TABLE_RECIPE_IMAGE));
			if (imgFile != null && imgFile.length() != 0) {
				
				Log.d(debugTag, "Fetcing Image: " + imgFile);
				ivImage.setTag(imgFile);
				Bitmap bmp = imgFetcher.loadImage(this, ivImage);
				if (bmp != null) {
					Log.d(debugTag, "Has Bitmap in cash: " + imgFile + " " + bmp.toString());
					ivImage.setImageBitmap(bmp);
				} else {
					Log.d(debugTag, "Waiting for Bitmap to load: " + imgFile);
					ivImage.setImageResource(R.drawable.loading);
					ivImage.setVisibility(View.VISIBLE);
					AnimationDrawable frameAnimation = (AnimationDrawable)ivImage.getDrawable();
					frameAnimation.setCallback(ivImage);
					frameAnimation.setVisible(true, true);
				}
			} else {
				Log.d(debugTag, "No Image assoicated!");
				ivImage.setImageResource(R.drawable.noimage);
			}
		}

		if (rbRating != null)
			rbRating.setRating(cursor.getFloat(cursor.getColumnIndex(ReciPalDB.TABLE_RECIPE_RATING)));

		return v;
	}

}
