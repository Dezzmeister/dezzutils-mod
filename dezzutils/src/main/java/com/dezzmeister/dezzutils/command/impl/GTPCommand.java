package com.dezzmeister.dezzutils.command.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.dezzmeister.dezzutils.DezzUtilsMod;
import com.dezzmeister.dezzutils.command.Control;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.LocationInput;
import net.minecraft.command.arguments.RotationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;

/**
 * Modified teleport command that prevents anyone from teleporting the protected
 * player.
 * 
 * @author Joe Desmond
 */
public class GTPCommand {
	
	private static final IFormattableTextComponent getProtectedWarningText(final String caller) {
		final StringTextComponent playerComponent = new StringTextComponent(caller);
		playerComponent.func_240701_a_(TextFormatting.YELLOW);
		
		final StringTextComponent warningComponent = new StringTextComponent(" attempted to g-teleport you to ");
		warningComponent.func_240701_a_(TextFormatting.RED);
		
		return playerComponent.func_230529_a_(warningComponent);
	}
	
	private static final void warnPlayer(final String caller, final Entity player, final String destString) {
		final StringTextComponent posComponent = new StringTextComponent(destString);
		posComponent.func_240701_a_(TextFormatting.BLUE);
		
		final IFormattableTextComponent message = getProtectedWarningText(caller).func_230529_a_(posComponent);
		player.sendMessage(message, player.getUniqueID());
	}
	
