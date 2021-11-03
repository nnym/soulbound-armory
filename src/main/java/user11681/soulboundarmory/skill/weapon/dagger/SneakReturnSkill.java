package user11681.soulboundarmory.skill.weapon.dagger;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import user11681.cell.client.gui.screen.CellScreen;
import user11681.soulboundarmory.registry.Skills;
import user11681.soulboundarmory.skill.Skill;

public class SneakReturnSkill extends Skill {
    public SneakReturnSkill(Identifier identifier) {
        super(identifier);
    }

    @Override
    public void initDependencies() {
        this.dependencies.add(Skills.returning);

        super.initDependencies();
    }

    @Override
    public int cost(boolean learned, int level) {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(CellScreen screen, MatrixStack matrices, int level, int x, int y, int zOffset) {
        screen.renderGuiItem(Items.LEAD.getDefaultStack(), x, y, zOffset);
    }
}
