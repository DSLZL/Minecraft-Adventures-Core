package net.mrbt0907.util.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.inventory.IInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.util.MrbtAPI;
import net.mrbt0907.util.capabilities.CapabilityCrossbow;
import net.mrbt0907.util.capabilities.CapabilityCrossbow.Provider;
import net.mrbt0907.util.item.ItemCrossbow;
import net.mrbt0907.util.util.ReflectionUtil;

public class CapabilityHandler
{
	public static final ResourceLocation CROSSBOW = new ResourceLocation(MrbtAPI.MODID, "crossbow");
	public static void preInit()
	{
		CapabilityManager.INSTANCE.register(CapabilityCrossbow.class, new CapabilityCrossbow.Storage(), CapabilityCrossbow::new);
	}
	
	@SubscribeEvent
	public static void attach(AttachCapabilitiesEvent<ItemStack> event)
	{
		ItemStack stack = event.getObject();
		
		if (stack.getItem() instanceof ItemCrossbow)
			event.addCapability(CROSSBOW, new CapabilityCrossbow.Provider());
	}
	
	@SubscribeEvent
	public static void onPlayerTracking(PlayerEvent.StartTracking event)
	{
		if (!(event.getTarget() instanceof EntityPlayer)) return;
		EntityPlayer player = (EntityPlayer) event.getTarget();
		if (player.world.isRemote) return;
		
		IInventory inventory = player.inventory;
		int size = inventory.getSizeInventory();
		ItemStack stack;
		for(int i = 0; i < size; i++)
		{
			stack = inventory.getStackInSlot(i);
			if (stack.hasCapability(CapabilityCrossbow.Provider.INSTANCE, Provider.FACE))
				stack.getCapability(CapabilityCrossbow.Provider.INSTANCE, Provider.FACE).markDirty(player, "inventory", "field_71071_by", i);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void onNetworkRecieve(String capability, String entityClass, UUID entityUUID, String fieldName, String obfName, int inventoryIndex, NBTTagCompound nbtMagazine)
	{
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
		if (mc.world != null)
		{
			List<Entity> entities = new ArrayList<Entity>(mc.world.loadedEntityList);
			IInventory inventory = null;
			
			for(Entity entity : entities)
			{
				if (entity.getUniqueID().equals(entityUUID))
				{
					Class<?> clazz = ReflectionUtil.getClass(entityClass);
					Object obj = null;
					try
					{
						obj = clazz != null ? ReflectionUtil.get(entity.getClass(), entity, fieldName, obfName) : null;
					}
					catch(Exception e) {}
					if (obj instanceof IInventory)
					{
						inventory = (IInventory) obj;
					}
					break;
				}
			}
			
			if (inventory != null && inventoryIndex < inventory.getSizeInventory())
			{
				ItemStack stack = inventory.getStackInSlot(inventoryIndex);	
				if (!stack.isEmpty())
				{
					switch (capability)
					{
						case "crossbow":
							if (stack.getItem() instanceof ItemCrossbow && stack.hasCapability(CapabilityCrossbow.Provider.INSTANCE, Provider.FACE))
							{
								CapabilityCrossbow.Provider.INSTANCE.readNBT(stack.getCapability(CapabilityCrossbow.Provider.INSTANCE, Provider.FACE), Provider.FACE, nbtMagazine);
							}	
							break;
					}
				}
			}
		}
	}
}
