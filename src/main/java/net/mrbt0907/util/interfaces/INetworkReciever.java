package net.mrbt0907.util.interfaces;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.mrbt0907.util.network.NetworkHandler;

public interface INetworkReciever
{
	public String getID();
	public void onClientRecieved(int commandID, NBTTagCompound nbt);
	public void onServerRecieved(int commandID, NBTTagCompound nbt, EntityPlayerMP player);
	public default boolean sendToClients(int commandID, NBTTagCompound nbt, Object... targets)
	{
		return NetworkHandler.sendToClients(getID(), commandID, nbt, targets);
	}
	public default boolean sendToServer(int commandID, NBTTagCompound nbt)
	{
		return NetworkHandler.sendToServer(getID(), commandID, nbt);
	}
}
