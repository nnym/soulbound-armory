package transfarmer.soulboundarmory.network.server;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import transfarmer.soulboundarmory.capability.soulbound.common.ISoulbound;
import transfarmer.soulboundarmory.network.ExtendedPacketBuffer;
import transfarmer.soulboundarmory.network.IExtendedMessage;
import transfarmer.soulboundarmory.network.IExtendedMessageHandler;
import transfarmer.soulboundarmory.network.client.S2CEnchant;
import transfarmer.soulboundarmory.statistics.base.iface.ICapabilityType;
import transfarmer.soulboundarmory.statistics.base.iface.IItem;

public class C2SEnchant implements IExtendedMessage {
    private String capability;
    private String item;
    private ResourceLocation enchantment;
    private int amount;

    public C2SEnchant() {
    }

    public C2SEnchant(final ICapabilityType capability, final IItem item, final Enchantment enchantment, final int amount) {
        this.capability = capability.toString();
        this.item = item.toString();
        this.enchantment = enchantment.getRegistryName();
        this.amount = amount;
    }

    @Override
    public void fromBytes(final ExtendedPacketBuffer buffer) {
        this.capability = buffer.readString();
        this.item = buffer.readString();
        this.enchantment = buffer.readResourceLocation();
        this.amount = buffer.readInt();
    }

    @Override
    public void toBytes(final ExtendedPacketBuffer buffer) {
        buffer.writeString(this.capability);
        buffer.writeString(this.item);
        buffer.writeResourceLocation(this.enchantment);
        buffer.writeInt(this.amount);
    }

    public static final class Handler implements IExtendedMessageHandler<C2SEnchant> {
        @Override
        public IExtendedMessage onMessage(final C2SEnchant message, final MessageContext context) {
            final ICapabilityType type = ICapabilityType.get(message.capability);
            final ISoulbound instance = (ISoulbound) context.getServerHandler().player.getCapability(type.getCapability(), null);
            final Enchantment enchantment = Enchantment.getEnchantmentByLocation(message.enchantment.toString());
            final IItem weaponType = instance.getItemType(message.item);

            instance.addEnchantment(weaponType, enchantment, message.amount);

            return new S2CEnchant(type, weaponType, enchantment, message.amount);
        }
    }
}