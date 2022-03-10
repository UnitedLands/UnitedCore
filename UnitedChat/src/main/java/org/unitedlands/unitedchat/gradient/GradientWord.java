package org.unitedlands.unitedchat.gradient;

public class GradientWord {

    private String word;
    private String style;
    private String basicStyle;
    private boolean isCancelled;

    public GradientWord(String word, String style, String basicStyle, boolean isCancelled) {
        this.word = word;
        this.style = style;
        this.basicStyle = basicStyle;
        this.isCancelled = isCancelled;
    }

    public String getWord() {
        return word;
    }

    public String getStyle() {
        return style;
    }

    public String getBasicStyle() {
        return basicStyle;
    }

    public boolean isCancelled() {
        return isCancelled;
    }
}
