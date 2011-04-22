package net.weasel.PaintBucket;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PaintBucket extends JavaPlugin
{
	public static HashMap<Player,Boolean> isPaintingWithBlocks = new HashMap<Player,Boolean>();
	public static HashMap<Player,Integer> paintBlocks = new HashMap<Player,Integer>();
	public static HashMap<Player,Integer> paintBlockData = new HashMap<Player,Integer>();
	public static HashMap<Player,Integer> targetBlocks = new HashMap<Player,Integer>();
	public static HashMap<Player,ArrayList<Location>> blockLists = new HashMap<Player,ArrayList<Location>>();
	public static ArrayList<Player> disabledList = new ArrayList<Player>();
	public static PermissionHandler Permissions;
	public static BukkitScheduler timer = null;
	public static Integer tTask = 0;
	
	public static PluginManager manager = null;
	public static PaintBucket plugin = null;
	public static Server server = null;
	
	@Override
	public void onDisable() 
	{
		try
		{
			timer.cancelTask(tTask);
		}
		catch( Exception e )
		{
			e.getMessage();
		}

		System.out.println( "[" + getDescription().getName() + "] " 
				+ getDescription().getName() + " v" + getDescription().getVersion() + " disabled." );
	}

	@Override
	public void onEnable() 
	{
		
		setupPermissions();
		
		plugin = this;
		server = plugin.getServer();
		manager = server.getPluginManager();
		timer = server.getScheduler();
		
		manager.registerEvent(Event.Type.PLAYER_INTERACT, new PaintBucketListener(plugin), Priority.Normal, plugin );
		
		tTask = timer.scheduleSyncRepeatingTask( this, new TimerTask(this), 4, 1 );

		System.out.println( "[" + getDescription().getName() + "] " 
				+ getDescription().getName() + " v" + getDescription().getVersion() + " enabled." );
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		String pCommand = command.getName().toLowerCase();

		if( sender instanceof Player )
		{
			if( pCommand.equals( "paintblock" ) || pCommand.equals( "pblock" ) )
			{
				Player player = (Player)sender;
				
				if( isPaintingWithBlocks.containsKey(player) )
				{
					if( isPaintingWithBlocks.get(player) == false )
					{
						isPaintingWithBlocks.put( player,true );
						player.sendMessage( "Your next right-click will paint with the block in your hand." );
					}
					else
					{
						player.sendMessage( "Your next right-click will be normal." );
					}
				}
				else
				{
					isPaintingWithBlocks.put( player,true );
					player.sendMessage( "Your next right-click will paint with the block in your hand." );
				}
			}
			
			if( pCommand.equals( "pbs" ) )
			{
				Player player = (Player)sender;
				
				if( isAllowed( player ) )
				{
					if( blockLists.containsKey(player) )
					{	
						if( blockLists.get(player).size() > 0 )
						{
							blockLists.get(player).clear();
							player.sendMessage( "Paint bucket operation cancelled." );
							return true;
						}
						else
							player.sendMessage( "You have no paint operations currently running." );
					}
					return true;
				}
			}

			if( pCommand.equals("pbstat") )
			{
				Player player = (Player)sender;
				
				if( isAllowed( (Player)sender ) )
				{
					if( blockLists.containsKey(player) )
					{	
						if( blockLists.get(player).size() > 0 )
						{
							player.sendMessage( "You have " + blockLists.get(player).size() + " blocks being processed." );
							return true;
						}
						else
							player.sendMessage( "You have no paint operations currently running." );
					}
					return true;
				}
			}
			
			if( pCommand.equals("paintbucket") || pCommand.equals("pb") )
			{
				if( isAllowed( (Player)sender ) )
				{
					if( args.length > 0 )
					{
						String argStr = arrayToString( args, " " );
						Player player = (Player)sender;
						
						if( argStr.toLowerCase().equals( "cancel" ) 
						|| argStr.toLowerCase().equals( "stop" ) )
						{
							if( blockLists.containsKey(player) )
							{	
								if( blockLists.get(player).size() > 0 )
								{
									blockLists.get(player).clear();
									player.sendMessage( "Paint bucket operation cancelled." );
									return true;
								}
							}
						}
						
						if( argStr.toLowerCase().equals( "disable" ) )
						{
							if( disabledList.contains(player) == false )
							{
								paintBucketDisable(player);
								return true;
							}
							else
								return false;
						}
						
						if( argStr.toLowerCase().equals( "enable" ) )
						{
							if( disabledList.contains(player) == true )
							{
								paintBucketEnable(player);
								
								return true;
							}
							else
								return false;
						}

						if( argStr.contains(":") )
						{
							String blockStr[] = argStr.split( ":" );
							
							if( blockStr.length == 2 )
							{
								if( isNumeric( blockStr[0] ) && isNumeric(blockStr[1] ) )
								{
									paintBlocks.put( player, Integer.parseInt(blockStr[0]) );
									paintBlockData.put( player, Integer.parseInt(blockStr[1]) );
									player.sendMessage( "Paint bucket set to " + getBlockNameById(Integer.parseInt(blockStr[0]) ) + "." );
									
									return true;
								}
								else
									return false;
							}
						}
						else if( isNumeric( argStr ) == true )
						{
							int tArg = Integer.parseInt( args[0] ); 
	
							paintBlocks.put( player, tArg );
							player.sendMessage( "Paint bucket set to " + getBlockNameById(tArg) + "." );
							return true;
						}
						else
						{
							int tArg = getBlockIdByName( argStr );
							
							if( tArg != -1 )
							{
								paintBlocks.put( player, tArg );
								player.sendMessage( "Paint bucket set to " + getBlockNameById(tArg) + "." );
								return true;
							}
							else
								return false;
						}
					}
					else
					{
						Player player = (Player)sender;

						if( blockLists.containsKey(player) == true )
						{
							try
							{
								if( paintBlocks.containsKey(player) )
								{
									player.sendMessage( ChatColor.BLUE + "Your paint bucket is set to paint with "
									+ ChatColor.YELLOW + getBlockNameById( paintBlocks.get(player) ) );
									
									return true;
								}
							}
							catch( NullPointerException e )
							{
								return false;
							}
						}
						else
							return false;
					}

				}
				else
					return true;
			}
		}
		
		return true;
	}

	private void setupPermissions() 
	{
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

	    if (PaintBucket.Permissions == null) 
	    {
	    	if (test != null) 
	    	{
	    		PaintBucket.Permissions = ((Permissions)test).getHandler();
	    	}
	    	else 
	    	{
	    		logOutput("Permission system not detected, defaulting to OP");
	        }
	    }
	}

	public static Boolean isAllowed( Player player )
	{
		if( Permissions.has(player, "paintbucket.use") ) 
		    return true;
		else
			return false;
	}
	
	public static Boolean isPaintBucketDisabled( Player player )
	{
		return( disabledList.contains(player) );
	}
	
	public static void paintBucketDisable( Player player )
	{
		if( isPaintBucketDisabled(player) == true )
		{
			player.sendMessage( "Your paint bucket is already disabled." );
		}
		else
		{
			disabledList.add(player);
			player.sendMessage( "Your buckets will now function normally." );
		}
	}

	public static void paintBucketEnable( Player player )
	{
		if( isPaintBucketDisabled(player) == true )
		{
			disabledList.remove(player);
			player.sendMessage( "Paint bucket enabled!" );
		}
		else
			player.sendMessage( "Your paint bucket is already disabled." );
	}
	
	public static ArrayList<Location> getListForPlayer( Player player )
	{
		ArrayList<Location> retVal = null;
		
		if( blockLists.containsKey(player) )
		{
			retVal = blockLists.get(player);
		}
		
		return retVal;
	}
	
	public static String getBlockNameById( Integer blockID )
    {
    	String retVal = "NOT FOUND";
    	
    	String bSplit = "Air;Stone;Grass;Dirt;Cobblestone;" // 0-4
    				  + "Wooden Planks;Sapling;Adminium;Water;Stationary Water;" // 5-9 
    				  + "Lava;Stationary Lava;Sand;Gravel;Gold Ore;" // 10-14
    				  + "Iron Ore;Coal Ore;Wood;Leaves;Sponge;" // 15-19
    				  + "Glass;Lapis Ore;Lapis Block;Dispenser;Sandstone;" // 20-24
    				  + "Note Block;Bed;0;0;0;" // 25-29
    				  + "0;0;0;0;0;" // 30-34
    				  + "Wool;0;Yellow Flower;Red Rose;Brown Mushroom;" // 35-39
    				  + "Red Mushroom;Gold Block;Iron Block;Double Slab;Slab;" // 40-44
    				  + "Brick Block;TNT;Bookshelf;Moss Stone;Obsidian;" // 45-49
    				  + "Torch;Fire;Mob Spawner;Wooden Stairs;Chest;" // 50-54
    				  + "Redstone Wire Block;Diamond Ore;Diamond Block;Crafting Table;Crops;" // 55-59
    				  + "Farmland;Furnace;Burning Furnace;Sign Post;Wooden Door;" // 60-64
    				  + "Ladder;Rails;Stone Stairs;Wall Sign;Lever;" // 65-69
    				  + "Stone Pressure Plate;Iron Door;Wooden Pressure Plate;"
    				     + "Redstone Ore;Redstone Ore (lit);" // 70-74
    				  + "Redstone Torch (off);Redstone Torch (on);Stone Button;Snow;Ice;" // 75-79
    				  + "Snow Block;Cactus Block;Clay Block;Sugar Cane;Jukebox;" // 80-84
    				  + "Fence;Pumpkin;Netherrack;Soul Sand;Glowstone Block;" // 85-89
    				  + "Portal;Jack-o-Lantern;Cake Block;Diode (off);Diode (on)"; // 90-94
    	
    	String[] blockNames = bSplit.split(";");
    	
    	if( blockID < blockNames.length ) retVal = blockNames[blockID];
    	
    	return retVal;
    }

    public static int getBlockIdByName( String name )
    {
    	int retVal = -1;
    	
    	String bSplit = "Air;Stone;Grass;Dirt;Cobblestone;" // 0-4
    				  + "Wooden Planks;Sapling;Adminium;Water;Stationary Water;" // 5-9 
    				  + "Lava;Stationary Lava;Sand;Gravel;Gold Ore;" // 10-14
    				  + "Iron Ore;Coal Ore;Wood;Leaves;Sponge;" // 15-19
    				  + "Glass;Lapis Ore;Lapis Block;Dispenser;Sandstone;" // 20-24
    				  + "Note Block;Bed;0;0;0;" // 25-29
    				  + "0;0;0;0;0;" // 30-34
    				  + "Wool;0;Yellow Flower;Red Rose;Brown Mushroom;" // 35-39
    				  + "Red Mushroom;Gold Block;Iron Block;Double Slab;Slab;" // 40-44
    				  + "Brick Block;TNT;Bookshelf;Moss Stone;Obsidian;" // 45-49
    				  + "Torch;Fire;Mob Spawner;Wooden Stairs;Chest;" // 50-54
    				  + "Redstone Wire Block;Diamond Ore;Diamond Block;Crafting Table;Crops;" // 55-59
    				  + "Farmland;Furnace;Burning Furnace;Sign Post;Wooden Door;" // 60-64
    				  + "Ladder;Rails;Stone Stairs;Wall Sign;Lever;" // 65-69
    				  + "Stone Pressure Plate;Iron Door;Wooden Pressure Plate;"
    				     + "Redstone Ore;Redstone Ore (lit);" // 70-74
    				  + "Redstone Torch (off);Redstone Torch (on);Stone Button;Snow;Ice;" // 75-79
    				  + "Snow Block;Cactus Block;Clay Block;Sugar Cane;Jukebox;" // 80-84
    				  + "Fence;Pumpkin;Netherrack;Soul Sand;Glowstone Block;" // 85-89
    				  + "Portal;Jack-o-Lantern;Cake Block;Diode (off);Diode (on)"; // 90-94
    	
    	String[] blockNames = bSplit.split(";");
    	
    	for( int X = 0; X < blockNames.length; X++ )
    	{
    		if( blockNames[X].toLowerCase().startsWith( name.toLowerCase() ) 
    		|| blockNames[X].toLowerCase().equals( name.toLowerCase() ))
    		{
    			retVal = X;
    			break;
    		}
    	}
    	
    	return retVal;
    }

	public static void startPaint( Player player, Block block )
	{	
		if( blockLists.containsKey(player) )
		{
			if( block.getTypeId() != 0 ) 
			{
				blockLists.get(player).add( block.getLocation() );
			}
		}
		else
		{
			ArrayList<Location> newEntry = new ArrayList<Location>();
			newEntry.add( block.getLocation() );
			blockLists.put( player, newEntry );
		}
	}
	
	public static void delBlock( Location blockLoc, Player player )
	{
		if( blockLists.containsKey(player) )
		{
			blockLists.get(player).remove( blockLoc );
			
			if( blockLists.get(player).size() < 1 ) 
			{
				player.sendMessage( "Done." );
			}
		}
	}
	
	public static void getAdjacentBlocks( Block whichBlock, Player player )
    {
    	Block targetBlock = null;

    	if( blockLists.containsKey(player) )
    	{
	    	targetBlock = whichBlock.getRelative(BlockFace.UP); 
	    	if( targetBlock.getType() == whichBlock.getType() 
	    	&& !blockLists.get(player).contains(targetBlock.getLocation() )) 
	    		blockLists.get(player).add( targetBlock.getLocation() );
	    	
	    	targetBlock = whichBlock.getRelative(BlockFace.DOWN); 
	    	if( targetBlock.getType() == whichBlock.getType() 
	    	&& !blockLists.get(player).contains(targetBlock.getLocation() )) 
	    		blockLists.get(player).add( targetBlock.getLocation() );
	    	
	    	targetBlock = whichBlock.getRelative(BlockFace.NORTH); 
	    	if( targetBlock.getType() == whichBlock.getType() 
	    	&& !blockLists.get(player).contains(targetBlock.getLocation() )) 
	    		blockLists.get(player).add( targetBlock.getLocation() );
	    	
	    	targetBlock = whichBlock.getRelative(BlockFace.EAST); 
	    	if( targetBlock.getType() == whichBlock.getType() 
	    	&& !blockLists.get(player).contains(targetBlock.getLocation() )) 
	    		blockLists.get(player).add( targetBlock.getLocation() );
	    	
	    	targetBlock = whichBlock.getRelative(BlockFace.SOUTH); 
	    	if( targetBlock.getType() == whichBlock.getType() 
	    	&& !blockLists.get(player).contains(targetBlock.getLocation() )) 
	    		blockLists.get(player).add( targetBlock.getLocation() );
	    	
	    	targetBlock = whichBlock.getRelative(BlockFace.WEST); 
	    	if( targetBlock.getType() == whichBlock.getType() 
	    	&& !blockLists.get(player).contains(targetBlock.getLocation() )) 
	    		blockLists.get(player).add( targetBlock.getLocation() );
    	}
    }
	
	public static boolean isNumeric( String text )
	{
		return text.matches( "[-+]?\\d+(\\.\\d+)?" );
	}

	public static String arrayToString(String[] a, String separator) 
    {
        String result = "";
        
        if (a.length > 0) 
        {
            result = a[0];    // start with the first element
            for (int i=1; i<a.length; i++) {
                result = result + separator + a[i];
            }
        }
        
        return result;
    }
	
	public static void logOutput( String message )
	{
		System.out.println( "[" + plugin.getDescription().getName() + "] " + message );
	}
	
	public static Integer getPlayerPaintBlockTypeId( Player player )
	{		
		if( paintBlocks.containsKey(player) == true )
			return paintBlocks.get(player);
		else
			return null;
	}
}
