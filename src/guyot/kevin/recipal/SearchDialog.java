package guyot.kevin.recipal;

import guyot.kevin.recipal.R;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
/**
 * Dialog used to add a tag to a photo.
 * 
 * @author Kevin Guyot
 *
 */
public class SearchDialog extends Dialog implements OnCancelListener, OnDismissListener, OnClickListener {
    
	/**
	 * public interface to capture when a tag has been added.
	 * this was my solution to a modal dialog.
	 * 
	 * @author Kevin Guyot
	 *
	 */
	public interface SearchListener { 
        public void search(String tag);
    }
    
    /**
     * Dialog components 
     */
    private Button okBtn, cancelBtn; 
	private EditText txt;
	
	//the listener used to call after a tag is added
	private SearchListener searchListener;
	
	/**
	 * Constructor
	 * 
	 * @param context - calling context
	 * @param imageId - imageId
	 * @param addTagListener - tag to add
	 */
	public SearchDialog(Context context, SearchListener searchListener) {
		super(context);
		
		this.setContentView(R.layout.searchdailog);
		this.setTitle("Search");
		
		//the ok button
		this.okBtn = (Button) findViewById(R.id.Ok);
		this.okBtn.setOnClickListener(this);
		
		//the cancel button
		this.cancelBtn = (Button) findViewById(R.id.Cancel);
		this.cancelBtn.setOnClickListener(this);

		//tag text added
		this.txt = (EditText)findViewById(R.id.TagText);


		//function to call after tag is added.
		this.searchListener = searchListener;
	}

	public void onClick(View v) {
		 if(v.getId() == R.id.Ok)
		 {
			String tag = this.txt.getText().toString(); //tag that was entered
			searchListener.search(tag);
			this.dismiss(); //close the dialog
			
		 }
		 else
			 this.cancel();//cancel the dialog.
	}

	//unused and not implemented stubs.
	public void onDismiss(DialogInterface dialog) {
	}

	public void onCancel(DialogInterface dialog) {
	}

}