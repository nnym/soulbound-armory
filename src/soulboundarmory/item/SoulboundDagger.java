package soulboundarmory.item;

import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.item.StorageType;
import soulboundarmory.component.soulbound.item.weapon.DaggerStorage;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.entity.SoulboundDaggerEntity;
import soulboundarmory.registry.Skills;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class SoulboundDagger extends SoulboundMeleeWeapon {
    private static final int USE_TIME = 1200;

    public SoulboundDagger() {
        super(1, -2, -1);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TIME;
    }

    public float getMaxUsageRatio(float attackSpeed, int timeLeft) {
        return Math.min(attackSpeed / 2 * (USE_TIME - timeLeft) / 20F, 1);
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        var component = Components.weapon.of(player).storage(StorageType.dagger);

        if (!world.isClientSide && component.hasSkill(Skills.throwing)) {
            player.startUsingItem(hand);

            return new ActionResult<>(ActionResultType.SUCCESS, player.getItemInHand(hand));
        }

        return new ActionResult<>(ActionResultType.FAIL, player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack itemStack, World world, LivingEntity entity, int timeLeft) {
        var player = (PlayerEntity) entity;
        var component = DaggerStorage.get(player);

        if (!world.isClientSide) {
            var attackSpeed = (float) component.attributeTotal(StatisticType.attackSpeed);
            var velocity = this.getMaxUsageRatio(attackSpeed, timeLeft) * attackSpeed;
            var maxVelocity = velocity / attackSpeed;
            var dagger = new SoulboundDaggerEntity(world, entity, itemStack, component.hasSkill(Skills.shadowClone), velocity, maxVelocity);

            world.addFreshEntity(dagger);

            if (!player.isCreative()) {
                player.inventory.removeItem(itemStack);
            }
        }
    }
}