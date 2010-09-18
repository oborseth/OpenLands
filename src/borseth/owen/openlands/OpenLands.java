package borseth.owen.openlands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class OpenLands extends Activity {
	
	public static final String PREFS_NAME = "OpenLands";
	
	private DataHelper dh;
	private HashMap<String, String> mapTilesMap = null;
	private HashMap<String, List<HashMap<String, String>>> mapTileObjects = null;
	private HashMap<String, Boolean> mapTileAllowedMap = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String lastX = settings.getString("lastX", "0");
        String lastY = settings.getString("lastY", "0");
        boolean showSplash = settings.getBoolean("showSplash", true);
        
		SharedPreferences.Editor editor = settings.edit();
        
		Typeface tangerineBold = Typeface.createFromAsset(this.getAssets(), "fonts/Tangerine_Bold.ttf");
		Typeface tangerineRegular = Typeface.createFromAsset(this.getAssets(), "fonts/Tangerine_Regular.ttf");
		
		if(showSplash)
		{
			editor.putBoolean("showSplash", false);
			
	        MyDialog d = new MyDialog(this);
			d.setTitle("");
			
			TextView dialogTitle = (TextView)d.findViewById(R.id.dialogTitle);
			dialogTitle.setText(R.string.app_name);
			dialogTitle.setTypeface(tangerineBold);
			
			TextView dialogText = (TextView)d.findViewById(R.id.dialog);
			dialogText.setText(R.string.splash);
			dialogText.setTypeface(tangerineRegular);
					
	    	LayoutParams params = d.getWindow().getAttributes();
	    	params.height = LayoutParams.FILL_PARENT;
	    	params.width = LayoutParams.FILL_PARENT;
	    	
	    	d.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
			d.show();
		}
        
        this.dh = new DataHelper(this);
        this.mapTilesMap = this.dh.loadMapTiles();
        this.mapTileObjects = this.dh.loadMapTileObjects();
        this.mapTileAllowedMap = this.dh.loadMapTileAllow();

        Boolean allowedTile = mapTileAllowedMap.get(lastX+":"+lastY);
        if(allowedTile == null || allowedTile)
        {
    		editor.putString("lastX", "0");
    		editor.putString("lastY", "0");
    		lastX = "0";
    		lastY = "0";
        }		

		editor.commit();

        int mapTileResId = getResources().getIdentifier("tile_"+lastX+"_"+lastY, "drawable", getPackageName());

        DisplayMetrics metrics = new DisplayMetrics(); 
        getWindowManager().getDefaultDisplay().getMetrics( metrics ); 
        setContentView(new Panel(this, mapTileResId, mapTilesMap, mapTileObjects, mapTileAllowedMap, metrics, settings, Integer.parseInt(lastX), Integer.parseInt(lastY)));
    }
    
    @Override
    public void onRestart()
    {
    	super.onRestart();
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    }
    
    @Override
    public void onStop()
    {
    	this.finish();
    	super.onStop();
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    }
}

class Panel extends SurfaceView implements SurfaceHolder.Callback {
	private GameThread _thread;
	private ArrayList<GraphicObject> _graphics = new ArrayList<GraphicObject>();
	
    public int _x = 0;
    public int _y = 0;
    public int _px = 192;
    public int _py = 0;
    public int _currentMapId = 0;
    public int _previousMapId = 0;
    public int _piece = 0;
    public float _upX = 0;
    public float _upY = 0;
    public float _downX = 0;
    public float _downY = 0;
    public float _leftX = 0;
    public float _leftY = 0;
    public float _rightX = 0;
    public float _rightY = 0;
    
    public boolean _allowUp = false;
    public boolean _allowDown = false;
    public boolean _allowLeft = false;
    public boolean _allowRight = false;
    
    public HashMap<String, String> _mapTilesMap = null;
    public HashMap<String, List<HashMap<String, String>>> _mapTileObjects = null;
    public List<HashMap<String, String>> _currentMapObjects = null;
    public HashMap<String, Boolean> _mapTileAllowedMap = null;
    public DisplayMetrics _metrics = null;
    SharedPreferences _settings = null;
    private Vibrator _vibrator;
	
