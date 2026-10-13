package com.darklock.vault;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.graphics.BitmapFactory;
import android.view.*;
import android.widget.*;
import android.database.Cursor;
import android.provider.OpenableColumns;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, list;
    final int PURPLE = 0xff9b5cff, BG = 0xff0c0a14;
    File vault;
    int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        vault = new File(getFilesDir(), "vault");
        if (!vault.exists()) vault.mkdirs();
        if (getPreferences(0).getString("pin", "").isEmpty()) pinDialog(true);
        else pinDialog(false);
    }

    void pinDialog(boolean setup) {
        EditText input = new EditText(this);
        input.setHint(setup ? "Create a 4+ digit PIN" : "Enter your PIN");
        input.setInputType(2);
        new AlertDialog.Builder(this).setTitle(setup ? "Set DARKLOCK PIN" : "Unlock DARKLOCK")
            .setView(input).setCancelable(false)
            .setPositiveButton(setup ? "Save PIN" : "Unlock", (d,w) -> {
                String p = input.getText().toString();
                if (setup) {
                    if (p.length() < 4) { Toast.makeText(this,"PIN must be at least 4 digits",Toast.LENGTH_LONG).show(); pinDialog(true); }
                    else { getPreferences(0).edit().putString("pin",p).apply(); home(); }
                } else if (p.equals(getPreferences(0).getString("pin", ""))) home();
                else { Toast.makeText(this,"Wrong PIN",Toast.LENGTH_SHORT).show(); pinDialog(false); }
            }).show();
    }

    TextView text(String s, int size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); return t;
    }
    Button button(String s, Runnable r) {
        Button b = new Button(this); b.setText(s); b.setAllCaps(false); b.setTextColor(-1);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(PURPLE));
        b.setOnClickListener(v -> r.run()); return b;
    }
    void home() {
        root = new LinearLayout(this); root.setOrientation(1); root.setPadding(dp(20),dp(28),dp(20),dp(20)); root.setBackgroundColor(BG);
        TextView logo = text("DARKLOCK",32,-1); logo.setTypeface(null,1); logo.setGravity(17); root.addView(logo);
        TextView sub = text("YOUR PRIVATE SPACE",13,PURPLE); sub.setGravity(17); root.addView(sub);
        root.addView(button("＋  Add photos or videos", () -> picker()));
        TextView heading = text("YOUR VAULT",18,-1); heading.setPadding(0,dp(24),0,dp(8)); root.addView(heading);
        ScrollView scroll = new ScrollView(this); list = new LinearLayout(this); list.setOrientation(1); scroll.addView(list); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(button("Lock DARKLOCK", () -> pinDialog(false)));
        setContentView(root); refresh();
    }

    void picker() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","video/*"});
        startActivityForResult(i,101);
    }
    @Override protected void onActivityResult(int req,int result,Intent data) {
        super.onActivityResult(req,result,data);
        if(req==101 && result==RESULT_OK && data!=null && data.getData()!=null) {
            Uri uri=data.getData();
            try {
                String name="media_"+System.currentTimeMillis();
                String mime=getContentResolver().getType(uri);
                String ext=(mime!=null && mime.startsWith("video/"))?".mp4":".img";
                Cursor c=getContentResolver().query(uri,null,null,null,null);
                if(c!=null) { int ix=c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if(c.moveToFirst() && ix>=0) { String n=c.getString(ix); if(n!=null && n.contains(".")) ext=n.substring(n.lastIndexOf('.')); } c.close(); }
                File out=new File(vault,name+ext);
                try(InputStream in=getContentResolver().openInputStream(uri); OutputStream os=new FileOutputStream(out)) {
                    if(in==null) throw new IOException("Cannot read selected file");
                    byte[] buf=new byte[8192]; int n; while((n=in.read(buf))>0) os.write(buf,0,n);
                }
                Toast.makeText(this,"Saved inside DARKLOCK",Toast.LENGTH_SHORT).show(); refresh();
            } catch(Exception e) { Toast.makeText(this,"Could not save file: "+e.getMessage(),Toast.LENGTH_LONG).show(); }
        }
    }

    void refresh() {
        list.removeAllViews(); File[] files=vault.listFiles();
        if(files==null || files.length==0) { list.addView(text("Your vault is empty. Add photos or videos above.",15,0xffaaa5b7)); return; }
        Arrays.sort(files,(a,b)->Long.compare(b.lastModified(),a.lastModified()));
        for(File f:files) {
            LinearLayout row=new LinearLayout(this); row.setOrientation(1); row.setPadding(dp(12),dp(8),dp(12),dp(8));
            row.setBackgroundColor(0xff201a2c);
            row.addView(text(f.getName(),14,-1));
            row.addView(text(String.format(java.util.Locale.US,"%.1f KB",f.length()/1024.0),12,0xffaaa5b7));
            LinearLayout actions=new LinearLayout(this); actions.setOrientation(0);
            Button view=button("View / Play",()->openFile(f));
            Button del=button("Delete",()->new AlertDialog.Builder(this).setTitle("Delete from Vault?").setMessage("This removes the saved copy from DARKLOCK.").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{f.delete();refresh();}).show());
            actions.addView(view,new LinearLayout.LayoutParams(0,dp(48),1)); actions.addView(del,new LinearLayout.LayoutParams(0,dp(48),1)); row.addView(actions);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(5),0,dp(5)); list.addView(row,p);
        }
    }

    void openFile(File f) {
        if(f.getName().toLowerCase().endsWith(".mp4") || f.getName().toLowerCase().endsWith(".mp3") || f.getName().toLowerCase().endsWith(".3gp") || f.getName().toLowerCase().endsWith(".mkv")) {
            VideoView v=new VideoView(this); v.setVideoPath(f.getAbsolutePath());
            new AlertDialog.Builder(this).setTitle("DARKLOCK Video").setView(v).setPositiveButton("Close",(d,w)->v.stopPlayback()).show(); v.start();
        } else {
            android.graphics.Bitmap bm=BitmapFactory.decodeFile(f.getAbsolutePath());
            if(bm==null) { Toast.makeText(this,"This image format cannot be previewed",Toast.LENGTH_SHORT).show(); return; }
            ImageView image=new ImageView(this); image.setImageBitmap(bm); image.setAdjustViewBounds(true); image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            new AlertDialog.Builder(this).setTitle("DARKLOCK Photo").setView(image).setPositiveButton("Close",null).show();
        }
    }
}
