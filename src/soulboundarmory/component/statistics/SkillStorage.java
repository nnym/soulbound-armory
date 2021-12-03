package soulboundarmory.component.statistics;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import soulboundarmory.serial.CompoundSerializable;
import soulboundarmory.skill.Skill;
import soulboundarmory.skill.SkillContainer;

public class SkillStorage extends Object2ObjectLinkedOpenHashMap<Skill, SkillContainer> implements CompoundSerializable {
    public SkillStorage(Skill... skills) {
        for (var skill : skills) {
            this.put(skill, new SkillContainer(skill, this));
        }
    }

    public void add(SkillContainer skill) {
        this.put(skill.skill(), skill);
    }

    public boolean contains(Skill skill) {
        var container = this.get(skill);

        return container != null && container.learned();
    }

    public boolean contains(Skill skill, int level) {
        var container = this.get(skill);

        return container != null && container.learned() && container.level() >= level;
    }

    public void reset() {
        for (var container : this.values()) {
            if (container.hasDependencies())
                this.clear();
        }
    }

    @Override
    public void serializeNBT(NbtCompound tag) {
        for (var skill : this.values()) {
            if (skill != null) {
                tag.put(skill.skill().getRegistryName().toString(), skill.serializeNBT());
            }
        }
    }

    @Override
    public void deserializeNBT(NbtCompound tag) {
        for (var identifier : tag.getKeys()) {
            var skill = this.get(Skill.registry.getValue(new Identifier(identifier)));

            if (skill != null) {
                skill.deserializeNBT(tag.getCompound(identifier));
            }
        }
    }
}
