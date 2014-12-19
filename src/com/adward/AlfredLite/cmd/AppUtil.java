package com.adward.AlfredLite.cmd;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.adward.AlfredLite.util.Unicode2Alpha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Adward on 14/12/18.
 */
public class AppUtil {

    Context context;

    public AppUtil(Context context){this.context = context;}

    public List<Map<String,Object>> getUserApps(Context context,String[] keys) {
        List<Map<String,Object>> apps = new ArrayList<Map<String,Object>>();
        PackageManager pManager = context.getPackageManager();
        //Obtain all installed app info in the cell phone
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = paklist.get(i);
            //See if the app is not pre-installed (user installed)
            if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
                // customs applications
                String pkgLabel = pManager.getApplicationLabel(pak.applicationInfo).toString();
                int flag = 0;
                for (int j=1;j<keys.length;j++){
                    boolean flag1 = pkgLabel.toLowerCase().contains(keys[j].toLowerCase());
                    boolean flag2 = Unicode2Alpha.toAlpha(pkgLabel).contains(Unicode2Alpha.toAlpha(keys[j]));
                    if (!flag1&&!flag2){
                        flag++;
                        break;
                    }
                }

                if (flag==0) {
                    Map<String, Object> listItem = new HashMap<String, Object>();
                    listItem.put("pkgName", pak.packageName);
                    listItem.put("pkgLabel", pkgLabel);
                    //listItem.put("pkgInstallTime",)
                    //listItem.put("pkgIcon",pManager.getApplicationIcon(pak));
                    listItem.put("pkgIcon", pak.applicationInfo.loadIcon(pManager));
                    apps.add(listItem);
                    //System.out.println(pManager.getApplicationLabel(paklist.get(i).applicationInfo));
                }
            }
        }
        return apps;
    }

    public void openApp(String packageName) throws PackageManager.NameNotFoundException {
        PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);

        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(pi.packageName);

        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(resolveIntent, 0);

        ResolveInfo ri = apps.iterator().next();
        if (ri != null ) {
            String pkgName = ri.activityInfo.packageName;
            String className = ri.activityInfo.name;

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            ComponentName cn = new ComponentName(pkgName, className);

            intent.setComponent(cn);
            context.startActivity(intent);
        }
    }
}
