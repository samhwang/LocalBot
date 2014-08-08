package com.example.localbot;

import android.R.color;
import android.content.Context;
import android.text.Html;
import android.widget.FrameLayout;
import android.widget.TextView;

public class TextBubble extends TextView{
	private String facebookEvent;
	private String publicEvent;
	private String friendEvent;
	
	public TextBubble(Context context, int id, String location, String distance, int x, int y) {
		super(context);
		setBackgroundResource(color.black);
		getBackground().setAlpha(200); // Transparency
		setPadding(10, 10, 10, 10);
		// Set layout
		FrameLayout.LayoutParams layoutBubbles = new  FrameLayout.LayoutParams(300,200);
    		layoutBubbles.setMargins(10, 10, 10, 10); // left, top, right, bottom
    		setLayoutParams(layoutBubbles);
    	
    	setId(id);
    	setX(x);
    	setY(y);
		setText(Html.fromHtml("<b><big>" + location + "</big></b>" +
				"<br/> <small>" + distance + "</small>"));
		
	}
	
}