	private static final boolean canTeleport(final CommandSource source, final Entity target) {
		try {
			final ServerPlayerEntity caller = source.asPlayer(); // Throws an exception if the caller is not a ServerPlayerEntity
			if (target instanceof ServerPlayerEntity) {
				final ServerPlayerEntity subject = (ServerPlayerEntity) target;
				
				if (subject.getName().getString().equals(DezzUtilsMod.PROTECTED_PLAYER) && !caller.getName().getString().equals(DezzUtilsMod.PROTECTED_PLAYER)) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private static final SimpleCommandExceptionType field_241077_a_ = new SimpleCommandExceptionType(
			new TranslationTextComponent("commands.teleport.invalidPosition"));

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralCommandNode<CommandSource> literalcommandnode = dispatcher
				.register(Commands.literal("gteleport").requires((p_198816_0_) -> {
					return p_198816_0_.hasPermissionLevel(2);
				}).then(Commands.argument("targets", EntityArgument.entities())
						.then(Commands.argument("location", Vec3Argument.vec3()).executes((p_198807_0_) -> {
							return teleportToPos(p_198807_0_.getSource(),
									EntityArgument.getEntities(p_198807_0_, "targets"),
									p_198807_0_.getSource().getWorld(),
									Vec3Argument.getLocation(p_198807_0_, "location"), (ILocationArgument) null,
									(GTPCommand.Facing) null);
						}).then(Commands.argument("rotation", RotationArgument.rotation()).executes((p_198811_0_) -> {
							return teleportToPos(p_198811_0_.getSource(),
									EntityArgument.getEntities(p_198811_0_, "targets"),
									p_198811_0_.getSource().getWorld(),
									Vec3Argument.getLocation(p_198811_0_, "location"),
									RotationArgument.getRotation(p_198811_0_, "rotation"), (GTPCommand.Facing) null);
						})).then(Commands.literal("facing").then(Commands.literal("entity").then(
								Commands.argument("facingEntity", EntityArgument.entity()).executes((p_198806_0_) -> {
									return teleportToPos(p_198806_0_.getSource(),
											EntityArgument.getEntities(p_198806_0_, "targets"),
											p_198806_0_.getSource().getWorld(),
											Vec3Argument.getLocation(p_198806_0_, "location"), (ILocationArgument) null,
											new GTPCommand.Facing(EntityArgument.getEntity(p_198806_0_, "facingEntity"),
													EntityAnchorArgument.Type.FEET));
								}).then(Commands.argument("facingAnchor", EntityAnchorArgument.entityAnchor())
										.executes((p_198812_0_) -> {
											return teleportToPos(p_198812_0_.getSource(),
													EntityArgument.getEntities(p_198812_0_, "targets"),
													p_198812_0_.getSource().getWorld(),
													Vec3Argument.getLocation(p_198812_0_, "location"),
													(ILocationArgument) null,
													new GTPCommand.Facing(
															EntityArgument.getEntity(p_198812_0_, "facingEntity"),
															EntityAnchorArgument.getEntityAnchor(p_198812_0_,
																	"facingAnchor")));
										}))))
								.then(Commands.argument("facingLocation", Vec3Argument.vec3())
										.executes((p_198805_0_) -> {
											return teleportToPos(p_198805_0_.getSource(),
													EntityArgument.getEntities(p_198805_0_, "targets"),
													p_198805_0_.getSource().getWorld(),
													Vec3Argument.getLocation(p_198805_0_, "location"),
													(ILocationArgument) null, new GTPCommand.Facing(
															Vec3Argument.getVec3(p_198805_0_, "facingLocation")));
										}))))
						.then(Commands.argument("destination", EntityArgument.entity()).executes((p_198814_0_) -> {
							return teleportToEntity(p_198814_0_.getSource(),
									EntityArgument.getEntities(p_198814_0_, "targets"),
									EntityArgument.getEntity(p_198814_0_, "destination"));
						}))).then(Commands.argument("location", Vec3Argument.vec3()).executes((p_200560_0_) -> {
							return teleportToPos(p_200560_0_.getSource(),
									Collections.singleton(p_200560_0_.getSource().assertIsEntity()),
									p_200560_0_.getSource().getWorld(),
									Vec3Argument.getLocation(p_200560_0_, "location"), LocationInput.current(),
									(GTPCommand.Facing) null);
						})).then(Commands.argument("destination", EntityArgument.entity()).executes((p_200562_0_) -> {
							return teleportToEntity(p_200562_0_.getSource(),
									Collections.singleton(p_200562_0_.getSource().assertIsEntity()),
									EntityArgument.getEntity(p_200562_0_, "destination"));
						})));
		dispatcher.register(Commands.literal("gtp").requires((p_200556_0_) -> {
			return p_200556_0_.hasPermissionLevel(2);
		}).redirect(literalcommandnode));
	}

	private static int teleportToEntity(CommandSource source, Collection<? extends Entity> targets, Entity destination)
			throws CommandSyntaxException {
		
		if (Control.violatesGhost(source, targets, destination)) {
			Control.showNoEntityFound(source);
			return 1;
		}
		
		for (Entity entity : targets) {
			if (!canTeleport(source, entity)) {
				source.sendFeedback(Control.NOT_ALLOWED_MESSAGE, false);
				warnPlayer(source.asPlayer().getName().getString(), entity, destination.getName().getString());
				return 0;
			}
			
			teleport(source, entity, (ServerWorld) destination.world, destination.getPosX(), destination.getPosY(),
					destination.getPosZ(), EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class),
					destination.rotationYaw, destination.rotationPitch, (GTPCommand.Facing) null);
		}

		if (targets.size() == 1) {
			source.sendFeedback(new TranslationTextComponent("commands.teleport.success.entity.single",
					targets.iterator().next().getDisplayName(), destination.getDisplayName()), false);
		} else {
			source.sendFeedback(new TranslationTextComponent("commands.teleport.success.entity.multiple",
					targets.size(), destination.getDisplayName()), false);
		}

		return targets.size();
	}

	private static int teleportToPos(CommandSource source, Collection<? extends Entity> targets, ServerWorld worldIn,
			ILocationArgument position, @Nullable ILocationArgument rotationIn, @Nullable GTPCommand.Facing facing)
			throws CommandSyntaxException {
		Vector3d vector3d = position.getPosition(source);
		
		if (Control.violatesGhost(source, targets, null)) {
			Control.showNoEntityFound(source);
			return 1;
		}
		
		for (final Entity target : targets) {
			if (!canTeleport(source, target)) {
				source.sendFeedback(Control.NOT_ALLOWED_MESSAGE, false);
				warnPlayer(source.asPlayer().getName().getString(), target, vector3d.toString());
				return 0;
			}
		}
		
		Vector2f vector2f = rotationIn == null ? null : rotationIn.getRotation(source);
		Set<SPlayerPositionLookPacket.Flags> set = EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class);
		if (position.isXRelative()) {
			set.add(SPlayerPositionLookPacket.Flags.X);
		}

		if (position.isYRelative()) {
			set.add(SPlayerPositionLookPacket.Flags.Y);
		}

		if (position.isZRelative()) {
			set.add(SPlayerPositionLookPacket.Flags.Z);
		}

		if (rotationIn == null) {
			set.add(SPlayerPositionLookPacket.Flags.X_ROT);
			set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
		} else {
			if (rotationIn.isXRelative()) {
				set.add(SPlayerPositionLookPacket.Flags.X_ROT);
			}

			if (rotationIn.isYRelative()) {
				set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
			}
		}

		for (Entity entity : targets) {
			if (rotationIn == null) {
				teleport(source, entity, worldIn, vector3d.x, vector3d.y, vector3d.z, set, entity.rotationYaw,
						entity.rotationPitch, facing);
			} else {
				teleport(source, entity, worldIn, vector3d.x, vector3d.y, vector3d.z, set, vector2f.y, vector2f.x,
						facing);
			}
		}

		if (targets.size() == 1) {
			source.sendFeedback(new TranslationTextComponent("commands.teleport.success.location.single",
					targets.iterator().next().getDisplayName(), vector3d.x, vector3d.y, vector3d.z), false);
		} else {
			source.sendFeedback(new TranslationTextComponent("commands.teleport.success.location.multiple",
					targets.size(), vector3d.x, vector3d.y, vector3d.z), false);
		}

		return targets.size();
	}

