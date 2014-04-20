package com.example.homework311rczaplic;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.homework311rczaplic.ShakeDetector.OnShakeListener;


/**
 * A list fragment representing a list of Articles. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ArticleDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ArticleListFragment extends ListFragment
{
	private static final String TAG = "ArticleListFragment";
	
	private SimpleCursorAdapter mAdapter;
	
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	// Shake detector variables
	private ShakeDetector mShakeDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
	
	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ArticleListFragment() {
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ShakeDetector initialization
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        // NEW IMPLEMENTATION
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new OnShakeListener()
        {
            @Override
            public void onShake(int count)
            {
            	xmlParse();
            }
        });        
        
// OLD IMPLEMENTATION - See ShakeDetector.java        
//        mShakeDetector = new ShakeDetector(new OnShakeListener() 
//        {
//            @Override
//            public void onShake()
//            {
//                xmlParse();
//            }
//        });
		
        // Use a cursor loader
        CursorLoader cl = new CursorLoader(getActivity(), Articles.CONTENT_URI, 
        		Articles.Article.PROJECTION, null, null, null);
        
        Cursor c = cl.loadInBackground();
        
        if (c == null) 
        {
            Log.e(TAG, "onCreate() Null Cursor from Query");
            
        }
        
        //////////////////// UNCOMMENT CODE TO DO BASIC, SIMPLE LIST VIEW
        
//        // Setup our mapping from the cursor result to the display field
//        String[] from = { Articles.Article.TITLE };
//        int[] to = { android.R.id.text1 };        
//
//        // Create a simple cursor adapter
//        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, c, from, to);
//        
//        // Associate the simple cursor adapter to the list view
//        setListAdapter(mAdapter);
        
        //////////////////// END OF COMMENTED OUT CODE
        
		String[] from = { Articles.Article.TITLE };
		int[] to = { R.id.title_textView };        
		
		// Create a simple cursor adapter
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_task, c, from, to);
		  
		// Associate the simple cursor adapter to the list view
		setListAdapter(mAdapter);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onPause() {
		mSensorManager.unregisterListener(mShakeDetector);
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
	}	
	
	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		
		Log.d(TAG, "TOUCHED ID = " + id);
		mCallbacks.onItemSelected(Long.toString(id));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
	
	private void xmlParse()
	{
		// If getActivity() == null, we need to stop any further processing
		if (getActivity() == null)
		{
			Log.d(TAG, "getActivity() == null....  Bad.");
			return;
		}
		
		Toast.makeText(getActivity(), "Loading XML Data", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Loading XML Data");

		// First remove all the existing data from the database
		ContentResolver cr = getActivity().getContentResolver();
		cr.delete(Articles.CONTENT_URI, null, null);		
		
		XmlPullParser parser = Xml.newPullParser();
		
		try 
		{
			InputStream in_s = getActivity().getAssets().open("HRD314_data.xml");
	        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);

            parseXML(parser);
		} 
		catch (XmlPullParserException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void parseXML(XmlPullParser parser) throws XmlPullParserException,IOException
	{
        int eventType = parser.getEventType();

        String title = "";
        String content = "";
        String icon = "";
        String date = "";

        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            String name = null;
            switch (eventType)
            {
                case XmlPullParser.START_TAG:
                    name = parser.getName();

                    if (name.equalsIgnoreCase("item"))
                    {
                    	// Reset all these values at the beginning of an item tag
                    	title = "";
                    	content = "";
                    	icon = "";
                    	date = "";
                    }
                    else if (name.equalsIgnoreCase("title"))
                    {
                    	title = parser.nextText();
                    }
                    else if (name.equalsIgnoreCase("content"))
                    {
                    	content = parser.nextText();
                    }
                    
                    // TODO: Parse out the icon and date data in HW312
                    /*
                    else if (name.equalsIgnoreCase("icon"))
                    {
                    	icon = parser.nextText();
                    }
                    else if (name.equalsIgnoreCase("date"))
                    {
                    	date = parser.nextText();
                    }
                    */
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("item"))
                    {
                    	ContentValues cv = new ContentValues();
                        cv.put(Articles.Article.TITLE, title);
                        cv.put(Articles.Article.CONTENT, content);
                        cv.put(Articles.Article.ICON, icon);
                        cv.put(Articles.Article.DATE, date);                        

                        ContentResolver cr = getActivity().getContentResolver();
                        cr.insert(Articles.CONTENT_URI, cv);
                    	
                    	Log.d(TAG, "Added an Article");
                    } 
            }
            eventType = parser.next();
        }
	}	
}
