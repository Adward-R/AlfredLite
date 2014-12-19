package com.adward.AlfredLite.cmd;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.adward.AlfredLite.util.Unicode2Alpha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Adward on 14/12/18.
 */
public class ContactUtil {
    Context context;

    public ContactUtil(Context context){
        this.context = context;
    }

    public void reloadContactIndex(){
        SQLiteDatabase db = context.openOrCreateDatabase("database.db",0,null);

        db.execSQL("DROP TABLE IF EXISTS data");
        db.execSQL("CREATE TABLE data ( " +
                "contactName varchar(20)," +
                "contactPhoneNum varchar(20)," +
                "contactAlpha varchar(20)" +
                ")");

        db.execSQL("CREATE INDEX data1_index ON data(contactName,contactPhoneNum,contactAlpha)");

        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[] { "_id" }, null, null, null);

        while (cursor.moveToNext()) {
            int contactID = cursor.getInt(0);
            uri = Uri.parse("content://com.android.contacts/contacts/"
                    + contactID + "/data");
            Cursor cursor1 = resolver.query(uri, new String[] { "mimetype",
                    "data1" }, null, null, null);
            ContentValues cValue = new ContentValues();
            boolean isFirstNum = true;
            boolean insertFlag = true;
            String contactName = "";
            while (cursor1.moveToNext()){
                String data1 = cursor1.getString(cursor1.getColumnIndex("data1"));
                if(data1 == null) {
                    insertFlag = false;
                    break;
                }
                String mimeType = cursor1.getString(cursor1.getColumnIndex("mimetype"));
                if(mimeType.equals("vnd.android.cursor.item/name")){
                    contactName = data1;//.toLowerCase();
                    cValue.put("contactName", contactName);
                    cValue.put("contactAlpha", Unicode2Alpha.toAlpha(contactName));
                }
                if(mimeType.equals("vnd.android.cursor.item/phone_v2")) {
                    if(isFirstNum){
                        cValue.put("contactPhoneNum",data1);
                        isFirstNum = false;
                    }
                    else {
                        ContentValues newcValue = new ContentValues();
                        newcValue.put("contactName",contactName);
                        newcValue.put("contactPhoneNum",data1);
                        newcValue.put("contactAlpha", Unicode2Alpha.toAlpha(contactName));
                        db.insert("data",null,newcValue);
                    }
                }
            }
            if (insertFlag) {
                db.insert("data", null, cValue);
            }
        }
        db.close();
    }

    public List<Map<String,Object>> getUserContacts(String[] keys) {
        SQLiteDatabase db = context.openOrCreateDatabase("database.db",0,null);
        List<Map<String,Object>> contacts = new ArrayList<Map<String,Object>>();
        String totalKey = "%";
        for(int j = 1;j < keys.length;j++){
            totalKey += keys[j].toLowerCase()+"%";
        }
        Cursor cursor = db.rawQuery("SELECT contactName,contactPhoneNum,contactAlpha FROM data WHERE contactName like "
                +"'"+totalKey+"'"+" or "+ "contactPhoneNum like " +"'"
                +totalKey+"'"+" or "+ "contactAlpha like " +"'"+Unicode2Alpha.toAlpha(totalKey)+"'", null);

        if(cursor.moveToFirst()){
            for(int i = 0; i < cursor.getCount();i++){
                Map<String,Object> contact = new HashMap<String, Object>();
                contact.put("contactName", cursor.getString(cursor.getColumnIndex("contactName")));
                contact.put("contactPhoneNum", cursor.getString(cursor.getColumnIndex("contactPhoneNum")));
                contacts.add(contact);
                cursor.moveToNext();
            }
        }
        db.close();
        return contacts;
    }
}

/*public List<Map<String,Object>> getUserContacts(String[] keys) {
		Uri uri = Uri.parse("content://com.android.contacts/contacts");
		ContentResolver resolver = this.getContentResolver();
		Cursor cursor = resolver.query(uri, new String[] { "_id" }, null, null, null);
		List<Map<String,Object>> contacts = new ArrayList<Map<String,Object>>();

		while (cursor.moveToNext()) {
			Map<String,Object> contact = new HashMap<String, Object>();
			int contactID = cursor.getInt(0);
			contact.put("contactID",contactID);
			uri = Uri.parse("content://com.android.contacts/contacts/"
					+ contactID + "/data");
			Cursor cursor1 = resolver.query(uri, new String[] { "mimetype",
					"data1", "data2" }, null, null, null);
			int flag1=0,flag2=0; //indicates if the item should be put into the result list

			while (cursor1.moveToNext()){
				String data1 = cursor1.getString(cursor1.getColumnIndex("data1"));
				String mimeType = cursor1.getString(cursor1.getColumnIndex("mimetype"));

				if ("vnd.android.cursor.item/name".equals(mimeType)) { // Is name
					//Judge if the search key suits the name of contact
					if (data1==null){
						break;
					}
					for (int j=1;j<keys.length;j++){
						if (!data1.toLowerCase().contains(keys[j].toLowerCase())){
							flag1++;
							break;
						}
					}
					contact.put("contactName",data1);
				}
				else if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) { // Is Phone Number
					if (data1==null){
						break;
					}
					String phoneNum = data1.replaceAll("[- +]", "");
					for (int j=1;j<keys.length;j++){
						if (!phoneNum.contains(keys[j])){
							flag2++;
							break;
						}
					}
					//System.out.println("~~"+data1+"~~");
					contact.put("contactPhoneNum",phoneNum);
				}
			}
			if (flag1==0||flag2==0) {
				contacts.add(contact);
			}
			cursor1.close();
		}
		cursor.close();
		return contacts;
	}*/