	private static void teleport(CommandSource source, Entity entityIn, ServerWorld worldIn, double x, double y,
			double z, Set<SPlayerPositionLookPacket.Flags> relativeList, float yaw, float pitch,
			@Nullable GTPCommand.Facing facing) throws CommandSyntaxException {
		
		BlockPos blockpos = new BlockPos(x, y, z);
		if (!World.func_234935_k_(blockpos)) {
			throw field_241077_a_.create();
		} else {
			if (entityIn instanceof ServerPlayerEntity) {
				ChunkPos chunkpos = new ChunkPos(new BlockPos(x, y, z));
				worldIn.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkpos, 1,
						entityIn.getEntityId());
				entityIn.stopRiding();
				if (((ServerPlayerEntity) entityIn).isSleeping()) {
					((ServerPlayerEntity) entityIn).stopSleepInBed(true, true);
				}

				if (worldIn == entityIn.world) {
					((ServerPlayerEntity) entityIn).connection.setPlayerLocation(x, y, z, yaw, pitch, relativeList);
				} else {
					((ServerPlayerEntity) entityIn).teleport(worldIn, x, y, z, yaw, pitch);
				}

				entityIn.setRotationYawHead(yaw);
			} else {
				float f1 = MathHelper.wrapDegrees(yaw);
				float f = MathHelper.wrapDegrees(pitch);
				f = MathHelper.clamp(f, -90.0F, 90.0F);
				if (worldIn == entityIn.world) {
					entityIn.setLocationAndAngles(x, y, z, f1, f);
					entityIn.setRotationYawHead(f1);
				} else {
					entityIn.detach();
					Entity entity = entityIn;
					entityIn = entityIn.getType().create(worldIn);
					if (entityIn == null) {
						return;
					}

					entityIn.copyDataFromOld(entity);
					entityIn.setLocationAndAngles(x, y, z, f1, f);
					entityIn.setRotationYawHead(f1);
					worldIn.addFromAnotherDimension(entityIn);
				}
			}

			if (facing != null) {
				facing.updateLook(source, entityIn);
			}

			if (!(entityIn instanceof LivingEntity) || !((LivingEntity) entityIn).isElytraFlying()) {
				entityIn.setMotion(entityIn.getMotion().mul(1.0D, 0.0D, 1.0D));
				entityIn.func_230245_c_(true);
			}

			if (entityIn instanceof CreatureEntity) {
				((CreatureEntity) entityIn).getNavigator().clearPath();
			}

		}
	}

	static class Facing {
		private final Vector3d position;
		private final Entity entity;
		private final EntityAnchorArgument.Type anchor;

		public Facing(Entity entityIn, EntityAnchorArgument.Type anchorIn) {
			this.entity = entityIn;
			this.anchor = anchorIn;
			this.position = anchorIn.apply(entityIn);
		}

		public Facing(Vector3d positionIn) {
			this.entity = null;
			this.position = positionIn;
			this.anchor = null;
		}

		public void updateLook(CommandSource source, Entity entityIn) {
			if (this.entity != null) {
				if (entityIn instanceof ServerPlayerEntity) {
					((ServerPlayerEntity) entityIn).lookAt(source.getEntityAnchorType(), this.entity, this.anchor);
				} else {
					entityIn.lookAt(source.getEntityAnchorType(), this.position);
				}
			} else {
				entityIn.lookAt(source.getEntityAnchorType(), this.position);
			}

		}
	}
}
