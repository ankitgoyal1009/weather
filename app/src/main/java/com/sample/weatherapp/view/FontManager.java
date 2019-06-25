package com.sample.weatherapp.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.HashMap;

public class FontManager {

    private static FontManager sFontManager;
    private static HashMap<String, Typeface> sTypeFace;
    private static AssetManager sAssetManager;

    private FontManager() {

    }

    public static FontManager getFontManager(Context context) {
        if (sFontManager == null) {
            sFontManager = new FontManager();
            sTypeFace = new HashMap<>();
            sAssetManager = context.getAssets();
        }
        return sFontManager;
    }

    public Typeface getFont(String fontName) {
        if (sTypeFace.containsKey(fontName)) {
            return sTypeFace.get(fontName);
        }
        Typeface font;
        try {
            font = Typeface.createFromAsset(sAssetManager, "fonts/" + fontName);
        } catch (Exception e) {
            //default font name in case wrong name or any other exception occurs while creating font
            font = Typeface.createFromAsset(sAssetManager, "fonts/Roboto-Regular.ttf");
        }
        sTypeFace.put(fontName, font);

        return font;
    }
}
