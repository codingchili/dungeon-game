package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.model.items.Inventory;

import com.codingchili.core.configuration.Attributes;
import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * model for player characters.
 */
public class PlayerCharacter extends Attributes implements Storable {
    private String account;
    private String name;
    private String className;
    private Inventory inventory;

    public PlayerCharacter() {
    }

    public PlayerCharacter(PlayerCharacter template, String name, String className) {
        this.name = name;
        this.className = className;
        this.attributes = template.attributes;
        this.inventory = template.inventory;
    }

    public String getClassName() {
        return className;
    }

    public PlayerCharacter setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getName() {
        return name;
    }

    public PlayerCharacter setName(String name) {
        this.name = name;
        return this;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String getAccount() {
        return account;
    }

    public PlayerCharacter setAccount(String account) {
        this.account = account;
        return this;
    }

    @Override
    public String getId() {
        return name;
    }
}