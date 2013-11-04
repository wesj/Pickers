package com.digdug.colorpicker;

import android.graphics.Color;
import android.util.SparseArray;

/**
 * Created by wesj on 11/3/13.
 */
public class ColorNames {
    private static float[] hsv = new float[3];

    public static String toRGB(int color) {
        return "rgb(" + Color.red(color) + ", " +
                        Color.green(color) + ", " +
                        Color.blue(color) + ")";
    }

    public static String toRGBA(int color) {
        return "rgba(" + Color.red(color) + ", " +
                Color.green(color) + ", " +
                Color.blue(color) +
                Color.alpha(color) + ")";
    }

    public static String toHSV(int color) {
        Color.colorToHSV(color, hsv);
        return "hsv(" + hsv[0] + ", " +
                        hsv[1]*100 + "%, " +
                        hsv[2]*100 + "%)";
    }

    public static String toHSVA(int color) {
        Color.colorToHSV(color, hsv);
        return "hsva(" + Color.alpha(color) + ", " +
                         hsv[0] + ", " +
                         hsv[1]*100 + "%, " +
                         hsv[2]*100 + "%)";
    }

    public static String toName(int color) {
        return namedColors.get(color);
    }

    private static float difference(float[] hsv1, float[] hsv2) {
        return (hsv1[0] - hsv2[0])*(hsv1[0] - hsv2[0]) +
               (hsv1[1] - hsv2[1])*(hsv1[1] - hsv2[1]) +
               (hsv1[2] - hsv2[2])*(hsv1[2] - hsv2[2]);
    }

    public static String roundToName(int color) {
        float[] hsv2 = new float[4];
        int found = -1;
        float previousD = Float.MAX_VALUE;
        Color.colorToHSV(color, hsv);

        for (int i = 0; i < namedColors.size(); i++) {
            int namedColor = namedColors.keyAt(i);
            Color.colorToHSV(namedColor, hsv2);

            float d = difference(hsv, hsv2);
            if (d < previousD) {
                previousD = d;
                found = i;
            }
        }

        if (found > 0) {
            return namedColors.valueAt(found);
        }

        return "";
    }

    public static int fromName(String name) {
        int index = namedColors.indexOfValue(name);
        if (index < 0)
            return -1;
        return namedColors.keyAt(index);
    }

