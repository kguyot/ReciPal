package guyot.kevin.recipal;
/**
 * TabWidget Activity
 * 
 * Is the main actibity of the application
 * implementing the tab based interface for the application.
 * It adds tabs for Me, Vanity and Map and calls the 
 * respective activity.
 * 
 * @author Kevin Guyot
 *
 */
import java.util.List;

import guyot.kevin.recipal.R;
import guyot.kevin.recipal.SearchDialog.SearchListener;
import guyot.kevin.recipal.data.ReciPalDB;
import guyot.kevin.recipal.data.Recipe;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

public class TabWidget extends TabActivity implements SearchListener, SensorEventListener {
	private static final int RECIPE_ADD = 1;
	private static final int RECIPE_SEARCH = 2;
	private static final int RECIPE_RANDOM = 3;
	private ImageView searchBtn;
	private ImageView addBtn;

	//Sensor variables
	private SensorManager sensorManager;
	private Sensor sensor;	
	private long lastUpdate = -1;
	private long currentTime = -1;
	private float last_x, last_y, last_z;
	private float current_x, current_y, current_z, currenForce;
	private static final int FORCE_THRESHOLD = 900;
	private final int DATA_X = SensorManager.DATA_X;
	private final int DATA_Y = SensorManager.DATA_Y;
	private final int DATA_Z = SensorManager.DATA_Z;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tablayout);

		InitShakeDetection();

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost();  // The activity TabHost
		TabHost.TabSpec spec;  // Reusable TabSpec for each tab
		Intent intent;  // Reusable Intent for each tab


		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, ReciPal.class);
		intent.putExtra("edu.gvsu.cis680.recipal.sort", ReciPal.SORT_AZ);
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec(res.getString(R.string.alphabetical)).setIndicator(res.getString(R.string.alphabetical), res.getDrawable(R.drawable.ic_tab_az)).setContent(intent);
		tabHost.addTab(spec);

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, ReciPal.class);
		intent.putExtra("edu.gvsu.cis680.recipal.sort", ReciPal.SORT_RATING);
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec(res.getString(R.string.rating)).setIndicator(res.getString(R.string.rating), res.getDrawable(R.drawable.ic_tab_rating)).setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);

		searchBtn = (ImageView)this.findViewById(R.id.RecipeSearch);
		addBtn = (ImageView)this.findViewById(R.id.RecipeAdd);

		searchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//lvRecipes.setFilterText("Chicken");
				SearchDialog d = new SearchDialog(TabWidget.this, TabWidget.this);
				d.show();			
			}
		});

		addBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(TabWidget.this, AddRecipe.class);
				startActivityForResult(i, ReciPal.RECIPE_ADD_DIALOG);
			}
		});

	}

	private void InitShakeDetection() {
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sensor = sensors.get(0);
		}
	}	

	@Override
	public void search(String tag) {
		Intent i = new Intent(TabWidget.this, ReciPal.class);
		i.putExtra("edu.gvsu.cis680.recipal.search", tag);
		startActivity(i);		
	}	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//Check if the returning activity is EDIT_REQUEST...
		//if it was something else it is not something we know how to handle
		if(requestCode == ReciPal.RECIPE_ADD_DIALOG)
		{
			String msg = null;  //Holder to toast a success / canceled msg to user...

			//check if we have data returned from the activity and if the result was OK
			//if the result was not OK and the data was null
			//then the activity was most likely canceled...or something else happened
			if(data != null && resultCode == Activity.RESULT_OK)
			{
				ReciPalDB db = new ReciPalDB(this);
				Recipe r = (Recipe) data.getExtras().getSerializable("edu.gvsu.cis680.recipal.recipe");    			    			
				r.setId((int) db.insertRecipe(r));
				db.Close();
				db = null;

				msg = "Added - " + r.getName();

				//refresh the display
				//cursorAdapter.getCursor().requery();
			}
			else
			{
				msg = "Canceled";
			}
			Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
		} 
	}

	/**
	 * used to save that state of the information between screen rotations.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {		

		//Store the Current selected tab
		outState.putInt("edu.gvsu.cis680.homework4.currTab", getTabHost().getCurrentTab());
	}    

	/**
	 * After the screen has been rotated recall the data that was saved from onSaveInstanceState
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		//Restore the current selected tab
		getTabHost().setCurrentTab(savedInstanceState.getInt("edu.gvsu.cis680.homework4.currTab"));	
	}

	/** 
	 * Creates a menu option for editing   
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0,RECIPE_ADD,0,R.string.add).setIcon(R.drawable.ic_menu_add);
		menu.add(0,RECIPE_SEARCH,1,R.string.search).setIcon(R.drawable.ic_menu_search);
		menu.add(0,RECIPE_RANDOM,2,R.string.random).setIcon(R.drawable.ic_menu_random);
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
		case RECIPE_ADD:
			addBtn.performClick();
			break;
		case RECIPE_SEARCH:
			searchBtn.performClick();
			break;
		case RECIPE_RANDOM:
			viewRandomRecipe();
			break;
		}
		return true;
	}  	

	@Override
	protected void onStart() {
		if (sensor!=null)  {
			sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
		}
		super.onStart();		
	}

	@Override
	public void onStop() {
		sensorManager.unregisterListener(this);
		super.onStop();
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER || event.values.length < 3)
		      return;

		currentTime = System.currentTimeMillis();

		if ((currentTime - lastUpdate) > 100) {
			long diffTime = (currentTime - lastUpdate);
			lastUpdate = currentTime;

			current_x = event.values[DATA_X];
			current_y = event.values[DATA_Y];
			current_z = event.values[DATA_Z];

			currenForce = Math.abs(current_x+current_y+current_z - last_x - last_y - last_z) / diffTime * 10000;

			if (currenForce > FORCE_THRESHOLD) {

				// Device has been shaken now go on and do something
				viewRandomRecipe();
			}
			last_x = current_x;
			last_y = current_y;
			last_z = current_z;
		}		
	}

	private void viewRandomRecipe() {
		ReciPalDB db = new ReciPalDB(this);
		Recipe r = db.SelectRandomRecipe();
		db.Close();

		Intent i = new Intent(TabWidget.this, ViewRecipe.class);
		i.putExtra("edu.gvsu.cis680.recipal.recipe", r);
		startActivity(i);
	}  
}