    public Panel(Context context, int mapTileResId, HashMap<String, String> mapTilesMap, HashMap<String, List<HashMap<String, String>>> mapTileObjects, HashMap<String, Boolean> mapTileAllowedMap, DisplayMetrics metrics, SharedPreferences settings, int x, int y) {
        super(context);
        _currentMapId = mapTileResId;
        _mapTilesMap = mapTilesMap;
        _mapTileObjects = mapTileObjects;
        _mapTileAllowedMap = mapTileAllowedMap;
        _metrics = metrics;
        _settings = settings;
        _x = x;
        _y = y;
        
        _vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        
        getHolder().addCallback(this);
        _thread = new GameThread(getHolder(), this);
        setFocusable(true);
    }
 
    public boolean setMapTile()
    {        
    	String mapTile = "";   	
   	
    	if((mapTile = _mapTilesMap.get(_x+":"+_y)) != null)
    	{
	        int resID = getResources().getIdentifier(mapTile, "drawable", this.getContext().getPackageName());
	        _previousMapId = _currentMapId;
	        _currentMapId = resID;
	        
    		SharedPreferences.Editor editor = _settings.edit();
    		editor.putString("lastX", String.valueOf(_x));
    		editor.putString("lastY", String.valueOf(_y));
    		editor.commit();
    		
	        return(true);
    	}
    	
    	return(false);
    }
    
    public boolean setMapPieces()
    {	
    	_currentMapObjects = _mapTileObjects.get(_x+":"+_y);
    	
        Boolean allowedTile = _mapTileAllowedMap.get(_x+":"+_y);
        if(allowedTile == null || allowedTile)
        {
        	return false;
        }

    	if(_mapTilesMap.get(_x+":"+String.valueOf(_y+1)) != null)
    		_allowUp = true;
    	else
    		_allowUp = false;
        
        if(_mapTilesMap.get(_x+":"+String.valueOf(_y-1)) != null)
        	_allowDown = true;
    	else
    		_allowDown = false;
        
        if(_mapTilesMap.get(String.valueOf(_x-1)+":"+_y) != null)
        	_allowLeft = true;
    	else
    		_allowLeft = false;
        
	    if(_mapTilesMap.get(String.valueOf(_x+1)+":"+_y) != null)
	    	_allowRight = true;
    	else
    		_allowRight = false;
	    
    	return(true);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
    	return false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) 
    {
    	int eventAction = event.getAction();
    	float x = event.getX();
    	float y = event.getY();
    	boolean tileChange = true;
    	    	
    	if(eventAction == MotionEvent.ACTION_DOWN)
    	{
    		if(_allowUp && 
    				x < (_upX + 20) && x > (_upX - 20) && 
    				y < (_upY + 20) && y > (_upY - 20))
    		{
    			_y++;
    		}
    		else if(_allowDown && 
    				x < (_downX + 20) && x > (_downX - 20) && 
    				y < (_downY + 20) && y > (_downY - 20))
    		{
    			_y--;
    		}
    		else if(_allowLeft && 
    				x < (_leftX + 20) && x > (_leftX - 20) && 
    				y < (_leftY + 20) && y > (_leftY - 20))
    		{
    			_x--;
    		}
    		else if(_allowRight && 
    				x < (_rightX + 20) && x > (_rightX - 20) && 
    				y < (_rightY + 20) && y > (_rightY - 20))
    		{
    			_x++;
    		}
    		else
    			tileChange = false;
    		
    		if(tileChange)
    		{
	    		_vibrator.vibrate(100);
	    		this.setMapTile();
    		}
    		
    		return(true);
    	}
		
        return false;
    }
    
