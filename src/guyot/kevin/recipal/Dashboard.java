package guyot.kevin.recipal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.bumpapi.BumpAPI;
import com.bumptech.bumpapi.BumpAPIListener;
import com.bumptech.bumpapi.BumpConnectFailedReason;
import com.bumptech.bumpapi.BumpConnection;
import com.bumptech.bumpapi.BumpDisconnectReason;

import guyot.kevin.recipal.R;
import guyot.kevin.recipal.SearchDialog.SearchListener;
import guyot.kevin.recipal.data.ReciPalDB;
import guyot.kevin.recipal.data.Recipe;

public class Dashboard extends ReciPalBaseActivity implements SearchListener,
		BumpAPIListener {

	private static final int BUMP_DIALOG = 0;
	private static final String debugTag = "Dashboard";

	private ImageButton viewRecipes;
	private ImageButton searchRecipe;
	private ImageButton addRecipe;
	private ImageButton bumpRecipe;
	private ImageButton about;

	private BumpConnection conn;
	private ProgressDialog progDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);

		viewRecipes = (ImageButton) this.findViewById(R.id.viewrecipes);
		searchRecipe = (ImageButton) this.findViewById(R.id.searchrecipe);
		addRecipe = (ImageButton) this.findViewById(R.id.addrecipe);
		bumpRecipe = (ImageButton) this.findViewById(R.id.bumprecipe);
		about = (ImageButton) this.findViewById(R.id.about);

		viewRecipes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(Dashboard.this, TabWidget.class));
			}
		});

		searchRecipe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SearchDialog d = new SearchDialog(Dashboard.this,
						Dashboard.this);
				d.show();

			}
		});

		addRecipe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Dashboard.this, AddRecipe.class);
				startActivityForResult(i, ReciPal.RECIPE_ADD_DIALOG);
			}
		});

		bumpRecipe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Dashboard.this.BumpConnect();
			}
		});
		
		about.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(Dashboard.this, About.class));
			}
		});		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check if the returning activity is EDIT_REQUEST...
		// if it was something else it is not something we know how to handle
		if (requestCode == ReciPal.RECIPE_ADD_DIALOG) {
			String msg = null; // Holder to toast a success / canceled msg to
								// user...

			// check if we have data returned from the activity and if the
			// result was OK
			// if the result was not OK and the data was null
			// then the activity was most likely canceled...or something else
			// happened
			if (data != null && resultCode == Activity.RESULT_OK) {
				ReciPalDB db = new ReciPalDB(this);
				Recipe r = (Recipe) data.getExtras().getSerializable(
						"edu.gvsu.cis680.recipal.recipe");
				r.setId((int) db.insertRecipe(r));
				db.Close();
				db = null;

				if (r.getId() != -1) {
					msg = "Added - " + r.getName();

					Intent i = new Intent(Dashboard.this, ViewRecipe.class);
					i.putExtra("edu.gvsu.cis680.recipal.recipe", r);
					startActivity(i);
				}
			} else {
				msg = "Canceled";
			}
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}

		if (requestCode == BUMP_DIALOG) {
			if (resultCode == RESULT_OK) {
	        	Log.d(debugTag, "Returned from Bump Dialog-OK");

		    	progDialog = ProgressDialog.show(this, this.getResources().getString(R.string.receiving_recipe), this.getResources().getString(R.string.receiving_recipe) , true, false);
				conn = (BumpConnection) data.getParcelableExtra(BumpAPI.EXTRA_CONNECTION);
				conn.setListener(this);
			
				
			} else {
	        	Log.d(debugTag, "Returned from Bump Dialog-Failure");

				BumpConnectFailedReason reason = (BumpConnectFailedReason) data.getSerializableExtra(BumpAPI.EXTRA_REASON);
				Log.wtf(debugTag,("--- Failed to connect (" + reason.toString() + ")---"));
			}
		}
	}

	@Override
	public void search(String tag) {

		Intent i = new Intent(Dashboard.this, ReciPal.class);
		i.putExtra("edu.gvsu.cis680.recipal.search", tag);
		startActivity(i);
	}

	protected void BumpConnect() {
		Intent bump = new Intent(this, BumpAPI.class);
		bump.putExtra(BumpAPI.EXTRA_USER_NAME, "ReciPal");
		bump.putExtra(BumpAPI.EXTRA_API_KEY, "b970b8717d934086aee23de96032d686");
		bump.putExtra(BumpAPI.EXTRA_ACTION_MSG, "Bump with another phone to recieve recipe.");
		try { 
			startActivityForResult(bump, BUMP_DIALOG);
		}
		catch (Exception e)
		{
			Log.wtf(debugTag, e.getMessage());
		}
	}

	@Override
	public void bumpDataReceived(byte[] yourBytes) {
		ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);
		ObjectInput in;
		try {
			in = new ObjectInputStream(bis);
			Recipe r = (Recipe) in.readObject();
			progDialog.dismiss();
			r.setId(-1);
			r.setImage("");
			Intent i = new Intent(Dashboard.this, AddRecipe.class);
			i.putExtra("edu.gvsu.cis680.recipal.recipe", r);
			startActivityForResult(i, ReciPal.RECIPE_ADD_DIALOG);
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void bumpDisconnect(BumpDisconnectReason reason) {
		switch (reason) {
		case END_OTHER_USER_QUIT:
			Toast.makeText(this,
					("--- " + conn.getOtherUserName() + " QUIT ---"),
					Toast.LENGTH_LONG).show();
			break;
		case END_OTHER_USER_LOST:
			Toast.makeText(this,
					("--- " + conn.getOtherUserName() + " LOST ---"),
					Toast.LENGTH_LONG).show();
			break;
		}
	}

	@Override
	public void onStop() {
		if (conn != null)
			conn.disconnect();

		super.onStop();
	}
}
