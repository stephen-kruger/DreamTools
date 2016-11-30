package com.madibasoft.dreamtools.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/*
 * Modified from original code from Arno Den Hond, made available at http://android.arnodenhond.com/components/graphview
 */
public class GraphView extends View {

	public static boolean BAR = true;
	public static boolean LINE = false;

	private Paint paint;
	private float[] values = new float[0];
	private String[] horlabels = new String[0];
	private String[] verlabels = new String[0];
	private String title="";
	private boolean type = BAR;
	private boolean drawTextOverlay = true;


	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		paint = new Paint();
	}

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
	}

	public GraphView(Context context) {
		super(context);
		paint = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		paint.setTypeface(Typeface.SANS_SERIF);

		float border = 20;
		float horstart = border * 2;
		float height = getHeight();
		float width = getWidth() - 1;
		float max = getMax();
		float min = getMin();
		float diff = max - min;
		float graphheight = height - (2 * border);
		float graphwidth = width - (2 * border);

		paint.setTextAlign(Align.LEFT);
		int vers = verlabels.length - 1;
		for (int i = 0; i < verlabels.length; i++) {
			paint.setColor(Color.DKGRAY);
			float y = ((graphheight / vers) * i) + border;
			canvas.drawLine(horstart, y, width, y, paint);
			paint.setColor(Color.WHITE);
			canvas.drawText(verlabels[i], 0, y, paint);
		}
		int hors = horlabels.length - 1;
		for (int i = 0; i < horlabels.length; i++) {
			paint.setColor(Color.DKGRAY);
			float x = ((graphwidth / hors) * i) + horstart;
			canvas.drawLine(x, height - border, x, border, paint);
			paint.setTextAlign(Align.CENTER);
			if (i==horlabels.length-1)
				paint.setTextAlign(Align.RIGHT);
			if (i==0)
				paint.setTextAlign(Align.LEFT);
			paint.setColor(Color.WHITE);
			canvas.drawText(horlabels[i], x, height - 4, paint);
		}

		if (!drawTextOverlay) {
			paint.setTextAlign(Align.CENTER);
			canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);
		}
		
		if (max != min) {
			paint.setColor(Color.LTGRAY);
			if (type == BAR) {
				float datalength = values.length;
				float colwidth = (width - (2 * border)) / datalength;
				for (int i = 0; i < values.length; i++) {
					paint.setColor(getTrendColor(values,i));
					float val = values[i] - min;
					float rat = val / diff;
					float h = graphheight * rat;
					canvas.drawRect((i * colwidth) + horstart, (border - h) + graphheight, ((i * colwidth) + horstart) + (colwidth - 1), height - (border - 1), paint);
				}
			} 
			else {
				paint.setStrokeWidth(3);
				float datalength = values.length;
				float colwidth = (width - (2 * border)) / datalength;
				float halfcol = colwidth / 2;
				float lasth = 0;
				for (int i = 0; i < values.length; i++) {
					paint.setColor(getTrendColor(values,i));
					float val = values[i] - min;
					float rat = val / diff;
					float h = graphheight * rat;
					if (i > 0) {
						canvas.drawLine(((i - 1) * colwidth) + (horstart + 1) + halfcol, (border - lasth) + graphheight, (i * colwidth) + (horstart + 1) + halfcol, (border - h) + graphheight, paint);
					}
					lasth = h;
				}
				paint.setStrokeWidth(1);
			}
		}
		
		if (drawTextOverlay)
			drawOverlay(title, paint, horstart, graphwidth, graphheight, canvas);

	}

	private void drawOverlay(String text, Paint p, float horstart, float graphwidth, float graphheight, Canvas canvas) {
		// save values
		Typeface tf = p.getTypeface();
		float ts = p.getTextSize();
//		Style tst = p.getStyle();
//		int alpha = p.getAlpha();
		int tc = p.getColor();
		
		// now draw the big overlay
		final float GESTURE_THRESHOLD_DIP = 48.0f;
		// Convert the dips to pixels
		final float scale = getContext().getResources().getDisplayMetrics().density;
		int mGestureThreshold = (int) (GESTURE_THRESHOLD_DIP * scale + 0.5f);
		p.setTextAlign(Align.CENTER);
		p.setTypeface(Typeface.DEFAULT_BOLD);
		p.setTextSize(mGestureThreshold);
//		p.setStyle(Style.FILL_AND_STROKE);
//		p.setAlpha(128);
		p.setColor(Color.WHITE);
		p.setAntiAlias(true);
		canvas.drawText(text, (graphwidth / 2) + horstart+1, graphheight / 2 + mGestureThreshold+1, p);
		p.setColor(lighter(tc));
		canvas.drawText(text, (graphwidth / 2) + horstart-1, graphheight / 2 + mGestureThreshold-1, p);
		
		// restore values
		p.setTypeface(tf);
		p.setTextSize(ts);
//		p.setStyle(tst);
//		p.setAlpha(alpha);
	}

	/*
	 * If the trend is increasing, show green, if decreasing show red, uf static, show blue
	 */
	private int getTrendColor(float[] values, int i) {
		if (i==0)
			return Color.BLUE;
		if (values[i]>values[i-1])
			return Color.GREEN;
		if (values[i]<values[i-1])
			return Color.RED;
		return Color.BLUE;
	}
	
	private int lighter(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[0] *= 0.7f; // value component
		hsv[1] *= 0.7f; // value component
		hsv[2] *= 0.7f; // value component
		return Color.HSVToColor(hsv);
	}

	private float getMax() {
//				float largest = Integer.MIN_VALUE;
//				for (int i = 0; i < values.length; i++)
//					if (values[i] > largest)
//						largest = values[i];
//				return largest;
		return 100;
	}

	private float getMin() {
//				float smallest = Integer.MAX_VALUE;
//				for (int i = 0; i < values.length; i++)
//					if (values[i] < smallest)
//						smallest = values[i];
//				return smallest;
		return 0;
	}

	public void setData(float[] values, String title, String[] horlabels, String[] verlabels, boolean type, boolean dto) {
		if (values == null)
			values = new float[0];
		else
			this.values = values;
		if (title == null)
			title = "";
		else
			this.title = title;
		if (horlabels == null)
			this.horlabels = new String[0];
		else
			this.horlabels = horlabels;
		if (verlabels == null)
			this.verlabels = new String[0];
		else
			this.verlabels = verlabels;
		this.type = type;
		
		this.drawTextOverlay = dto;
	}

}
