package com.sample.weatherapp.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.sample.weatherapp.weatherapp.R;

public class CustomTextView extends TextView {

    private static Typeface myTypeface;

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomTextView(Context context) {
        super(context);
        init(context, null);
    }


    private void init(Context context, AttributeSet attrs) {
        String defaultFontName = "Roboto-Regular.ttf";

        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTextView);
            if (typedArray != null) {
                String fontName = typedArray.getString(R.styleable.CustomTextView_fontName);
                if (fontName != null) {
                    defaultFontName = fontName;
                }
                typedArray.recycle();
            }
        }

        setTypeface(FontManager.getFontManager(context).getFont(defaultFontName));
    }
}
