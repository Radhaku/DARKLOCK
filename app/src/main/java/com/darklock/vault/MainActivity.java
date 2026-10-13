package com.darklock.vault;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends Activity {

    int bg = Color.rgb(12, 10, 20);
    int purple = Color.rgb(155, 92, 255);
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(28, 30, 28, 30);
        layout.setBackgroundColor(bg);

        TextView logo = new TextView(this);
        logo.setText("DARKLOCK");
        logo.setTextSize(34);
        logo.setTextColor(Color.WHITE);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);
        layout.addView(logo);

        TextView subtitle = new TextView(this);
        subtitle.setText("YOUR PRIVATE SPACE");
        subtitle.setTextSize(12);
        subtitle.setLetterSpacing(0.22f);
        subtitle.setTextColor(purple);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
            -1, -2);
        sp.setMargins(0, 8, 0, 55);
        layout.addView(subtitle, sp);

        addButton("🔒  OPEN PRIVATE VAULT");
        addButton("＋  ADD PHOTOS & VIDEOS");

        TextView note = new TextView(this);
        note.setText("Your memories. Your control.");
        note.setTextColor(Color.rgb(155, 150, 170));
        note.setTextSize(13);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
            -1, -2);
        np.setMargins(0, 45, 0, 0);
        layout.addView(note, np);

        setContentView(layout);
    }

    void addButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(purple));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            -1, 58);
        p.setMargins(0, 10, 0, 10);
        layout.addView(button, p);
    }
}
