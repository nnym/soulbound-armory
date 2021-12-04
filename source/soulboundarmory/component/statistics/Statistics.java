package soulboundarmory.component.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import soulboundarmory.serial.CompoundSerializable;

public class Statistics extends HashMap<Category, Map<StatisticType, Statistic>> implements CompoundSerializable, Iterable<Statistic> {
    protected Statistics() {}

    public static Builder create() {
        return new Builder(new Statistics());
    }

    public Statistic get(Category category, StatisticType statistic) {
        return this.get(category).get(statistic);
    }

    public Statistic get(StatisticType type) {
        for (var category : this.values()) {
            var statistic = category.get(type);

            if (statistic != null) {
                return statistic;
            }
        }

        return null;
    }

    public void put(StatisticType type, Number value) {
        this.get(type).value(value);
    }

    public void add(StatisticType type, Number value) {
        var statistic = this.get(type);

        if (statistic != null) {
            statistic.add(value);
        }
    }

    public int size(Category category) {
        return this.get(category).size();
    }

    public void reset() {
        for (var category : this.keySet()) {
            this.reset(category);
        }
    }

    public void reset(Category category) {
        for (var statistic : this.get(category).values()) {
            statistic.reset();
        }
    }

    @Override
    public Iterator<Statistic> iterator() {
        var statistics = new HashSet<Statistic>();

        for (var category : this.values()) {
            statistics.addAll(category.values());
        }

        return statistics.iterator();
    }

    @Override
    public void serializeNBT(NbtCompound tag) {
        for (var category : this.keySet()) {
            tag.put(category.id().toString(), this.toTag(category));
        }
    }

    public NbtCompound toTag(Category category) {
        var tag = new NbtCompound();

        for (var statistic : this.get(category).values()) {
            tag.put(statistic.type().id().toString(), statistic.serializeNBT());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(NbtCompound tag) {
        for (var key : tag.getKeys()) {
            this.deserializeNBT(tag.getCompound(key), Category.registry.getValue(new Identifier(key)));
        }
    }

    public void deserializeNBT(NbtCompound tag, Category category) {
        if (category != null) {
            for (var identifier : tag.getKeys()) {
                var statistic = this.get(StatisticType.registry.getValue(new Identifier(identifier)));

                if (statistic != null) {
                    statistic.deserializeNBT(tag.getCompound(identifier));
                }
            }
        }
    }

    public static class Builder {
        protected final Statistics statistics;

        protected Builder(Statistics statistics) {
            this.statistics = statistics;
        }

        public Builder category(Category categoryType, StatisticType... statisticTypes) {
            var category = new HashMap<StatisticType, Statistic>();
            var statistics = this.statistics;

            for (var statisticType : statisticTypes) {
                category.put(statisticType, new Statistic(categoryType, statisticType));
                statistics.put(categoryType, category);
            }

            return this;
        }

        public Builder min(double min, Category category) {
            for (var statistic : this.statistics.get(category).keySet()) {
                this.min(min, statistic);
            }

            return this;
        }

        public Builder min(double min, StatisticType... types) {
            var statistics = this.statistics;

            for (var type : types) {
                statistics.get(type).min(min);
            }

            return this;
        }

        public Builder max(double max, Category category) {
            for (var type : this.statistics.get(category).keySet()) {
                this.max(max, type);
            }

            return this;
        }

        public Builder max(double max, StatisticType... types) {
            var statistics = this.statistics;

            for (var type : types) {
                statistics.get(type).max(max);
            }

            return this;
        }

        public Statistics build() {
            return this.statistics;
        }
    }
}