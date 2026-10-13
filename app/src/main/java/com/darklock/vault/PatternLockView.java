package com.darklock.vault;

import android.view.View;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Color;
import android.content.Context;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;

public class PatternLockView extends View {
    private final Paint paint = new Paint(3);
    private final List<Integer> selected = new ArrayList<>();
    private float spacing, radius;
    private int active = -1;

    public interface Listener {
        void onPatternComplete(String pattern);
    }

    private Listener listener;

    public PatternLockView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setListener(Listener l) {
        listener = l;
    }

    private float cx(int i) {
        return getWidth() / 2f + (i % 3 - 1) * spacing;
    }

    private float cy(int i) {
        return getHeight() / 2f + (i / 3 - 1) * spacing;
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        spacing = Math.min(getWidth(), getHeight()) / 3.2f;
        radius = spacing * 0.12f;

        paint.setColor(Color.rgb(155, 92, 255));
        paint.setStrokeWidth(radius * 0.45f);
        paint.setStyle(Paint.Style.STROKE);

        if (selected.size() > 1) {
            Path p = new Path();
            p.moveTo(cx(selected.get(0)), cy(selected.get(0)));
            for (int i = 1; i < selected.size(); i++)
                p.lineTo(cx(selected.get(i)), cy(selected.get(i)));
            c.drawPath(p, paint);
        }

        for (int i = 0; i < 9; i++) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(selected.contains(i)
                ? Color.rgb(155, 92, 255) : Color.LTGRAY);
            c.drawCircle(cx(i), cy(i), radius, paint);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY();

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            selected.clear();
            active = -1;
            addDot(x, y);
            invalidate();
            return true;
        }

        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            addDot(x, y);
            invalidate();
            return true;
        }

        if (e.getAction() == MotionEvent.ACTION_UP) {
            addDot(x, y);
            StringBuilder result = new StringBuilder();
            for (int i : selected) result.append(i);
            if (listener != null) listener.onPatternComplete(result.toString());
            selected.clear();
            active = -1;
            invalidate();
            return true;
        }
        return true;
    }

    private void addDot(float x, float y) {
        if (spacing <= 0) return;
        for (int i = 0; i < 9; i++) {
            float dx = x - cx(i), dy = y - cy(i);
            if (dx * dx + dy * dy <= spacing * spacing * 0.20f
                    && !selected.contains(i)) {
                selected.add(i);
                active = i;
                break;
            }
        }
    }
}
