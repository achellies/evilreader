/**
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * Dainius Jocas <dainius(dot)jocas(at)gmail(dot)com> wrote this file. 
 * As long as you retain this notice you can do whatever you want with this 
 * stuff. If we meet some day, and you think this stuff is worth it, you can 
 * buy me a beer in return.
 * ----------------------------------------------------------------------------
 */

package com.evilreader.android.dbcontroller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class is dedicated to control EvilReader communication with database. 
 * @author Dainius Jocas
 *
 */
public class DBAdapter {
	/*
	 * Names and definitions for the EvilReader DB.
	 */
	public static final String DATABASE_NAME = "evilreaderdb";
	public static final int DATABASE_VERSION = 1;
	
	/*
	 * Name definitions for Notes table. For only one table for now.
	 */
	public static final String NOTE_TABLE_TITLE = "note";
	public static final String NOTE_ROWID = "rowid";
	public static final String NOTE_BODY  = "body";
	public static final String NOTE_BOOK_ID = "book_id";
	public static final String NOTE_LOCATION_ID = "location_id";
	private static final String DATABASE_CREATE_NOTES =
	        "create table " 
	        + NOTE_TABLE_TITLE 
	        + " ( "
	        + NOTE_ROWID
	        + " integer primary key autoincrement, "
	        + NOTE_BODY
	        + " text not null, "
	        + NOTE_BOOK_ID 
	        + " text not null, "
	        + NOTE_LOCATION_ID
	        + " text not null);";
	
	/*
	 * Possible definition for a LOCATION table.
	 * 
	 * LOCATION_FIRST_WORD_NUMBER caption the exact location of an object
	 * related with an ebook. For example, I highlighted two words in a book, 
	 * so, my highlight started at the 5th word of the specific paragraph and
	 * length of selection was two words.
	 * 
	 * Other columns in my opinion are self explainable
	 */
	public static final String LOCATION_TABLE_TITLE = "location";
	// what if object will be deleted? Nothing reader will not take the record
	// into consideration.
	public static final String LOCATION_ROWID = "rowid";
	public static final String LOCATION_BOOK_ID = "book_id";
	public static final String LOCATION_CHAPTER_NUMBER = "chapter_number";
	public static final String LOCATION_PARAGRAPH_NUMBER = "paragraph_number";
	public static final String LOCATION_FIRST_WORD_NUMBER = "first_word_number";
	// LENGTH_OF_SELECTION is in words because it may span multiple paragraphs
	public static final String LOCATION_LENGTH_OF_SELECTION = "length_of_selection";
	private static final String DATABASE_CREATE_LOCATION = 
			"create table "
			+ LOCATION_TABLE_TITLE
			+ " ( "
			+ LOCATION_ROWID
			+ " integer primary key autoincrement, "
			+ LOCATION_BOOK_ID
			+ " text not null, "
			+ LOCATION_CHAPTER_NUMBER
			+ " text not null, "
			+ LOCATION_PARAGRAPH_NUMBER
			+ " text not null, "
			+ LOCATION_FIRST_WORD_NUMBER
			+ " text not null, "
			+ LOCATION_LENGTH_OF_SELECTION
			+ " text not null);";
	
	
	//TODO(dainius): describe all the tables;
	//TODO(dainius): add code for all the table handlers. 
	
	/**
     * Database creation sql statement. For now its just one table NOTES!
     * TODO(dainius): rewrite for multitable case;
     */
    private static final String DATABASE_CREATE =
        "create table " 
        + NOTE_TABLE_TITLE 
        + " ( "
        + NOTE_ROWID
        + " integer primary key autoincrement, "
        + NOTE_BODY
        + " text not null, "
        + NOTE_BOOK_ID 
        + " text not null);";
    
    
	
	private static final String TAG = "DBAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private final Context mContext;
	
	/**
	 * Constructor - takes the context to allow database to be created or 
	 *   opened.
	 *   
	 *   @param context within which our DB should work
	 */
	public DBAdapter(Context context) {
		this.mContext = context;
	}

