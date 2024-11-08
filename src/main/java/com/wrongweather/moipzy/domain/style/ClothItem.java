package com.wrongweather.moipzy.domain.style;

public class ClothItem {

    private final String thickness;
    private final String type;

    public ClothItem(String thickness, String type) {
        this.thickness = thickness;
        this.type = type;
    }

    public String getThickness() {
        return thickness;
    }

    public String getType() {
        return type;
    }
}
