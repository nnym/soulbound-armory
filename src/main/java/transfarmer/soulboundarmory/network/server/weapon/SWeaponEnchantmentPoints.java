package transfarmer.soulboundarmory.network.server.weapon;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import transfarmer.soulboundarmory.capability.weapon.ISoulWeapon;
import transfarmer.soulboundarmory.capability.weapon.SoulWeaponProvider;
import transfarmer.soulboundarmory.data.IEnchantment;
import transfarmer.soulboundarmory.data.IType;
import transfarmer.soulboundarmory.data.weapon.SoulWeaponEnchantment;
import transfarmer.soulboundarmory.data.weapon.SoulWeaponType;
import transfarmer.soulboundarmory.network.client.weapon.CWeaponSpendEnchantmentPoints;

public class SWeaponEnchantmentPoints implements IMessage {
    private int amount;
    private int enchantmentIndex;
    private int typeIndex;

    public SWeaponEnchantmentPoints() {}

    public SWeaponEnchantmentPoints(final int amount, final IEnchantment enchantment, final IType type) {
        this.amount = amount;
        this.enchantmentIndex = enchantment.getIndex();
        this.typeIndex = type.getIndex();
    }

    @Override
    public void fromBytes(final ByteBuf buffer) {
        this.amount = buffer.readInt();
        this.enchantmentIndex = buffer.readInt();
        this.typeIndex = buffer.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buffer) {
        buffer.writeInt(this.amount);
        buffer.writeInt(this.enchantmentIndex);
        buffer.writeInt(this.typeIndex);
    }

    public static final class Handler implements IMessageHandler<SWeaponEnchantmentPoints, IMessage> {
        @Override
        public IMessage onMessage(final SWeaponEnchantmentPoints message, final MessageContext context) {
            final ISoulWeapon instance = SoulWeaponProvider.get(context.getServerHandler().player);
            final IEnchantment enchantment = SoulWeaponEnchantment.getEnchantment(message.enchantmentIndex);
            final IType weaponType = SoulWeaponType.getType(message.typeIndex);

            instance.addEnchantment(message.amount, enchantment, weaponType);

            return new CWeaponSpendEnchantmentPoints(message.amount, enchantment, weaponType);
        }
    }
}