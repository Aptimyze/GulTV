package com.yj.wangjatv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DBManager extends SQLiteOpenHelper {
	
	Context m_context;
	
	public DBManager(Context context)
	{
		super(context, "wangjatv.db", null, 2);
		m_context = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tb_mottos (fd_id INTEGER PRIMARY KEY AUTOINCREMENT, fd_name TEXT);");       // 유저정보의 주제목록.
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tb_mottos;");

		onCreate(db);
	}
	

	
	//////////////////////////////////////////
	// DB Operation interface
	//////////////////////////////////////////

    public void setMottos(ArrayList<String> p_lstMottos) {
        SQLiteDatabase w_db = this.getWritableDatabase();

        w_db.execSQL("DELETE FROM tb_mottos;");

        for (String w_strMotto : p_lstMottos) {
            ContentValues w_newMotto = new ContentValues();
            w_newMotto.put("fd_name", w_strMotto);
            w_db.insert("tb_mottos", null, w_newMotto);
        }

        w_db.close();
    }

    public ArrayList<String> getMottos() {
        ArrayList<String> w_lstResult = new ArrayList<String>();
        SQLiteDatabase w_db = this.getReadableDatabase();

        String w_strQuery = "SELECT fd_name FROM tb_mottos ORDER BY fd_id ASC;";
        Cursor w_cursor = w_db.rawQuery(w_strQuery, null);
        while (w_cursor.moveToNext())
        {
            w_lstResult.add(w_cursor.getString(0));
        }

        w_db.close();

        return w_lstResult;
    }
}
