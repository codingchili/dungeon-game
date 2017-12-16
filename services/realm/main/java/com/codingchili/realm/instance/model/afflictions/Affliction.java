package com.codingchili.realm.instance.model.afflictions;

import com.codingchili.realm.instance.model.entity.Entity;
import com.codingchili.realm.instance.model.spells.*;

/**
 * @author Robin Duda
 */
public class Affliction {
    protected String name = "missing name";
    protected String description = "missing description";
    protected Integer duration = 30;
    protected Integer interval = 50;
    protected Float chance = 1.0f;
    protected DamageType type = DamageType.magical;
    protected Scripted modifier;
    protected Scripted tick;

    public ActiveAffliction apply(Entity source, Entity target) {
        return new ActiveAffliction(source, target, this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public <T> T tick(Bindings bindings) {
        if (tick != null && interval > 0) {
            return tick.apply(bindings);
        }
        return null;
    }

    public <T> T apply(Bindings bindings) {
        if (modifier != null) {
            return modifier.apply(bindings);
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Float getChance() {
        return chance;
    }

    public void setChance(Float chance) {
        this.chance = chance;
    }

    public Scripted getModifier() {
        return modifier;
    }

    public void setModifier(Scripted modifier) {
        this.modifier = modifier;
    }

    public Scripted getTick() {
        return tick;
    }

    public void setTick(Scripted tick) {
        this.tick = tick;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public DamageType getType() {
        return type;
    }

    public void setType(DamageType type) {
        this.type = type;
    }
}