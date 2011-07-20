package guyot.kevin.recipal.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import guyot.kevin.recipal.ReciPal;

public class ReciPalDB {
	private static final String DATABASE_NAME = "ReciPal.db";
	private static final int DATABASE_VERSION = 2;
	
	public static final String TABLE_RECIPE = "RECIPE";
	public static final String TABLE_RECIPE_ID = "_id";
	public static final String TABLE_RECIPE_NAME = "NAME";
	public static final String TABLE_RECIPE_PREPERATION = "PREPERATION";
	public static final String TABLE_RECIPE_IMAGE = "IMAGE";
	public static final String TABLE_RECIPE_RATING = "RATING";

	public static final String TABLE_INGREDIENT = "INGREDIENT";
	public static final String TABLE_INGREDIENT_ID = "_id";
	public static final String TABLE_INGREDIENT_RECIPE_ID = "RECIPE_ID";
	public static final String TABLE_INGREDIENT_SORT_ORDER = "SORT_ORDER";
	public static final String TABLE_INGREDIENT_DESCRIPTION = "DESCRIPTION";

	private Context context;
	private SQLiteDatabase db;

	private SQLiteStatement insertStmtRecipe;
	private SQLiteStatement insertStmtIngredient;
	
	private static final String INSERT_RECIPE = "insert into "
			+ TABLE_RECIPE + "(" 
			+ TABLE_RECIPE_NAME + ", "  
			+ TABLE_RECIPE_PREPERATION + ", " 
			+ TABLE_RECIPE_IMAGE + ", " 
			+ TABLE_RECIPE_RATING + ") values (?, ?, ?, ?)";

	private static final String INSERT_INGREDIENT = "insert into "
		+ TABLE_INGREDIENT + "(" 
		+ TABLE_INGREDIENT_RECIPE_ID + ", " 
		+ TABLE_INGREDIENT_SORT_ORDER + ", "
		+ TABLE_INGREDIENT_DESCRIPTION + ") values (?, ?, ?)";

	public ReciPalDB(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmtRecipe = this.db.compileStatement(INSERT_RECIPE);
		this.insertStmtIngredient = this.db.compileStatement(INSERT_INGREDIENT);
	}

	public long insertRecipe(String name, String prep, String image, float rating) {
		this.insertStmtRecipe.bindString(1, name);
		this.insertStmtRecipe.bindString(2, prep);
		this.insertStmtRecipe.bindString(3, image);
		this.insertStmtRecipe.bindDouble(4, rating);
		
		return this.insertStmtRecipe.executeInsert();
	}

	public long insertIngredient(int recipeId, int sortOrder, String description) {
		this.insertStmtIngredient.bindLong(1, recipeId);
		this.insertStmtIngredient.bindLong(2, sortOrder);
		this.insertStmtIngredient.bindString(3, description);
		return this.insertStmtIngredient.executeInsert();
	}
	
	public void deleteAll() {
		this.db.delete(TABLE_INGREDIENT, null, null);
		this.db.delete(TABLE_RECIPE, null, null);
	}

