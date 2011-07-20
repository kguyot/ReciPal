package guyot.kevin.recipal;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.os.Bundle;

public class ReciPalBaseActivity extends Activity {
	private GoogleAnalyticsTracker tracker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 tracker = GoogleAnalyticsTracker.getInstance();
		 tracker.start("UA-24320632-1", this); 		    // Start the tracker in manual dispatch mode...
		 tracker.setDispatchPeriod(10);		 
	}

	@Override
	protected void onResume() {
		super.onResume();
		 tracker.trackPageView("/" + this.getLocalClassName());
		 tracker.dispatch(); //Force the Dispatch;
	}

	  @Override
	  protected void onDestroy() {
	    super.onDestroy();
	    // Stop the tracker when it is no longer needed.
	    tracker.stop();
	  }
}
