package guyot.kevin.recipal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class About extends ReciPalBaseActivity  {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		Button btn = (Button)this.findViewById(R.id.sharerecipal);
		btn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				ShareReciPal();
			}
		});
	}
		
	
	void ShareReciPal()
	{
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=guyot.kevin.recipal"));
		startActivity(intent);	
	}
}
