package org.unitedlands.upkeep.listeners;

import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.TextComponent;
import com.palmergames.adventure.text.event.HoverEvent;
import com.palmergames.adventure.text.format.NamedTextColor;
import com.palmergames.adventure.text.format.TextDecoration;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.statusscreens.StatusScreen;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.upkeep.UnitedUpkeep;
import org.unitedlands.upkeep.calculators.TownUpkeepCalculator;

public class StatusScreenListener implements Listener {
    private final UnitedUpkeep unitedUpkeep;
    private Town town;
    private StatusScreen screen;

    public StatusScreenListener(UnitedUpkeep unitedUpkeep) {
        this.unitedUpkeep = unitedUpkeep;
    }

    @EventHandler
    public void sendStatusScreen(TownStatusScreenEvent event) {
        screen = event.getStatusScreen();
        town = event.getTown();
        replaceTownSizeComponent();
        replaceUpkeepComponent();
    }

    private TownUpkeepCalculator getTownUpkeepCalculator() {
        return new TownUpkeepCalculator(unitedUpkeep, town);
    }

    private int getTownsize() {
        return town.getTownBlocks().size();
    }

    private void replaceUpkeepComponent() {
        TextComponent upkeepComponent = getUpkeepComponent();

        if (getBonusBlockDiscount() > 0 && getNationDiscount() > 0) {
            upkeepComponent = upkeepComponent.hoverEvent(HoverEvent.showText(getComponentWithAllDiscounts()));
        } else if (getNationDiscount() > 0) {
            upkeepComponent = upkeepComponent.hoverEvent(HoverEvent.showText(getNationDiscountComponent()));
        } else if (getBonusBlockDiscount() > 0) {
            upkeepComponent = upkeepComponent.hoverEvent(HoverEvent.showText(getBonusDiscountComponent()));
        }

        screen.replaceComponent("upkeep", upkeepComponent);
    }

    private TextComponent getComponentWithAllDiscounts() {
        return Component.text("")
                .append(getNationDiscountComponent())
                .append(Component.text("\n"))
                .append(getBonusDiscountComponent());
    }

    private TextComponent getNationDiscountComponent() {
        return Component
                .text("[Nation Discount: ", NamedTextColor.DARK_GREEN)
                .append(Component.text(getNationDiscount() + " Gold", NamedTextColor.GREEN))
                .append(Component.text("]", NamedTextColor.DARK_GREEN));
    }

    private TextComponent getBonusDiscountComponent() {
        return Component
                .text("[Bonus Discount: ", NamedTextColor.DARK_GREEN)
                .append(Component.text(getBonusBlockDiscount() + " Gold", NamedTextColor.GREEN))
                .append(Component.text("]", NamedTextColor.DARK_GREEN));
    }

    private TextComponent getUpkeepComponent() {
        var calculator = getTownUpkeepCalculator();
        double upkeep = calculator.calculateTownUpkeep();
        double discountedUpkeep = calculator.getDiscountedUpkeep();
        return Component
                .text("")
                .append(Component.text("\nUpkeep: ", NamedTextColor.DARK_GREEN))
                .append(Component.text( upkeep, NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text( " " + discountedUpkeep + " Gold", NamedTextColor.RED));
    }

    private void replaceTownSizeComponent() {
        TextComponent townSizeComponent = Component
                .text("\nTown Size: ", NamedTextColor.DARK_GREEN)
                .append(Component.text(getTownsize(), NamedTextColor.GREEN));
        screen.replaceComponent("townblocks", townSizeComponent);
    }

    private double getNationDiscount() {
        var calculator = getTownUpkeepCalculator();
        return calculator.getNationDiscount();
    }

    private double getBonusBlockDiscount() {
        var calculator = getTownUpkeepCalculator();
        return calculator.calculateBonusBlockDiscount();
    }
}
