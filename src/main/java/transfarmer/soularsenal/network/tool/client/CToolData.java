package transfarmer.soularsenal.network.tool.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.SideOnly;
import transfarmer.soularsenal.capability.tool.ISoulTool;
import transfarmer.soularsenal.capability.tool.SoulToolHelper;
import transfarmer.soularsenal.capability.tool.SoulToolProvider;
import transfarmer.soularsenal.data.tool.SoulToolType;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;
import static transfarmer.soularsenal.capability.tool.SoulToolHelper.*;

public class CToolData implements IMessage {
    private int toolIndex;
    private int currentTab;
    private int boundSlot;
    private int[][] data = new int[SOUL_TOOLS][DATA];
    private float[][] attributes = new float[SOUL_TOOLS][ATTRIBUTES];
    private int[][] enchantments = new int[SOUL_TOOLS][ENCHANTMENTS];

    public CToolData() {}

    public CToolData(final SoulToolType type, final int currentTab, final int boundSlot,
                     final int[][] data, final float[][] attributes, final int[][] enchantments) {
        if (type == null) {
            this.toolIndex = -1;
        } else {
            this.toolIndex = type.index;
        }

        this.currentTab = currentTab;
        this.boundSlot = boundSlot;

        if (SoulToolHelper.areEmpty(data, attributes, enchantments)) return;

        this.data = data;
        this.attributes = attributes;
        this.enchantments = enchantments;
    }

    public void fromBytes(ByteBuf buffer) {
        this.toolIndex = buffer.readInt();
        this.currentTab = buffer.readInt();
        this.boundSlot = buffer.readInt();

        SoulToolHelper.forEach(
            (Integer toolIndex, Integer valueIndex) -> this.data[toolIndex][valueIndex] = buffer.readInt(),
            (Integer toolIndex, Integer valueIndex) -> this.attributes[toolIndex][valueIndex] = buffer.readFloat(),
            (Integer toolIndex, Integer valueIndex) -> this.enchantments[toolIndex][valueIndex] = buffer.readInt()
        );
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.toolIndex);
        buffer.writeInt(this.currentTab);
        buffer.writeInt(this.boundSlot);

        SoulToolHelper.forEach(
            (Integer toolIndex, Integer valueIndex) -> buffer.writeInt(this.data[toolIndex][valueIndex]),
            (Integer toolIndex, Integer valueIndex) -> buffer.writeFloat(this.attributes[toolIndex][valueIndex]),
            (Integer toolIndex, Integer valueIndex) -> buffer.writeInt(this.enchantments[toolIndex][valueIndex])
        );
    }

    public static final class Handler implements IMessageHandler<CToolData, IMessage> {
        @SideOnly(CLIENT)
        @Override
        public IMessage onMessage(CToolData message, MessageContext context) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final ISoulTool instance = SoulToolProvider.get(Minecraft.getMinecraft().player);

                instance.setCurrentType(message.toolIndex);
                instance.setCurrentTab(message.currentTab);
                instance.setBoundSlot(message.boundSlot);
                instance.setStatistics(message.data, message.attributes, message.enchantments);
            });

            return null;
        }
    }
}
