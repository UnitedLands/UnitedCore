package me.obito.chromiumchat.gradient;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Gradient implements Serializable {

    /**
     *
     * do not
     * touch
     * this
     * class
     */
    private static final long serialVersionUID = 1L;
    private ArrayList<Color> colors;

    public Gradient(ArrayList<String> colors) {
        this.colors = new ArrayList<Color>();
        for(String color : colors) {
            this.colors.add(Color.decode(color));
        }
    }

    public ArrayList<String> stepGradient(int steps) {
        ArrayList<String> str = new ArrayList<String>();
        int colorDistance = (int) Math.ceil(((double)steps)/((double)this.colors.size()-1));
        for(int i = 0; i < colors.size()-1; i++) {
            Color c = colors.get(i);
            Color o = colors.get(i+1);
            for(int j = 0; j < colorDistance; j++) {
                double percent = (double)j/(double)colorDistance;
                int r = colorShift(c.getRed(), o.getRed(), percent);
                int g = colorShift(c.getGreen(), o.getGreen(), percent);
                int b = colorShift(c.getBlue(), o.getBlue(), percent);
                str.add(String.format("#%02x%02x%02x", r, g, b ));
            }
        }
        return str;
    }

    public int colorShift(int from, int to, double percent) {
        int value = (int)(from > to ? from - ((from-to) * percent) : from + ((to-from) * percent));
        return  (value > 255 ? 255 : value < 0 ? 0 : value);
    }

    public String gradientMessage(String msg, String sty, boolean trim) {
        List<String> str = Arrays.asList(trim ? msg.trim().split(" ") : msg.split(" "));
        ArrayList<GradientWord> words = new ArrayList<GradientWord>();

        int size = 0;
        for(String s : str) {
            boolean addedWord = false;
            for(Player p : Bukkit.getOnlinePlayers()) {
                if(s.equals(p.getName())) {
                    words.add(new GradientWord(s, null, "&e&o", true));
                    addedWord = true;
                    break;
                }
            }
            if(s.startsWith(":") && s.endsWith(":") && !addedWord) {
                words.add(new GradientWord(s, null, "&r", true));
                addedWord = true;
            }
            if(s.startsWith("https://") && !addedWord) {
                words.add(new GradientWord(s, null, "&b&o", true));
                addedWord = true;
            }
            if(!addedWord) {
                size += s.length();
                words.add(new GradientWord(s, sty, null, false));
            }
        }

        StringBuilder formatted = new StringBuilder();
        List<String> hueShift = this.stepGradient(size);
        if(hueShift.size() == 0)
            return msg;
        int x = 0;
        for(GradientWord gw : words) {
            if(gw.isCancelled()) {
                formatted.append(ChatColor.translateAlternateColorCodes('&', String.format("%s%s " , gw.getBasicStyle(), gw.getWord())));
                continue;
            }
            String word = gw.getWord();
            for(int i = 0; i < word.length(); i++) {
                if(gw.getStyle() == null) {
                    formatted.append(String.format("%s%c", ChatColor.of(hueShift.get(x) == null ? "#FF0000" : hueShift.get(x)).toString(),
                            word.charAt(i)));
                } else {
                    formatted.append(String.format("%s%s%c", ChatColor.of(hueShift.get(x) == null ? "#FF0000" : hueShift.get(x)).toString(),
                            ChatColor.translateAlternateColorCodes('&', gw.getStyle()),
                            word.charAt(i)));
                }
                x++;
            }
            formatted.append(" ");
        }
        msg = formatted.toString();
        return msg;
    }
}