    static public final SparseArray<String> namedColors = new SparseArray<String>();
    static {
        namedColors.put(0xFFF0F8FF, "AliceBlue");
        namedColors.put(0xFFFAEBD7, "AntiqueWhite");
        namedColors.put(0xFF00FFFF, "Aqua");
        namedColors.put(0xFF7FFFD4, "Aquamarine");
        namedColors.put(0xFFF0FFFF, "Azure");
        namedColors.put(0xFFF5F5DC, "Beige");
        namedColors.put(0xFFFFE4C4, "Bisque");
        namedColors.put(0xFF000000, "Black");
        namedColors.put(0xFFFFEBCD, "BlanchedAlmond");
        namedColors.put(0xFF0000FF, "Blue");
        namedColors.put(0xFF8A2BE2, "BlueViolet");
        namedColors.put(0xFFA52A2A, "Brown");
        namedColors.put(0xFFDEB887, "BurlyWood");
        namedColors.put(0xFF5F9EA0, "CadetBlue");
        namedColors.put(0xFF7FFF00, "Chartreuse");
        namedColors.put(0xFFD2691E, "Chocolate");
        namedColors.put(0xFFFF7F50, "Coral");
        namedColors.put(0xFF6495ED, "CornflowerBlue");
        namedColors.put(0xFFFFF8DC, "Cornsilk");
        namedColors.put(0xFFDC143C, "Crimson");
        namedColors.put(0xFF00FFFF, "Cyan");
        namedColors.put(0xFF00008B, "DarkBlue");
        namedColors.put(0xFF008B8B, "DarkCyan");
        namedColors.put(0xFFB8860B, "DarkGoldenRod");
        namedColors.put(0xFFA9A9A9, "DarkGray");
        namedColors.put(0xFF006400, "DarkGreen");
        namedColors.put(0xFFBDB76B, "DarkKhaki");
        namedColors.put(0xFF8B008B, "DarkMagenta");
        namedColors.put(0xFF556B2F, "DarkOliveGreen");
        namedColors.put(0xFFFF8C00, "Darkorange");
        namedColors.put(0xFF9932CC, "DarkOrchid");
        namedColors.put(0xFF8B0000, "DarkRed");
        namedColors.put(0xFFE9967A, "DarkSalmon");
        namedColors.put(0xFF8FBC8F, "DarkSeaGreen");
        namedColors.put(0xFF483D8B, "DarkSlateBlue");
        namedColors.put(0xFF2F4F4F, "DarkSlateGray");
        namedColors.put(0xFF00CED1, "DarkTurquoise");
        namedColors.put(0xFF9400D3, "DarkViolet");
        namedColors.put(0xFFFF1493, "DeepPink");
        namedColors.put(0xFF00BFFF, "DeepSkyBlue");
        namedColors.put(0xFF696969, "DimGray");
        namedColors.put(0xFF1E90FF, "DodgerBlue");
        namedColors.put(0xFFD19275, "Feldspar");
        namedColors.put(0xFFB22222, "FireBrick");
        namedColors.put(0xFFFFFAF0, "FloralWhite");
        namedColors.put(0xFF228B22, "ForestGreen");
        namedColors.put(0xFFFF00FF, "Fuchsia");
        namedColors.put(0xFFDCDCDC, "Gainsboro");
        namedColors.put(0xFFF8F8FF, "GhostWhite");
        namedColors.put(0xFFFFD700, "Gold");
        namedColors.put(0xFFDAA520, "GoldenRod");
        namedColors.put(0xFF808080, "Gray");
        namedColors.put(0xFF008000, "Green");
        namedColors.put(0xFFADFF2F, "GreenYellow");
        namedColors.put(0xFFF0FFF0, "HoneyDew");
        namedColors.put(0xFFFF69B4, "HotPink");
        namedColors.put(0xFFCD5C5C, "IndianRed");
        namedColors.put(0xFF4B0082, "Indigo");
        namedColors.put(0xFFFFFFF0, "Ivory");
        namedColors.put(0xFFF0E68C, "Khaki");
        namedColors.put(0xFFE6E6FA, "Lavender");
        namedColors.put(0xFFFFF0F5, "LavenderBlush");
        namedColors.put(0xFF7CFC00, "LawnGreen");
        namedColors.put(0xFFFFFACD, "LemonChiffon");
        namedColors.put(0xFFADD8E6, "LightBlue");
        namedColors.put(0xFFF08080, "LightCoral");
        namedColors.put(0xFFE0FFFF, "LightCyan");
        namedColors.put(0xFFFAFAD2, "LightGoldenRodYellow");
        namedColors.put(0xFFD3D3D3, "LightGrey");
        namedColors.put(0xFF90EE90, "LightGreen");
        namedColors.put(0xFFFFB6C1, "LightPink");
        namedColors.put(0xFFFFA07A, "LightSalmon");
        namedColors.put(0xFF20B2AA, "LightSeaGreen");
        namedColors.put(0xFF87CEFA, "LightSkyBlue");
        namedColors.put(0xFF8470FF, "LightSlateBlue");
        namedColors.put(0xFF778899, "LightSlateGray");
        namedColors.put(0xFFB0C4DE, "LightSteelBlue");
        namedColors.put(0xFFFFFFE0, "LightYellow");
        namedColors.put(0xFF00FF00, "Lime");
        namedColors.put(0xFF32CD32, "LimeGreen");
        namedColors.put(0xFFFAF0E6, "Linens");
        namedColors.put(0xFFFF00FF, "Magenta");
        namedColors.put(0xFF800000, "Maroon");
        namedColors.put(0xFF66CDAA, "MediumAquaMarine");
        namedColors.put(0xFF0000CD, "MediumBlue");
        namedColors.put(0xFFBA55D3, "MediumOrchid");
        namedColors.put(0xFF9370D8, "MediumPurple");
        namedColors.put(0xFF3CB371, "MediumSeaGreen");
        namedColors.put(0xFF7B68EE, "MediumSlateBlue");
        namedColors.put(0xFF00FA9A, "MediumSpringGreen");
        namedColors.put(0xFF48D1CC, "MediumTurquoise");
        namedColors.put(0xFFC71585, "MediumVioletRed");
        namedColors.put(0xFF191970, "MidnightBlue");
        namedColors.put(0xFFF5FFFA, "MintCream");
        namedColors.put(0xFFFFE4E1, "MistyRose");
        namedColors.put(0xFFFFE4B5, "Moccasin");
        namedColors.put(0xFFFFDEAD, "NavajoWhite");
        namedColors.put(0xFF000080, "Navy");
        namedColors.put(0xFFFDF5E6, "OldLace");
        namedColors.put(0xFF808000, "Olive");
        namedColors.put(0xFF6B8E23, "OliveDrab");
        namedColors.put(0xFFFFA500, "Orange");
        namedColors.put(0xFFFF4500, "OrangeRed");
        namedColors.put(0xFFDA70D6, "Orchid");
        namedColors.put(0xFFEEE8AA, "PaleGoldenRod");
        namedColors.put(0xFF98FB98, "PaleGreen");
        namedColors.put(0xFFAFEEEE, "PaleTurquoise");
        namedColors.put(0xFFD87093, "PaleVioletRed");
        namedColors.put(0xFFFFEFD5, "PapayaWhip");
        namedColors.put(0xFFFFDAB9, "PeachPuff");
        namedColors.put(0xFFCD853F, "Peru");
        namedColors.put(0xFFFFC0CB, "Pink");
        namedColors.put(0xFFDDA0DD, "Plum");
        namedColors.put(0xFFB0E0E6, "PowderBlue");
        namedColors.put(0xFF800080, "Purple");
        namedColors.put(0xFFFF0000, "Red");
        namedColors.put(0xFFBC8F8F, "RosyBrown");
        namedColors.put(0xFF4169E1, "RoyalBlue");
        namedColors.put(0xFF8B4513, "SaddleBrown");
        namedColors.put(0xFFFA8072, "Salmon");
        namedColors.put(0xFFF4A460, "SandyBrown");
        namedColors.put(0xFF2E8B57, "SeaGreen");
        namedColors.put(0xFFFFF5EE, "SeaShell");
        namedColors.put(0xFFA0522D, "Sienna");
        namedColors.put(0xFFC0C0C0, "Silver");
        namedColors.put(0xFF87CEEB, "SkyBlue");
        namedColors.put(0xFF6A5ACD, "SlateBlue");;
        namedColors.put(0xFF708090, "SlateGray");
        namedColors.put(0xFFFFFAFA, "Snow");
        namedColors.put(0xFF00FF7F, "SpringGreen");
        namedColors.put(0xFF4682B4, "SteelBlue");
        namedColors.put(0xFFD2B48C, "Tan");
        namedColors.put(0xFF008080, "Teal");
        namedColors.put(0xFFD8BFD8, "Thistle");
        namedColors.put(0xFFFF6347, "Tomato");
        namedColors.put(0xFF40E0D0, "Turquoise");
        namedColors.put(0xFFEE82EE, "Violet");
        namedColors.put(0xFFD02090, "VioletRed");
        namedColors.put(0xFFF5DEB3, "Wheat");
        namedColors.put(0xFFFFFFFF, "White");
        namedColors.put(0xFFF5F5F5, "WhiteSmoke");
        namedColors.put(0xFFFFFF00, "Yellow");
        namedColors.put(0xFF9ACD32, "YellowGreen");
    }
}