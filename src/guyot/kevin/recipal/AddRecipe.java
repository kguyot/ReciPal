package guyot.kevin.recipal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import guyot.kevin.recipal.R;
import guyot.kevin.recipal.data.Ingredient;
import guyot.kevin.recipal.data.ReciPalDB;
import guyot.kevin.recipal.data.Recipe;
import guyot.kevin.recipal.tasks.BitmapUtils;

public class AddRecipe extends ReciPalBaseActivity {
	private static final String debugTag = "AddRecipe";
	private final static int SELECT_IMAGE = 1;
	private static final int ADD_INGREDIENT_DIALOG = 2;
	private static final int ARE_YOU_SURE_DIALOG = 3;

	private LayoutInflater layoutInflator;

	private Button saveBtn;

	private ImageView recipeImage;
	private EditText recipeName;
	private RatingBar recipeRating;
	private EditText recipePrep;
	private ImageView addIngredient;
	private Recipe recipe;

	private LinearLayout lvIngredients;
	//private IngredientCursorAdapter cursorAdapter;

	private ReciPalDB db;
	private EditText ingredient;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addrecipe);
		db = new ReciPalDB(getApplicationContext());

		this.layoutInflator = LayoutInflater.from(this);

		this.saveBtn = (Button)this.findViewById(R.id.dialogSave);        
		this.recipeImage = (ImageView)this.findViewById(R.id.dialogImage);
		this.recipeName = (EditText)this.findViewById(R.id.dialogName);
		this.recipeRating = (RatingBar)this.findViewById(R.id.dialogRating);
		this.recipePrep = (EditText)this.findViewById(R.id.dialogPrep);
		this.lvIngredients = (LinearLayout)this.findViewById(R.id.ingredientList);
		this.addIngredient = (ImageView)this.findViewById(R.id.ibAddIngred);
		recipe = new Recipe("", "", "", 3);

		Bundle extras = this.getIntent().getExtras();
		if(extras != null)
		{
			recipe = (Recipe) extras.getSerializable("edu.gvsu.cis680.recipal.recipe");    			    			
			Refresh();
		}		

		this.addIngredient.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AddRecipe.this.recipe.setName(recipeName.getText().toString());
				AddRecipe.this.recipe.setImage((String)recipeImage.getTag());
				AddRecipe.this.recipe.setRating(recipeRating.getRating());
				AddRecipe.this.recipe.setPreperation(recipePrep.getText().toString());				
				showDialog(ADD_INGREDIENT_DIALOG);
			}
		});

		//Set the on Save click listener.  
		//On save pass back the set parameters to the calling activity
		this.saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				//if(recipe.getId() == -1) {
				//	recipe = new Recipe(recipeName.getText().toString(), recipePrep.getText().toString(), (String)recipeImage.getTag(), recipeRating.getRating());
				//}
				//else //this means it was passed in...must be editing...this keeps the id...
				//{
				recipe.setName(recipeName.getText().toString());
				recipe.setImage((String)recipeImage.getTag());
				recipe.setRating(recipeRating.getRating());
				recipe.setPreperation(recipePrep.getText().toString());
				//}

				Intent data = new Intent();
				data.putExtra("edu.gvsu.cis680.recipal.recipe", recipe);

				//set the result to OK and finish / close the activity
				setResult(Activity.RESULT_OK, data);
				finish();
			}
		}); 

		this.recipeImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);				
			}
		});

	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_IMAGE) {
				Uri selectedImageUri = data.getData();                

				Bitmap bmp =BitmapUtils.createImageThumbnail(getPath(selectedImageUri), Images.Thumbnails.MICRO_KIND); 
				this.recipeImage.setImageBitmap(bmp);            	
				this.recipeImage.setTag(getPath(selectedImageUri));                
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}     

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(db != null)
			db.Close();
		db = null;
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
		this.recipeImage.setTag(recipe.getImage());

		if(AddRecipe.this.recipe.getId() != -1)
			this.recipe.setIngredients(db.selectIngredientsByRecipeId(this.recipe.getId()));

		lvIngredients.removeAllViews();
		for (Ingredient i : this.recipe.getIngredients()) 
		{
			View v = layoutInflator.inflate(R.layout.ingredienteditlistitem,null);
			TextView tvDesc = (TextView) v.findViewById(R.id.IngredientDesc);
			tvDesc.setText(i.getDescription());

			ImageButton ibDelete = (ImageButton) v.findViewById(R.id.ibDeleteIngred);
			ibDelete.setTag(i);
			ibDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {	
//					AddRecipe.this.showDialog(ARE_YOU_SURE_DIALOG);
					AddRecipe.this.recipe.setName(recipeName.getText().toString());
					AddRecipe.this.recipe.setImage((String)recipeImage.getTag());
					AddRecipe.this.recipe.setRating(recipeRating.getRating());
					AddRecipe.this.recipe.setPreperation(recipePrep.getText().toString());									
					Ingredient i = (Ingredient) v.getTag();
					Log.d(debugTag,"Deleteing Ingredient " + i.getDescription() + " on Recipe " + AddRecipe.this.recipe.getName());
					if(AddRecipe.this.recipe.getId() != -1)
						AddRecipe.this.db.deleteIngredient(i.getId());
					else
						AddRecipe.this.recipe.removeIngredient(i);
					AddRecipe.this.Refresh();
				}
			});

			lvIngredients.addView(v);
		}           
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		if(id == ADD_INGREDIENT_DIALOG) {		
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.searchdailog);
			dialog.setTitle("Add Ingredient");
			Button okBtn = (Button) dialog.findViewById(R.id.Ok);
			Button cancelBtn = (Button) dialog.findViewById(R.id.Cancel);
			ingredient = (EditText) dialog.findViewById(R.id.TagText);
			ingredient.setHint(R.string.ingredients);
			okBtn.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					dismissDialog(ADD_INGREDIENT_DIALOG);
					String newIngred = AddRecipe.this.ingredient.getText().toString();
					int recipeId = -1;
					if(AddRecipe.this.recipe.getId() != -1)
					{
						recipeId = AddRecipe.this.recipe.getId();
						AddRecipe.this.db.insertIngredient(recipeId, AddRecipe.this.recipe.getIngredients().size(), newIngred);
					}
					AddRecipe.this.recipe.addIngredient(new Ingredient(recipeId, AddRecipe.this.recipe.getIngredients().size(), newIngred));
					AddRecipe.this.Refresh();
				}
			});
			cancelBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dismissDialog(ADD_INGREDIENT_DIALOG);
				}
			});
		}
		if(id == ARE_YOU_SURE_DIALOG)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure you want to delete this ingredient?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
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
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if(id == ADD_INGREDIENT_DIALOG) {		
			ingredient.setText("");
		}
		super.onPrepareDialog(id, dialog);
		
	}



	/** 
	 * Creates a menu option for editing   
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0,ADD_INGREDIENT_DIALOG,0,R.string.add).setIcon(R.drawable.ic_menu_add);
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
		case ADD_INGREDIENT_DIALOG:
			addIngredient.performClick();
			break;

		}
		return true;
	}  	

}
