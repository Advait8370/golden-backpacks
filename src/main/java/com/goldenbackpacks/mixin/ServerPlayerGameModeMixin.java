package com.goldenbackpacks.mixin;

import com.goldenbackpacks.augment.BackpackAugmentHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Unique
    private BlockState goldenbackpacks$capturedMinedState;

    @Unique
    private BlockPos goldenbackpacks$capturedMinedPos;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void goldenbackpacks$captureMinedBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.goldenbackpacks$capturedMinedState = this.player.serverLevel().getBlockState(pos);
        this.goldenbackpacks$capturedMinedPos = pos.immutable();
    }

    @Inject(
            method = "destroyAndAck",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayerGameMode;debugLogging(Lnet/minecraft/core/BlockPos;ZILjava/lang/String;)V",
                    ordinal = 0
            )
    )
    private void goldenbackpacks$afterSuccessfulDestroy(BlockPos pos, int action, String reason, CallbackInfo ci) {
        if (this.goldenbackpacks$capturedMinedState != null && this.goldenbackpacks$capturedMinedPos != null) {
            BackpackAugmentHandler.onBlockBroken(this.player, this.goldenbackpacks$capturedMinedState, this.goldenbackpacks$capturedMinedPos);
        }
    }

    @Inject(method = "destroyAndAck", at = @At("TAIL"))
    private void goldenbackpacks$clearCapturedMinedBlock(BlockPos pos, int action, String reason, CallbackInfo ci) {
        this.goldenbackpacks$capturedMinedState = null;
        this.goldenbackpacks$capturedMinedPos = null;
    }
}

