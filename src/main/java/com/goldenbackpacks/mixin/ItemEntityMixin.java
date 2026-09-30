package com.goldenbackpacks.mixin;

import com.goldenbackpacks.GoldenBackpacks;
import com.goldenbackpacks.augment.BackpackAugmentType;
import com.goldenbackpacks.augment.BackpackAugments;
import com.goldenbackpacks.item.BackpackItem;
import com.goldenbackpacks.item.component.BackpackAugmentsComponent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void goldenbackpacks$fireImmune(CallbackInfoReturnable<Boolean> cir) {
        ItemEntity entity = (ItemEntity) (Object) this;
        ItemStack stack = entity.getItem();
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            var tier = backpackItem.getTier();
            if (BackpackAugments.isUnlocked(tier, BackpackAugmentType.IMBUED_HIDE)) {
                BackpackAugmentsComponent augments = GoldenBackpacks.getOrCreateAugments(stack, tier);
                cir.setReturnValue(augments.imbuedHideEnabled());
            }
        }
    }
}
