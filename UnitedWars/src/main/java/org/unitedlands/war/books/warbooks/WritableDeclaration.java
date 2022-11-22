package org.unitedlands.war.books.warbooks;

import io.github.townyadvanced.eventwar.objects.WarType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.war.UnitedWars;
import org.unitedlands.war.Utils;
import org.unitedlands.war.books.data.Declarer;
import org.unitedlands.war.books.data.WarTarget;
import org.unitedlands.war.books.warbooks.WarBook;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class WritableDeclaration implements WarBook {
    private static final FileConfiguration CONFIG = Utils.getPlugin().getConfig();

    private final Declarer declarer;
    private final WarTarget warTarget;
    private final WarType warType;
    private final ItemStack writableBook = new ItemStack(Material.WRITABLE_BOOK);
    private List<Component> reason;

    public WritableDeclaration(Declarer declarer, WarTarget warTarget, WarType warType) {
        this.declarer = declarer;
        this.warTarget = warTarget;
        this.warType = warType;
    }

    @Nullable
    private static NamespacedKey key(String key) {
        return NamespacedKey.fromString(key);
    }

    public static boolean isWritableDeclaration(PersistentDataContainer container) {
        return container.has(key("unitedwars.book.writable"));
    }

    public WarTarget getWarTarget() {
        return warTarget;
    }
    public WarType getType() {
        return warType;
    }

    public String slug() {
        return "writable-declaration";
    }

    public Declarer getDeclarer() {
        return declarer;
    }

    public ItemStack getBook() {
        addItemMeta();
        attachWarData();
        return writableBook;
    }

    private void addItemMeta() {
        ItemMeta meta = writableBook.getItemMeta();
        meta.addEnchant(Enchantment.LURE, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        Component name = getDeserializedString(CONFIG.getString("declaration-book-name"));

        meta.displayName(name);
        meta.lore(getLore());
        meta.setCustomModelData(2);
        writableBook.setItemMeta(meta);
    }

    private void attachWarData() {
        UUID townUUID = getDeclarer().town().getUUID();
        UUID targetUUID = warTarget.uuid();

        ItemMeta meta = writableBook.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key("eventwar.dow.book.town"), PersistentDataType.STRING, townUUID.toString());
        pdc.set(key("eventwar.dow.book.type"), PersistentDataType.STRING, warType.name());
        pdc.set(key("unitedwars.book.target"), PersistentDataType.STRING, targetUUID.toString());
        pdc.set(key("unitedwars.book.writable"), PersistentDataType.INTEGER, 1); // 1 = true, 0 = false

        writableBook.setItemMeta(meta);
    }

    private List<Component> getLore() {
        List<String> configuredLore = CONFIG.getStringList("writable-declaration-lore");
        List<Component> componentLore = new ArrayList<>(configuredLore.size());
        for (String line : configuredLore) {
            Component component = getDeserializedString(line);
            componentLore.add(component);
        }
        return componentLore;
    }

    @NotNull
    private Component getDeserializedString(String string) {
        return UnitedWars.MINI_MESSAGE.deserialize(
                string,
                Placeholder.component("declarer-name", text(getDeclarerName())),
                Placeholder.component("target-name", text(getTargetName()))
        );
    }

    public String getDeclarerName() {
        if (warType.isTownWar()) {
            return declarer.town().getFormattedName();
        } else if (warType.isNationWar()) {
            return declarer.nation().getFormattedName();
        }
        return declarer.player().getName();
    }

    private String getTargetName() {
        if (warType.isTownWar()) {
            return warTarget.town().getFormattedName();
        } else if (warType.isNationWar()) {
            return warTarget.nation().getFormattedName();
        }
        return warTarget.targetMayor().getName();
    }

    public WarType getWarType() {
        return warType;
    }
    public List<Component> getReason() {
        return reason;
    }

    public void setReason(List<Component> reason) {
        this.reason = reason;
    }
}