package net.auoeke.soulboundarmory.event;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import net.auoeke.cell.client.gui.CellElement;
import net.auoeke.soulboundarmory.SoulboundArmory;
import net.auoeke.soulboundarmory.SoulboundArmoryClient;
import net.auoeke.soulboundarmory.capability.Capabilities;
import net.auoeke.soulboundarmory.capability.soulbound.item.ItemStorage;
import net.auoeke.soulboundarmory.capability.soulbound.item.weapon.StaffStorage;
import net.auoeke.soulboundarmory.client.gui.bar.ExperienceBarOverlay;
import net.auoeke.soulboundarmory.config.Configuration;
import net.auoeke.soulboundarmory.item.SoulboundItem;
import net.auoeke.soulboundarmory.text.Translation;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = SoulboundArmory.ID)
public class ClientEvents {
    public static ExperienceBarOverlay overlayBar;
    public static ExperienceBarOverlay tooltipBar;

    @SubscribeEvent
    public static void scroll(InputEvent.MouseScrollEvent event) {
        if (Screen.hasAltDown()) {
            PlayerEntity player = SoulboundArmoryClient.client.player;

            if (player != null && player.level != null) {
                var storage = Capabilities.weapon.get(player).get().heldItemStorage();

                if (storage instanceof StaffStorage) {
                    var dY = (int) event.getMouseY();

                    if (dY != 0) {
                        var staffStorage = (StaffStorage) storage;

                        staffStorage.cycleSpells(-dY);
                        SoulboundArmoryClient.client.gui.setOverlayMessage(new Translation("§4§l%s", staffStorage.spell()), false);

                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE && Configuration.instance().client.overlayExperienceBar && overlayBar.render(event.getMatrixStack(), event.getWindow())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var player = event.getPlayer();

        if (player != null) {
            var itemStack = event.getItemStack();
            var item = itemStack.getItem();

            ItemStorage.get(player, item).ifPresent(storage -> {
                var tooltip = event.getToolTip();
                var startIndex = 1;

                for (int index = 0, size = tooltip.size(); index < size; index++) {
                    var entry = tooltip.get(index);

                    if (entry instanceof Translation && ((Translation) entry).getKey().equals("item.modifiers.mainhand")) {
                        startIndex += index;
                    }
                }

                var toIndex = tooltip.size();
                // var fromIndex = Math.min(toIndex - 1, startIndex + ((SoulboundItem) item).getMainhandAttributeEntries(itemStack, player));

                var prior = new ArrayList<>(tooltip).subList(0, startIndex);
                var insertion = storage.tooltip();
                // var posterior = new ArrayList<>(tooltip).subList(fromIndex, toIndex);

                tooltip.clear();
                tooltip.addAll(prior);
                tooltip.addAll(insertion);
                // tooltip.addAll(posterior);

                var row = insertion.lastIndexOf(StringTextComponent.EMPTY) + prior.size();

                tooltipBar.data(row, CellElement.textRenderer.width(tooltip.get(row - 2)) - 4);
            });
        }
    }

    @SubscribeEvent
    public static void onRenderTooltip(RenderTooltipEvent.PostBackground event) {
        if (event.getStack().getItem() instanceof SoulboundItem) {
            tooltipBar.drawTooltip(event.getMatrixStack(), event.getX(), event.getY(), event.getStack());
        }
    }

    @SubscribeEvent
    public static void onLoadResources(AddReloadListenerEvent event) {
        event.addListener(new ExperienceBarReloader());
    }

    private static class ExperienceBarReloader extends ReloadListener<Void> {
        @Override
        protected Void prepare(IResourceManager manager, IProfiler profiler) {
            return null;
        }

        @Override
        protected void apply(Void nothing, IResourceManager manager, IProfiler profiler) {
            RenderSystem.recordRenderCall(() -> {
                overlayBar = new ExperienceBarOverlay();
                tooltipBar = new ExperienceBarOverlay();
                overlayBar.width(182).height(5).center(true);
            });
        }
    }
}