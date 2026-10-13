
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
import java.security.MessageDigest;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, list;
    final int PURPLE=0xff9b5cff, BG=0xff0c0a14;
    File vault;
    android.content.SharedPreferences prefs;

    int dp(float n) {
        return (int)(n*getResources().getDisplayMetrics().density+0.5f);
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        prefs=getSharedPreferences("darklock",0);
        vault=new File(getFilesDir(),"vault");
        if(!vault.exists()) vault.mkdirs();
        if(prefs.getString("lock_type","").isEmpty()) chooseLock(true);
        else unlock();
    }

    void chooseLock(boolean setup) {
        String[] choices={"PIN","Pattern"};
        new AlertDialog.Builder(this)
            .setTitle(setup?"Choose DARKLOCK security":"Change lock type")
            .setItems(choices,(d,which)->{
                if(which==0) pinDialog(setup);
                else patternDialog(setup);
            })
            .setCancelable(false).show();
    }

    String hash(String value) {
        try {
            byte[] b=MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"));
            StringBuilder s=new StringBuilder();
            for(byte x:b) s.append(String.format(Locale.US,"%02x",x&255));
            return s.toString();
        } catch(Exception e) { return value; }
    }

    void pinDialog(boolean setup) {
        EditText input=new EditText(this);
        input.setSingleLine(true);
        input.setInputType(2);
        input.setHint(setup?"Create a 4+ digit PIN":"Enter PIN");
        new AlertDialog.Builder(this)
            .setTitle(setup?"Set PIN":"Unlock DARKLOCK")
            .setView(input).setCancelable(false)
            .setPositiveButton(setup?"Save PIN":"Unlock",(d,w)->{
                String p=input.getText().toString();
                if(setup) {
                    if(p.length()<4) {
                        Toast.makeText(this,"Use at least 4 digits",Toast.LENGTH_LONG).show();
                        pinDialog(true); return;
                    }
                    prefs.edit().putString("lock_type","PIN")
                        .putString("lock_hash",hash(p)).apply();
                    home();
                } else if(hash(p).equals(prefs.getString("lock_hash",""))) home();
                else {
                    Toast.makeText(this,"Wrong PIN",Toast.LENGTH_SHORT).show();
                    pinDialog(false);
                }
            })
            .setNegativeButton(setup?"Back":"Cancel",(d,w)->{
                if(setup) chooseLock(true);
            }).show();
    }

    void patternDialog(boolean setup) {
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(1);
        box.setPadding(dp(12),dp(8),dp(12),dp(8));
        TextView info=text(setup?"Draw a pattern using at least 4 dots":"Draw your unlock pattern",16,-1);
        box.addView(info);
        PatternLockView pattern=new PatternLockView(this);
        box.addView(pattern,new LinearLayout.LayoutParams(-1,dp(280)));
        final String[] drawn={""};
        pattern.setListener(p->{
            drawn[0]=p;
            if(p.length()<4) info.setText("Use at least 4 dots. Try again.");
            else info.setText("Pattern selected: "+p.length()+" dots");
        });
        new AlertDialog.Builder(this)
            .setTitle(setup?"Set Pattern":"Unlock DARKLOCK")
            .setView(box).setCancelable(false)
            .setPositiveButton(setup?"Save Pattern":"Unlock",(d,w)->{
                String p=drawn[0];
                if(setup) {
                    if(p.length()<4) {
                        Toast.makeText(this,"Draw at least 4 dots",Toast.LENGTH_LONG).show();
                        patternDialog(true); return;
                    }
                    prefs.edit().putString("lock_type","Pattern")
                        .putString("lock_hash",hash(p)).apply();
                    home();
                } else if(hash(p).equals(prefs.getString("lock_hash",""))) home();
                else {
                    Toast.makeText(this,"Wrong pattern",Toast.LENGTH_SHORT).show();
                    patternDialog(false);
                }
            })
            .setNegativeButton("Back",(d,w)->{
                if(setup) chooseLock(true);
            }).show();
    }

    void unlock() {
        if("Pattern".equals(prefs.getString("lock_type",""))) patternDialog(false);
        else pinDialog(false);
    }

    TextView text(String s,int size,int color) {
        TextView t=new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        return t;
    }

    Button button(String s,Runnable r) {
        Button b=new Button(this); b.setText(s); b.setAllCaps(false);
        b.setTextColor(-1);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(PURPLE));
        b.setOnClickListener(v->r.run()); return b;
    }

    void home() {
        root=new LinearLayout(this); root.setOrientation(1);
        root.setPadding(dp(20),dp(28),dp(20),dp(20));
        root.setBackgroundColor(BG);
        TextView logo=text("🔒 DARKLOCK",30,-1);
        logo.setTypeface(null,1); logo.setGravity(17); root.addView(logo);
        TextView sub=text("YOUR PRIVATE SPACE",13,PURPLE);
        sub.setGravity(17); root.addView(sub);
        root.addView(button("＋ Add photos or videos",()->picker()));
        root.addView(button("Change PIN / Pattern",()->chooseLock(false)));
        TextView heading=text("YOUR VAULT",18,-1);
        heading.setPadding(0,dp(24),0,dp(8)); root.addView(heading);
        ScrollView scroll=new ScrollView(this);
        list=new LinearLayout(this); list.setOrientation(1);
        scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(button("Lock DARKLOCK",()->{
            if("Pattern".equals(prefs.getString("lock_type",""))) patternDialog(false);
            else pinDialog(false);
        }));
        setContentView(root); refresh();
    }

    void picker() {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
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
                if(c!=null) {
                    int ix=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if(c.moveToFirst() && ix>=0) {
                        String n=c.getString(ix);
                        if(n!=null && n.contains(".")) ext=n.substring(n.lastIndexOf('.'));
                    }
                    c.close();
                }
                File out=new File(vault,name+ext);
                try(InputStream in=getContentResolver().openInputStream(uri);
                    OutputStream os=new FileOutputStream(out)) {
                    if(in==null) throw new IOException("Cannot read selected file");
                    byte[] buf=new byte[8192]; int n;
                    while((n=in.read(buf))>0) os.write(buf,0,n);
                }
                Toast.makeText(this,"Saved inside DARKLOCK",Toast.LENGTH_SHORT).show();
                refresh();
            } catch(Exception e) {
                Toast.makeText(this,"Could not save: "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    void refresh() {
        list.removeAllViews();
        File[] files=vault.listFiles();
        if(files==null || files.length==0) {
            list.addView(text("Your vault is empty. Add photos or videos above.",15,0xffaaa5b7));
            return;
        }
        Arrays.sort(files,(a,b)->Long.compare(b.lastModified(),a.lastModified()));
        for(File f:files) {
            LinearLayout row=new LinearLayout(this); row.setOrientation(1);
            row.setPadding(dp(12),dp(8),dp(12),dp(8));
            row.setBackgroundColor(0xff201a2c);
            row.addView(text(f.getName(),14,-1));
            row.addView(text(String.format(Locale.US,"%.1f KB",f.length()/1024.0),12,0xffaaa5b7));
            LinearLayout actions=new LinearLayout(this);
            actions.setOrientation(0);
            actions.addView(button("View / Play",()->openFile(f)),
                new LinearLayout.LayoutParams(0,dp(48),1));
            actions.addView(button("Delete",()->new AlertDialog.Builder(this)
                .setTitle("Delete from Vault?")
                .setMessage("This removes the saved copy from DARKLOCK.")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Delete",(d,w)->{f.delete();refresh();})
                .show()),new LinearLayout.LayoutParams(0,dp(48),1));
            row.addView(actions);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
            p.setMargins(0,dp(5),0,dp(5)); list.addView(row,p);
        }
    }

    void openFile(File f) {
        String n=f.getName().toLowerCase(Locale.US);
        if(n.endsWith(".mp4")||n.endsWith(".mp3")||n.endsWith(".3gp")||n.endsWith(".mkv")||n.endsWith(".webm")) {
            VideoView v=new VideoView(this); v.setVideoPath(f.getAbsolutePath());
            new AlertDialog.Builder(this).setTitle("DARKLOCK Video")
                .setView(v).setPositiveButton("Close",(d,w)->v.stopPlayback()).show();
            v.start();
        } else {
            android.graphics.Bitmap bm=BitmapFactory.decodeFile(f.getAbsolutePath());
            if(bm==null) {
                Toast.makeText(this,"Cannot preview this image format",Toast.LENGTH_SHORT).show(); return;
            }
            ImageView image=new ImageView(this); image.setImageBitmap(bm);
            image.setAdjustViewBounds(true); image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            new AlertDialog.Builder(this).setTitle("DARKLOCK Photo")
                .setView(image).setPositiveButton("Close",null).show();
        }
    }

    @Override public void onBackPressed() {
        if(root!=null && root.getParent()!=null) super.onBackPressed();
        else {
            if("Pattern".equals(prefs.getString("lock_type",""))) patternDialog(false);
            else pinDialog(false);
        }
    }
}