    @Override
    public void onDraw(Canvas canvas) {
    	canvas.drawColor(Color.BLACK);
        Bitmap mapTile = BitmapFactory.decodeResource(getResources(), _currentMapId);
        canvas.drawBitmap(mapTile, 0, 0, null);
        
        if(_currentMapObjects != null)
        {
        	for(int i=0; i< _currentMapObjects.size(); i++)
      	  	{
        		HashMap<String, String> mapObject = _currentMapObjects.get(i);
        		
        		if(mapObject != null)
        		{
	        		int mapObjectImageResource = getResources().getIdentifier(mapObject.get("name"), "drawable", this.getContext().getPackageName());
	        		Bitmap mapObjectImage = BitmapFactory.decodeResource(getResources(), mapObjectImageResource);
	        		canvas.drawBitmap(mapObjectImage, Float.valueOf(mapObject.get("ox")), Float.valueOf(mapObject.get("oy")), null);
        		}
      	  	}
        }
        
        if(_allowUp)
        {
        	Bitmap upArrow = BitmapFactory.decodeResource(getResources(), R.drawable.uparrow);

        	int thisX = ((mapTile.getWidth() / 2) - (upArrow.getWidth() / 2));
        	int thisY = 5;
        	_upX = mapTile.getWidth() / 2;
        	_upY = 5 + (upArrow.getHeight() / 2);
        	
        	canvas.drawBitmap(upArrow, thisX, thisY, null);
        }
        
        if(_allowDown)
        {
        	Bitmap downArrow = BitmapFactory.decodeResource(getResources(), R.drawable.downarrow);
        	
        	int thisX = ((mapTile.getWidth() / 2) - (downArrow.getWidth() / 2));
        	int thisY = ((mapTile.getHeight() / 1) - (downArrow.getHeight()) - 5);
        	_downX = mapTile.getWidth() / 2;
        	_downY = mapTile.getHeight() - (downArrow.getHeight() / 2) - 5;
        	
        	canvas.drawBitmap(downArrow, thisX, thisY, null);
        }
        
        if(_allowLeft)
        {
	        Bitmap leftArrow = BitmapFactory.decodeResource(getResources(), R.drawable.leftarrow);
	        
        	int thisX = 5;
        	int thisY = ((mapTile.getHeight() / 2) - (leftArrow.getHeight() / 2));
        	_leftX = 5 + (leftArrow.getWidth() / 2);
        	_leftY = (mapTile.getHeight() / 2);
        	
	        canvas.drawBitmap(leftArrow, thisX, thisY, null);
        }
        
	    if(_allowRight)
	    {
	        Bitmap rightArrow = BitmapFactory.decodeResource(getResources(), R.drawable.rightarrow);
	        
        	int thisX = ((mapTile.getWidth() / 1) - rightArrow.getWidth() - 5);
        	int thisY = ((mapTile.getHeight() / 2) - (rightArrow.getWidth() / 2));
        	_rightX = mapTile.getWidth() - (rightArrow.getWidth() / 2) - 5;
        	_rightY = (mapTile.getHeight() / 2);

	        canvas.drawBitmap(rightArrow, thisX, thisY, null);
	    }
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        	_thread.setRunning(true);
        	_thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // simply copied from sample application LunarLander:
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        _thread.setRunning(false);
        while (retry) {
            try {
                _thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
    }
}

class GameThread extends Thread {
    private SurfaceHolder _surfaceHolder;
    private Panel _panel;
    private boolean _run = false;

    public GameThread(SurfaceHolder surfaceHolder, Panel panel) {
        _surfaceHolder = surfaceHolder;
        _panel = panel;
    }

    public void setRunning(boolean run) {
        _run = run;
    }
    
    public boolean getRunning() {
        return(_run);
    }

    @Override
    public void run() {
        Canvas c;
        while (_run) {
            c = null;
            try {
                c = _surfaceHolder.lockCanvas(null);
                synchronized (_surfaceHolder) {
                    _panel.onDraw(c);
                    
                    if(_panel._currentMapId != _panel._previousMapId)
                    	_panel.setMapPieces();	
                }
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    _surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }
}