package guyot.kevin.recipal;

import guyot.kevin.recipal.data.Ingredient;
import guyot.kevin.recipal.data.ReciPalDB;
import guyot.kevin.recipal.data.Recipe;
import guyot.kevin.recipal.tasks.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.bumpapi.BumpAPI;
import com.bumptech.bumpapi.BumpAPIListener;
import com.bumptech.bumpapi.BumpConnectFailedReason;
import com.bumptech.bumpapi.BumpConnection;
import com.bumptech.bumpapi.BumpDisconnectReason;

public class ViewRecipe extends ReciPalBaseActivity implements BumpAPIListener  {

	protected static final int ARE_YOU_SURE_DIALOG = 0;
	protected static final int BUMP_DIALOG = 1;
	private static final String debugTag = "ViewRecipe";
	
	private static final int RECIPE_EDIT = 0;
	private static final int RECIPE_DELETE = 1;
	private static final int RECIPE_BUMP = 2;
		
	private ImageView recipeImage;
	private TextView recipeName;
	private RatingBar recipeRating;
	private TextView recipePrep;
	private Recipe recipe;

	private ImageView editBtn;
	private LinearLayout lvIngredients;
	
	private BumpConnection conn;

	private ReciPalDB db;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewrecipe);
		db = new ReciPalDB(getApplicationContext());
		
		this.recipeImage = (ImageView)this.findViewById(R.id.dialogImage);
		this.recipeName = (TextView)this.findViewById(R.id.dialogName);
		this.recipeRating = (RatingBar)this.findViewById(R.id.dialogRating);
		this.recipePrep = (TextView)this.findViewById(R.id.dialogPrep);
		this.lvIngredients = (LinearLayout)this.findViewById(R.id.ingredientList);

		editBtn = (ImageView)this.findViewById(R.id.RecipeEdit);

		this.recipe = null;


		Bundle extras = this.getIntent().getExtras();
		if(extras != null)
		{
			this.recipe = (Recipe) extras.getSerializable("edu.gvsu.cis680.recipal.recipe");    			    			
			if(this.recipe != null)
			{
				Refresh();
			}
		}

		editBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(ViewRecipe.this, AddRecipe.class);
				i.putExtra("edu.gvsu.cis680.recipal.recipe", ViewRecipe.this.recipe);
				startActivityForResult(i, ReciPal.RECIPE_EDIT_DIALOG);
			}
		});        
	}
	
	protected void BumpConnect() {
		Intent bump = new Intent(this, BumpAPI.class);
		bump.putExtra(BumpAPI.EXTRA_USER_NAME, "ReciPal");
		bump.putExtra(BumpAPI.EXTRA_API_KEY, "b970b8717d934086aee23de96032d686");
		bump.putExtra(BumpAPI.EXTRA_ACTION_MSG, "Bump with another phone to send recipe.");
		startActivityForResult(bump, BUMP_DIALOG);		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(db != null)
			db.Close();
		db = null;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if(id == ARE_YOU_SURE_DIALOG)
		{
			builder.setMessage("Are you sure you want to delete this recipe?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					ReciPalDB db = new ReciPalDB(getApplicationContext());
					db.deleteRecipe(ViewRecipe.this.recipe.getId());
					db.Close();
					db = null;
					ViewRecipe.this.finish();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			})
			.setCancelable(false);
			dialog = builder.create();
		}		
		return dialog;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 * 
	 * Check what activities are returning for a result and update the display
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//Check if the returning activity is EDIT_REQUEST...
		//if it was something else it is not something we know how to handle
		if(requestCode == ReciPal.RECIPE_EDIT_DIALOG)
		{
			String msg = null;  //Holder to toast a success / canceled msg to user...

			//check if we have data returned from the activity and if the result was OK
			//if the result was not OK and the data was null
			//then the activity was most likely canceled...or something else happened
			if(data != null && resultCode == Activity.RESULT_OK)
			{
				this.recipe = (Recipe) data.getExtras().getSerializable("edu.gvsu.cis680.recipal.recipe");    			    			
				msg = "Saved - " + this.recipe .getName();
				ReciPalDB db = new ReciPalDB(this);
				db.updateRecipe(this.recipe);
				
				db.Close();
				db = null;

				//refresh the display
				Refresh();
			}
			else
			{
				msg = "Canceled";
			}
			Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();    		
		}
		
		if(requestCode == BUMP_DIALOG)
		{
			if (resultCode == RESULT_OK) {
				conn = (BumpConnection) data.getParcelableExtra(BumpAPI.EXTRA_CONNECTION);
				conn.setListener(this);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
					ObjectOutput out = new ObjectOutputStream(bos);
					out.writeObject(this.recipe);
				} catch (IOException e) {
					e.printStackTrace();
				}   
				conn.send(bos.toByteArray());
			} else {
				BumpConnectFailedReason reason = (BumpConnectFailedReason)data.getSerializableExtra(BumpAPI.EXTRA_REASON);
				Log.wtf(debugTag, "--- Failed to connect (" + reason.toString() + ")---");
			}		
		}
	}

	private void Refresh() {
		this.recipeName.setText(recipe.getName());
		this.recipeRating.setRating(recipe.getRating());
		this.recipePrep.setText(recipe.getPreperation());

		Bitmap bmp =  BitmapUtils.createImageThumbnail(recipe.getImage(), Images.Thumbnails.MICRO_KIND);
		if(bmp != null)
			this.recipeImage.setImageBitmap(bmp);
		else
			this.recipeImage.setImageResource(R.drawable.noimage);

		this.recipe.setIngredients(db.selectIngredientsByRecipeId(this.recipe.getId()));
		
		lvIngredients.removeAllViews();
		 for (Ingredient i : this.recipe.getIngredients())
		 {
			 TextView tvDesc = new TextView(this);
			 tvDesc.setText(i.getDescription());
			 lvIngredients.addView(tvDesc);
		 }                    
	}
	
	@Override
	public void bumpDataReceived(byte[] yourBytes) {
		Toast.makeText(ViewRecipe.this, "To Recieve a Recipe Choose Bump from Dashboard.", Toast.LENGTH_LONG).show();
	}

	@Override
	public void bumpDisconnect(BumpDisconnectReason reason) {
		switch (reason) {
		case END_OTHER_USER_QUIT:
			Log.wtf(debugTag,"--- " + conn.getOtherUserName() + " QUIT ---");
			break;
		case END_OTHER_USER_LOST:
			Log.wtf(debugTag,"--- " + conn.getOtherUserName() + " LOST ---");
			break;
		}
	}
	
	
     @Override
	public void onStop() {
		if (conn != null)
			conn.disconnect();
		
		super.onStop();
	}

	/** 
     * Creates a menu option for editing   
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0,RECIPE_EDIT,0,R.string.edit).setIcon(R.drawable.ic_menu_edit);
		menu.add(0,RECIPE_DELETE,1,R.string.delete).setIcon(R.drawable.ic_menu_delete);
		menu.add(0,RECIPE_BUMP,2,R.string.bumpsend).setIcon(R.drawable.ic_menu_bump);
		return true;	
	}    

	/**
	 * Method called when a menu option is selected.
	 * In this case we have 1 menu option for editing 
	 * me.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch(item.getItemId()) {
		case RECIPE_EDIT:
			editBtn.performClick();
			break;
		case RECIPE_DELETE:
			this.showDialog(ARE_YOU_SURE_DIALOG);
			break;
		case RECIPE_BUMP:
			ViewRecipe.this.BumpConnect();
			break;
		}
		return true;
	}

  	
}
