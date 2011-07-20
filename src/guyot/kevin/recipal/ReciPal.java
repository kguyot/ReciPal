package guyot.kevin.recipal;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import guyot.kevin.recipal.R;
import guyot.kevin.recipal.adapters.ReciPalCursorAdapter;
import guyot.kevin.recipal.data.ReciPalDB;
import guyot.kevin.recipal.data.Recipe;
import guyot.kevin.recipal.tasks.RecipeIconTask;

public class ReciPal extends ReciPalBaseActivity implements OnItemClickListener {
	final static int RECIPE_ADD_DIALOG = 1;
	final static int RECIPE_VIEW_DIALOG = 2;
	final static int RECIPE_EDIT_DIALOG = 3;
	public static final int SORT_AZ = 1;
	public static final int SORT_RATING = 2;

	//private ReciPalListAdapter listAdapter;
	private ReciPalCursorAdapter cursorAdapter;
	private ReciPalDB db;
	private ListView lvRecipes;

	private String searchTag;
	private RecipeIconTask recipeIconTask;
	private int sort;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.searchTag = "";
		this.sort = SORT_AZ;
		this.recipeIconTask = new RecipeIconTask(); 

		Bundle extras = this.getIntent().getExtras();
        if(extras != null)
        {
        	this.searchTag = extras.getString("edu.gvsu.cis680.recipal.search");
        	if(this.searchTag == null)
        	{
        		this.searchTag = "";
        		setContentView(R.layout.recipielist);
        	}
        	else
        	{
        		setContentView(R.layout.search);
        		TextView searchText = (TextView)this.findViewById(R.id.searchTerm);
        		searchText.setText("Search: " + searchTag);        		
        	}        	
        	this.sort = extras.getInt("edu.gvsu.cis680.recipal.sort");        	
        }

		lvRecipes = (ListView) this.findViewById(R.id.RecipeList);				
	}

	@Override
	protected void onStart() {
		super.onStart();
		if(db != null)
			db.Close();
		
		db = new ReciPalDB(this);

		Refresh();
	}

	@Override
	protected void onStop() {
		super.onStop();
		db.Close();
		db = null;
	}

	public void Refresh()
	{
		if(this.searchTag.length() != 0)
			Refresh(db.selectOnName(this.searchTag, this.sort));
		else
			Refresh(db.selectAll(this.sort));
	}
	
	public void Refresh(Cursor cursor)
	{
		startManagingCursor(cursor);
        String[] from = new String[] { ReciPalDB.TABLE_RECIPE_NAME };
        int[] to = new int[] { R.id.RecipeName };
		
        
		cursorAdapter = new ReciPalCursorAdapter(this.recipeIconTask, this, R.layout.recipelistitem, cursor, from, to);
		lvRecipes.setAdapter(cursorAdapter);
		lvRecipes.setOnItemClickListener(this);
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
    	if(requestCode == RECIPE_EDIT_DIALOG)
    	{
    		String msg = null;  //Holder to toast a success / canceled msg to user...
    		
    		//check if we have data returned from the activity and if the result was OK
    		//if the result was not OK and the data was null
    		//then the activity was most likely canceled...or something else happened
    		if(data != null && resultCode == Activity.RESULT_OK)
    		{
    			Recipe r = (Recipe) data.getExtras().getSerializable("edu.gvsu.cis680.recipal.recipe");    			    			
    			msg = "Udpdating - " + r.getName();
	    		
    			db.updateRecipe(r);	    			    		
    			//refresh the display
    			cursorAdapter.getCursor().requery();
    		}
    		else
    		{
    			msg = "Canceled";
    		}
    		Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();    		
    	}
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Recipe r = db.SelectRecipeOnId((int) id);

		Intent i = new Intent(ReciPal.this, ViewRecipe.class);
		i.putExtra("edu.gvsu.cis680.recipal.recipe", r);
		startActivityForResult(i, RECIPE_VIEW_DIALOG);
	}
}