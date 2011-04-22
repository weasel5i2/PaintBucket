package net.weasel.PaintBucket;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class PaintBucketListener extends PlayerListener
{
	public PaintBucket plugin;
	public Server server;
	
	public PaintBucketListener(PaintBucket instance)
	{
		plugin = instance;
		server = plugin.getServer();
	}

	public void onPlayerInteract( PlayerInteractEvent event )
	{
		Player player = event.getPlayer();
		
		if( PaintBucket.isAllowed(player) == true  && PaintBucket.isPaintBucketDisabled(player) == false )
		{
			int paintTypeId = 0;

			if( PaintBucket.isPaintingWithBlocks.containsKey(player) == false )
				PaintBucket.isPaintingWithBlocks.put( player, false );
			
			if( PaintBucket.paintBlocks.containsKey(player) )
			{
				paintTypeId = PaintBucket.paintBlocks.get(player);
			}
			else if( PaintBucket.isPaintingWithBlocks.get(player) == true )
			{
				paintTypeId = player.getItemInHand().getTypeId();
				PaintBucket.paintBlocks.put( player, paintTypeId );
				PaintBucket.paintBlockData.put( player, Integer.parseInt(String.valueOf(player.getItemInHand().getData().getData() ) ) );
			}
			else 
			{
				player.sendMessage( ChatColor.GREEN + "Please first set a block type with the " 
				+ ChatColor.YELLOW + "/paintbucket" + ChatColor.GREEN + " command." );
				player.sendMessage( ChatColor.GREEN + "(or try " + ChatColor.YELLOW 
				+ "/paintbucket disable" + ChatColor.GREEN + " to use your bucket normally.)" );
				event.setCancelled( true );
				return;
			}

			if( PaintBucket.Permissions.has(player, "paintblock.use" ) 
			&& PaintBucket.isPaintingWithBlocks.get(player) == true )
			{
				ItemStack typeBlock = player.getItemInHand();
				PaintBucket.paintBlocks.put(player, typeBlock.getTypeId() );
				PaintBucket.paintBlockData.put(player, Integer.parseInt( String.valueOf( typeBlock.getData().getData() ) ) );
				paintTypeId = PaintBucket.paintBlocks.get(player);
				
				if( event.getAction() == Action.RIGHT_CLICK_BLOCK )
				{
					Block targetBlock = event.getClickedBlock();
					
					if( PaintBucket.blockLists.containsKey(player) )
					{
						if( targetBlock.getTypeId() == paintTypeId ) return;
						
						player.sendMessage( "Painting.." );
						PaintBucket.targetBlocks.put(player, targetBlock.getTypeId() );
						PaintBucket.blockLists.get(player).add( targetBlock.getLocation() );
					}
					else
					{
						if( targetBlock.getTypeId() == paintTypeId ) return;

						ArrayList<Location> newList = new ArrayList<Location>();
						newList.add( targetBlock.getLocation() );

						player.sendMessage( "Painting.." );
						PaintBucket.targetBlocks.put(player, targetBlock.getTypeId() );
						PaintBucket.blockLists.put( player, newList );
					}
					
					PaintBucket.isPaintingWithBlocks.put( player, false );

					event.setCancelled( true );
				}
			}
			
			else if( player.getItemInHand().getType() == Material.BUCKET )
			{
				if( event.getAction() == Action.RIGHT_CLICK_BLOCK )
				{
					Block targetBlock = event.getClickedBlock();
					
					if( PaintBucket.blockLists.containsKey(player) )
					{
						if( targetBlock.getTypeId() == paintTypeId ) return;
						
						player.sendMessage( "Painting.." );
						PaintBucket.targetBlocks.put(player, targetBlock.getTypeId() );
						PaintBucket.blockLists.get(player).add( targetBlock.getLocation() );
					}
					else
					{
						if( targetBlock.getTypeId() == paintTypeId ) return;

						ArrayList<Location> newList = new ArrayList<Location>();
						newList.add( targetBlock.getLocation() );

						player.sendMessage( "Painting.." );
						PaintBucket.targetBlocks.put(player, targetBlock.getTypeId() );
						PaintBucket.blockLists.put( player, newList );
					}
					
					event.setCancelled( true );
				}
			}
		}
	}
}