	/*
	 * Method for opening DB. If DB cannot be created then SQLException will
	 * be thrown.
	 * 
	 * @return this (self reference)
	 * @throws SQLException if DB cannot be neither opened nor created
	 */
	public DBAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	/*
     * Releasing resourses related to DB connection.
     * TODO(dainius): when should this method be used?
     */
	public void close() {
		mDbHelper.close();
	}

	/**
	 * We'll see what we can do with this static class.
	 * In documentation it is written that SQLiteOpenHelper class usually is 
	 * use to manage database creation and version management. 
	 * @author dainius
	 *
	 */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + NOTE_TABLE_TITLE);
            onCreate(db);
        }
    }
    
    /************************************************************************** 
     * This series of functions is dedicated to work with NOTES table
     *************************************************************************/
    
    /**
     * Create one entry in the DB table NOTES when body of note and bookId of
     * the eBook to which note is assigned are provided. Is success then rowId 
     * is returned, otherwise -1.
     *  
     * @param body - actual content of the note
     * @param bookId - to which book note is assigned
     * @return rowId or -1 if failed
     */
    public long storeNote(String body, String bookId, String location_id) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(NOTE_BODY, body);
    	initialValues.put(NOTE_BOOK_ID, bookId);
    	initialValues.put(NOTE_LOCATION_ID, location_id);
    	return mDb.insert(NOTE_TABLE_TITLE, null, initialValues);
    }
    
    /**
     * Delete a note with a given note ID
     * @param rowId - id of the note that should be deleted
     * @return true if success, otherwise - false
     */
    public boolean deleteNote(long rowId) {
    	return mDb.delete(NOTE_TABLE_TITLE, NOTE_BOOK_ID + "=" + rowId,
    			null) > 0;
    }
    
    /**
     * Get all notes for a specific book.
     * @return cursor over all selected notes for a specific book.
     */
    public Cursor fetchAllNotes(long bookId) {
    	Cursor cursor = mDb.query(NOTE_TABLE_TITLE,
    			new String[] {NOTE_ROWID, NOTE_BODY, NOTE_LOCATION_ID},
    			NOTE_BOOK_ID + "=" + bookId, null, null, null,
    			null);
    	return cursor;
    }
    
    /**
     * Return a cursor positioned at the note that matches the given noteId.
     * 
     * @param noteId
     * @return cursor positioned to a matching note
     * @throws SQLException if note cannot be found/retrieved
     */
    public Cursor fetchNote(long noteId) throws SQLException {
    	Cursor mCursor = mDb.query(NOTE_TABLE_TITLE, 
    			new String[] {NOTE_ROWID, NOTE_BODY},
    			NOTE_ROWID + "=" + noteId, null, null, null, null);
    	if (mCursor != null) {
    		mCursor.moveToFirst();
    	}
    	return mCursor;
    }
    
    /**
     * Updates specific note with specific content. If success the returns true,
     * otherwise false
     * 
     * @param noteId
     * @param body - new content of the note
     * @return isSuccess - true when update was successful, otherwise false
     */
    public boolean updateNote(long noteId, String body) {
    	ContentValues content_values = new ContentValues();
    	content_values.put(NOTE_BODY, body);
    	boolean isSuccess = mDb.update(NOTE_TABLE_TITLE, content_values,
    			NOTE_ROWID + "=" + noteId, null) > 0;
    	return isSuccess;
    }
    /*************************************************************************/
    
    /**************************************************************************
     * TODO(dainius) create controllers for LOCATIONS table.
     *************************************************************************/
    
    /**
     * �
     * @param book_id
     * @param chapterNumber
     * @param paragraphNumber
     * @param firstWordNumber
     * @param lengthOfSelection
     * @return
     */
    public long storeLocation(String book_id, String chapterNumber, 
    		String paragraphNumber, String firstWordNumber,
    		String lengthOfSelection) {
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(LOCATION_BOOK_ID, book_id);
    	contentValues.put(LOCATION_CHAPTER_NUMBER, chapterNumber);
    	contentValues.put(LOCATION_PARAGRAPH_NUMBER, paragraphNumber);
    	contentValues.put(LOCATION_FIRST_WORD_NUMBER, firstWordNumber);
    	contentValues.put(LOCATION_LENGTH_OF_SELECTION, lengthOfSelection);
    	return mDb.insert(LOCATION_TABLE_TITLE, null, contentValues);
    }
}