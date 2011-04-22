package net.weasel.PaintBucket;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TimerTask implements Runnable
{
	public static PaintBucket plugin;
	public static Server server;
	
	public TimerTask( PaintBucket instance )
	{
		plugin = instance;
		server = plugin.getServer();
	}
	
	@Override
	public void run() 
	{
		World world = null;
		Player player = null;
		Location targetLoc = null;
		int blockTypeId = 0;
		
		Object[] players = PaintBucket.blockLists.keySet().toArray();
		
		for( int P = 0; P < players.length; P++ )
		{
			player = (Player)players[P];
			
			if( player != null )
			{
				if( PaintBucket.paintBlocks.containsKey(player) )
				{
					blockTypeId = PaintBucket.paintBlocks.get(player);
					
					if( PaintBucket.blockLists.get(player).size() > 0)
					{
						for( int C = 0; C < 4; C++ )
						{
							try
							{
								targetLoc = PaintBucket.blockLists.get(player).get(0);
								
								world = player.getWorld();
								
								if( world.getBlockAt(targetLoc) != null )
								{
									if( world.getBlockAt(targetLoc).getTypeId() == PaintBucket.targetBlocks.get(player) )
									{
										PaintBucket.getAdjacentBlocks( world.getBlockAt(targetLoc), player );
										world.getBlockAt(targetLoc).setTypeId( blockTypeId );
										world.getBlockAt(targetLoc).setData( Byte.parseByte( String.valueOf( PaintBucket.paintBlockData.get(player) ) ) );
										PaintBucket.delBlock( targetLoc, player );
									}
								}
							}
							catch( Exception e )
							{
								e.getMessage();
							}
						}
					}
				}
			}
		}
	}
}
