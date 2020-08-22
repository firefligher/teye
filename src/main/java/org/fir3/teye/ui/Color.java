package org.fir3.teye.ui;

import lombok.Data;

@Data
public class Color {
    public static final Color RED = new Color(0xFF0000FF);
    public static final Color GREEN = new Color(0x00FF00FF);
    public static final Color BLUE = new Color(0x0000FFFF);
    public static final Color WHITE = new Color(0xFFFFFFFF);
    public static final Color BLACK = new Color(0x000000FF);
    public static final Color TRANSPARENT = new Color(0xFFFFFF00);

    private final int r, g, b, a;

    public Color(int rgba) {
        this.r =  rgba >> 24;
        this.g = (rgba >> 16) & 0xFF;
        this.b = (rgba >> 8 ) & 0xFF;
        this.a =  rgba        & 0xFF;
    }
}