	public List<Recipe> selectAlltoObject() {
		List<Recipe> list = new ArrayList<Recipe>();
		Cursor cursor = this.db.query(TABLE_RECIPE, new String[] { TABLE_RECIPE_ID, TABLE_RECIPE_NAME, TABLE_RECIPE_PREPERATION, TABLE_RECIPE_IMAGE, TABLE_RECIPE_RATING }, 
				null, null, null, null, TABLE_RECIPE_NAME);
		if (cursor.moveToFirst()) {
			do {
				list.add(new Recipe(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getFloat(4)));
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}
	
	public Cursor selectAll(int sort) {
			
		String sortStr = TABLE_RECIPE_NAME + ", " + TABLE_RECIPE_RATING + " DESC";
		if(sort == ReciPal.SORT_RATING)
			sortStr = TABLE_RECIPE_RATING + " DESC, " + TABLE_RECIPE_NAME;
		
			return this.db.query(TABLE_RECIPE, new String[] { TABLE_RECIPE_ID, TABLE_RECIPE_NAME, TABLE_RECIPE_PREPERATION, TABLE_RECIPE_IMAGE, TABLE_RECIPE_RATING }, 
				null, null, null, null, sortStr);
	}	
	
	public Recipe SelectRecipeOnId(int id) {
		Recipe r = null;
		Cursor cursor = this.db.query(TABLE_RECIPE, new String[] { TABLE_RECIPE_ID, TABLE_RECIPE_NAME, TABLE_RECIPE_PREPERATION, TABLE_RECIPE_IMAGE, TABLE_RECIPE_RATING }, 
				TABLE_RECIPE_ID + " = ?",  new String[] { Integer.toString(id) }, null, null, TABLE_RECIPE_NAME);
		if (cursor.moveToFirst()) {
			r = new Recipe(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getFloat(4));
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return r;
	}
	
	public Recipe SelectRandomRecipe() {
		Recipe r = null;
		Cursor cursor = this.db.query(TABLE_RECIPE, new String[] { TABLE_RECIPE_ID, TABLE_RECIPE_NAME, TABLE_RECIPE_PREPERATION, TABLE_RECIPE_IMAGE, TABLE_RECIPE_RATING }, 
				null, null, null, null, "RANDOM() LIMIT 1");
		if (cursor.moveToFirst()) {
			r = new Recipe(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getFloat(4));
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return r;
	
	}

	public ArrayList<Ingredient> selectIngredientsByRecipeId(int recipeId) {
		ArrayList<Ingredient> list = new ArrayList<Ingredient>();

		Cursor cursor = this.db.query(TABLE_INGREDIENT, new String[] {TABLE_INGREDIENT_ID, TABLE_INGREDIENT_RECIPE_ID, TABLE_INGREDIENT_SORT_ORDER, TABLE_INGREDIENT_DESCRIPTION}, 
				TABLE_INGREDIENT_RECIPE_ID + " = ?", new String[] { Integer.toString(recipeId) }, null, null, TABLE_INGREDIENT_SORT_ORDER);
		if (cursor.moveToFirst()) {
			do {
				list.add(new Ingredient(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(3)));
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		
		return list;
	}

	public Cursor selectIngredientsByRecipeIdCursor(int recipeId) {

		Cursor cursor = this.db.query(TABLE_INGREDIENT, new String[] {TABLE_INGREDIENT_ID, TABLE_INGREDIENT_RECIPE_ID, TABLE_INGREDIENT_SORT_ORDER, TABLE_INGREDIENT_DESCRIPTION}, 
				TABLE_INGREDIENT_RECIPE_ID + " = ?", new String[] { Integer.toString(recipeId) }, null, null, TABLE_INGREDIENT_SORT_ORDER);
		
		return cursor;
	}

	public void deleteIngredientsForRecipe(int recipeId) {
		this.db.delete(TABLE_INGREDIENT, TABLE_INGREDIENT_RECIPE_ID + " = ?", new String[] { Integer.toString(recipeId) });		
	}

	public void deleteIngredient(int ingredId) {
		this.db.delete(TABLE_INGREDIENT, TABLE_INGREDIENT_ID + " = ?", new String[] { Integer.toString(ingredId) });				
	}
	
	public long insertRecipe(Recipe r) {
		long rc = insertRecipe(r.getName(), r.getPreperation(), r.getImage(), r.getRating());
		if(rc != -1)
		{
			if(r.getIngredients() != null){
				for(int i = 0; i < r.getIngredients().size(); i++)
					insertIngredient((int)rc, r.getIngredients().get(i).getSortOrder(), r.getIngredients().get(i).getDescription());
				}
		}
		return rc;
	}

	public long updateRecipe(Recipe r) {
		ContentValues cv=new ContentValues();
		cv.put(TABLE_RECIPE_NAME, r.getName());
		cv.put(TABLE_RECIPE_PREPERATION, r.getPreperation());
		cv.put(TABLE_RECIPE_RATING, r.getRating());
		cv.put(TABLE_RECIPE_IMAGE, r.getImage());
		return db.update(TABLE_RECIPE, cv, TABLE_RECIPE_ID+"=?", new String []{String.valueOf(r.getId())});   		
	}

	public Cursor selectOnName(String constraint, int sort) {
		
		String sortStr = TABLE_RECIPE_NAME + ", " + TABLE_RECIPE_RATING + " DESC";
		if(sort == ReciPal.SORT_RATING)
			sortStr = TABLE_RECIPE_RATING + " DESC, " + TABLE_RECIPE_NAME;
		
		return this.db.query(TABLE_RECIPE, new String[] { TABLE_RECIPE_ID, TABLE_RECIPE_NAME, TABLE_RECIPE_PREPERATION, TABLE_RECIPE_IMAGE, TABLE_RECIPE_RATING }, 
				TABLE_RECIPE_NAME + " like '%" + constraint + "%'", null, null, null, sortStr);
	}

	public void deleteRecipe(int recipeId) {
		this.db.delete(TABLE_RECIPE, TABLE_RECIPE_ID + " = ?", new String[] { Integer.toString(recipeId) });		
	}
	
	public void Close()
	{
		db.close();
	}
	
	private static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE Recipe ( " +
					  "_id           integer PRIMARY KEY AUTOINCREMENT NOT NULL, " +
					  "NAME         text NOT NULL, " +
					  "PREPERATION  text, " +
					  "IMAGE        text, " +
					  "RATING       numeric DEFAULT 0 " +
					  ");");
			
			db.execSQL("CREATE INDEX Recipe_Index01 " +
					  "ON Recipe " +
					  "(_id);");

			db.execSQL("CREATE INDEX Recipe_Index02 " +
					  "ON Recipe " +
					  "(NAME);");

			db.execSQL("CREATE TABLE Ingredient ( " +
					  "_id           integer PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, "+
					  "RECIPE_ID    integer, " +
					  "SORT_ORDER   integer DEFAULT 1, " +
					  "DESCRIPTION  text NOT NULL, " +
					  "FOREIGN KEY (RECIPE_ID) " +
					  "  REFERENCES Recipe(_id) " +
					  "  ON DELETE CASCADE " +
					  "  ON UPDATE NO ACTION " +
					  ");");

			db.execSQL("CREATE INDEX Ingredient_Index01 " +
					  "ON Ingredient " +
					  "(RECIPE_ID);");

					/* Data for table Recipe */
//			db.execSQL("INSERT INTO Recipe (NAME, PREPERATION, IMAGE, RATING) VALUES ('Baked Chicken Cordon Bleu', 'Process a few slices of bread in food processor to make about 4-5 cups of fresh breadcrumbs. Add Parmesan and 2 tsp onion powder. Set aside. Melt butter in a bowl and mix with oil. Set aside. Wash and clean chicken breasts.Pat dry and cut each breast into two pieces. You will have two pretty thin slices.Place a chicken piece on a cutting board. Lay a plastic wrap over the chicken piece and pound with a mallet to about a 1/4 inch thickness. Take care not to pound too hard because the meat may tear or create holes. Do the same thing with all the chicken pieces. You should have 10 chicken pieces to the original 5.', '', 2);");
//
//					/* Data for table Ingredient */
//			db.execSQL("INSERT INTO Ingredient (RECIPE_ID, SORT_ORDER, DESCRIPTION) VALUES (1, 1, '5 chicken breasts; split in 2 (total yield 10 pieces)');");
//			db.execSQL("INSERT INTO Ingredient (RECIPE_ID, SORT_ORDER, DESCRIPTION) VALUES (1, 2, '4 tsps onion powder; divided');");
//			db.execSQL("INSERT INTO Ingredient (RECIPE_ID, SORT_ORDER, DESCRIPTION) VALUES (1, 3, '3 cups fresh bread crumbs');");
//			db.execSQL("INSERT INTO Ingredient (RECIPE_ID, SORT_ORDER, DESCRIPTION) VALUES (1, 4, '3/4 cup shredded Parmesan');");
//			db.execSQL("INSERT INTO Ingredient (RECIPE_ID, SORT_ORDER, DESCRIPTION) VALUES (1, 5, 'salt/pepper; to taste');");
//			db.execSQL("INSERT INTO Ingredient (RECIPE_ID, SORT_ORDER, DESCRIPTION) VALUES (1, 6, '4 tbsp butter; melted');");
//			db.execSQL("INSERT INTO Ingredient (RECIPE_ID, SORT_ORDER, DESCRIPTION) VALUES (1, 7, '3 tbsp olive oil; or canola');");
//			db.execSQL("INSERT INTO Ingredient (RECIPE_ID, SORT_ORDER, DESCRIPTION) VALUES (1, 8, '10 slices deli ham; sliced thinly');");
//			db.execSQL("INSERT INTO Ingredient (RECIPE_ID, SORT_ORDER, DESCRIPTION) VALUES (1, 9, '10 slices Gruyere/Swiss cheese');");
//			db.execSQL("INSERT INTO Ingredient (RECIPE_ID, SORT_ORDER, DESCRIPTION) VALUES (1, 10, 'basil/parsley; optional sprinkle');");

//			db.execSQL("CREATE VIEW RECIPES_VIEW " +
//					"AS " +
//					"SELECT " + 
//					  "Recipe._id AS RECIPE_ID, " +
//					  "Recipe.NAME AS RECIPE_NAME, " +
//					  "Recipe.IMAGE AS RECIPE_IMAGE, " +
//					  "Recipe.PREPERATION AS RECIPE_PREPERATION, " +
//					  "Ingredient._id AS INGREDIENT_ID, " +
//					  "Ingredient.SORT_ORDER AS INGREDIENT_SORT_ORDER, " +
//					  "Ingredient.DESCRIPTION AS INGREDIENT_DESCRIPTION " +
//					"FROM  " +
//					  "Ingredient, " +
//					  "Recipe " +
//					"WHERE  " +
//					  "(Ingredient.RECIPE_ID = Recipe._id); ");		
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(DATABASE_NAME, "Upgrading database, this will drop tables and recreate.");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENT);
			onCreate(db);
		}
	}

}
