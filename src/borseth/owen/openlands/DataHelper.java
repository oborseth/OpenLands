package borseth.owen.openlands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.widget.Toast;

public class DataHelper {
	   private static final String DATABASE_NAME = "owbo";
	   private static final int DATABASE_VERSION = 12;
	   private static final String TABLE_NAME = "table1";

	   private Context context;
	   private SQLiteDatabase db;

	   private SQLiteStatement insertStmt;

	   public DataHelper(Context context) {
	      this.context = context;
	      OpenHelper openHelper = new OpenHelper(this.context);
	      this.db = openHelper.getWritableDatabase();
	   }

	   public long insert(String name) {
	      this.insertStmt.bindString(1, name);
	      return this.insertStmt.executeInsert();
	   }

	   public void deleteAll() {
	      this.db.delete(TABLE_NAME, null, null);
	   }

	   public HashMap<String, Boolean> loadMapTileAllow()
	   {
		   	HashMap<String, Boolean> mapTileAllowMap = new HashMap<String, Boolean>();
		   	Cursor cursor = null;

		   	try
		   	{
		   		cursor = this.db.query("map_tile_allow", new String[] { "x", "y" }, null, null, null, null, null);
		   	}
		   	catch(Exception e)
		   	{
		   		Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		   		return mapTileAllowMap;
		   	}

		   	if (cursor.moveToFirst()) 
		   	{
		   		do 
		   		{
		   			String x = cursor.getString(0);
					String y = cursor.getString(1);
					
					mapTileAllowMap.put(x+":"+y, true); 
		        } while (cursor.moveToNext());
		   	}
		    
		   	if (cursor != null && !cursor.isClosed()) 
		   	{
		   		cursor.close();
		    }

  		 	return(mapTileAllowMap);
	   }
	   
	   public HashMap<String, String> loadMapTiles()
	   {
		   	HashMap<String, String> mapTilesMap = new HashMap<String, String>();
		   
  		   	String[] mapTiles = context.getResources().getStringArray(R.array.map_tiles);
  		 	for(int i = 0; i < mapTiles.length; i++)
  		 	{
  		 		String[] mapTilesArray = mapTiles[i].split(":");
  		 		
				String x = mapTilesArray[0];
				String y = mapTilesArray[1];
				String tileImageFile = "tile_"+x+"_"+y;
				   
		        mapTilesMap.put(x+":"+y, tileImageFile);
  		 	}
  		 	
  		 	return(mapTilesMap);
	   }
	   
	   public HashMap<String, List<HashMap<String, String>>> loadMapTileObjects()
	   {        
		   	HashMap<String, List<HashMap<String, String>>> mapTileObjects = new HashMap<String, List<HashMap<String, String>>>();
		   
		   	String[] mapTileObjectArray = context.getResources().getStringArray(R.array.map_tile_objects);
	  		for(int i = 0; i < mapTileObjectArray.length; i++)
	  		{
	  			String[] mapTileObjectsArray = mapTileObjectArray[i].split(":");
	  			
	  			String mx = mapTileObjectsArray[0];
			    String my = mapTileObjectsArray[1];
			    String ox = mapTileObjectsArray[2];
			    String oy = mapTileObjectsArray[3];
			    String name = mapTileObjectsArray[4];
				   
			    List<HashMap<String, String>> mapObjects = mapTileObjects.get(mx+":"+my);
				   
			    if(mapObjects == null)
			    {
				    mapObjects = new ArrayList<HashMap<String, String>>();
			    }
				   
			    HashMap<String, String> mapObject = new HashMap<String, String>();
			    mapObject.put("ox", ox);
			    mapObject.put("oy", oy);
			    mapObject.put("name", name);
				   
			    mapObjects.add(mapObject);
				   
			    mapTileObjects.put(mx+":"+my, mapObjects); 
	  		}
		      
	  		return(mapTileObjects);
	   }

	   private static class OpenHelper extends SQLiteOpenHelper {
		   private Context context;

	      OpenHelper(Context context) {
	         super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    	 this.context = context;
	      }

	      @Override
	      public void onCreate(SQLiteDatabase db) {
	    	  try
	    	  {
	    		 db.execSQL("CREATE TABLE map_tiles (x integer, y integer)");
	    		 String[] mapTiles = context.getResources().getStringArray(R.array.map_tiles);
	    		 for(int i = 0; i < mapTiles.length; i++)
	    		 {
	    			 String[] mapTilesArray = mapTiles[i].split(":");
			         db.execSQL("insert into map_tiles (x, y) values ("+mapTilesArray[0]+", "+mapTilesArray[1]+")");
	    		 }
		         
		         db.execSQL("CREATE TABLE map_tile_objects (mx integer, my integer, ox integer, oy integer, name varchar(255))");
		         String[] mapTileObjects = context.getResources().getStringArray(R.array.map_tile_objects);
	    		 for(int i = 0; i < mapTileObjects.length; i++)
	    		 {
	    			 String[] mapTileObjectsArray = mapTileObjects[i].split(":");
	    			 db.execSQL("insert into map_tile_objects (mx, my, ox, oy, name) values ("+mapTileObjectsArray[0]+", "+mapTileObjectsArray[1]+", "+mapTileObjectsArray[2]+", "+mapTileObjectsArray[3]+", '"+mapTileObjectsArray[4]+"')");
	    		 }
	    		 
	    		 db.execSQL("CREATE TABLE map_tile_allow (x integer, y integer)");
	    		 db.execSQL("create unique index x_y on map_tile_allow (x, y)");
	    		 db.execSQL("insert into map_tile_allow (x, y) values (0, 0)");
	    	  }
	    	  catch(Exception e)
	    	  {
	    		 Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
	    	  }
	      }

	      @Override
	      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	      {
	    	  try
	    	  {
	    		  db.execSQL("DROP TABLE IF EXISTS map_tiles");
	    		  db.execSQL("DROP TABLE IF EXISTS map_tile_objects");
	    		  db.execSQL("DROP TABLE IF EXISTS map_tile_allow");
	    	  }
	    	  catch(Exception e)
	    	  {
	    		  Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
	    	  }
    	  
	    	  onCreate(db);
	      }
	   }
}
