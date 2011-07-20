package guyot.kevin.recipal.tasks;

import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Image cache and async fetcher task for recipe images.
 * 
 * @author Kevin Guyot
 */
public class RecipeIconTask {
	
	private static final String debugTag = "RecipeIconTask";
	private static final String debugTagImageTask = "ImageTask";

    private ConcurrentHashMap<String, Object> imageCache;
    private BaseAdapter adapt;
      
    public RecipeIconTask ()
    {
        imageCache = new ConcurrentHashMap<String, Object>();
    }
    
    public Bitmap loadImage (BaseAdapter adapt, ImageView view)
    {
        this.adapt = adapt;
        String filePath = (String) view.getTag();
        Bitmap result = null;
        if (imageCache.containsKey(filePath) )
        {
        	Log.d(debugTag, "Loading from Hashmap: " + filePath);
        	Object value = imageCache.get(filePath); 
        	if(value instanceof Bitmap)
        		result = (Bitmap)value;
        }
        else {
        	Log.d(debugTag, "Loading From Disk: " + filePath);
        	imageCache.put(filePath, 1);
            new ImageTask().execute(filePath);
        }
        return result;
    }
    
    private class ImageTask extends AsyncTask<String, Void, Bitmap>
    {
        private String s_filePath;

        @Override
        protected Bitmap doInBackground(String... params) {
        	s_filePath = params[0];
        	Bitmap bmp = null;

            Log.d(debugTagImageTask, "Fetching: " + s_filePath);
            try {            	
            	bmp = BitmapUtils.createImageThumbnail(s_filePath, Images.Thumbnails.MICRO_KIND);
            	String bmpLoaded = "";
            	if(bmp == null)
            		bmpLoaded = "false";
            	else
            		bmpLoaded = "true";
            	Log.d(debugTagImageTask, "Fetched: " + s_filePath + " Bitmap Loaded: " + bmpLoaded);
            }
            catch(Exception e){
            	 Log.d(debugTagImageTask, "Exception: " + e.getMessage());
            	throw new RuntimeException(e);
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if(result != null)
            {
            	Log.d(debugTagImageTask, "Placing [" + s_filePath + "] in to hashmap");
            	imageCache.put(s_filePath, result);   
            	
            	Log.d(debugTagImageTask, "notifyDataSetChanged: " + s_filePath);
   				adapt.notifyDataSetChanged();
            }
        }
    }
}
