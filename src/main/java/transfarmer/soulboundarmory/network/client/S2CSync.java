package transfarmer.soulboundarmory.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.SideOnly;
import transfarmer.soulboundarmory.capability.soulbound.common.ISoulbound;
import transfarmer.soulboundarmory.network.ExtendedPacketBuffer;
import transfarmer.soulboundarmory.network.IExtendedMessage;
import transfarmer.soulboundarmory.network.IExtendedMessageHandler;
import transfarmer.soulboundarmory.statistics.base.iface.ICapabilityType;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

public class S2CSync implements IExtendedMessage {
    private String capability;
    private NBTTagCompound tag;

    public S2CSync() {}

    public S2CSync(final ICapabilityType capability, final NBTTagCompound tag) {
        this.capability = capability.toString();
        this.tag = tag;
    }

    @SideOnly(CLIENT)
    public void fromBytes(final ExtendedPacketBuffer buffer) {
        this.capability = buffer.readString();
        this.tag = buffer.readCompoundTag();
    }

    public void toBytes(final ExtendedPacketBuffer buffer) {
        buffer.writeString(this.capability);
        buffer.writeCompoundTag(this.tag);
    }

    public static final class Handler implements IExtendedMessageHandler<S2CSync> {
        @SideOnly(CLIENT)
        @Override
        public IExtendedMessage onMessage(final S2CSync message, final MessageContext context) {
            final Minecraft minecraft = Minecraft.getMinecraft();

            minecraft.addScheduledTask(() -> {
                final ISoulbound capability = minecraft.player.getCapability(ICapabilityType.get(message.capability).getCapability(), null);

                capability.deserializeNBT(message.tag);
                capability.sync();
            });

            return null;
        }
    }
}