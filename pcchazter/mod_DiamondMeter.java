package pcchazter.DiamondMeter;

import ModLoader;
import ab;
import apa;
import cg;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarted;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import dk;
import dz;
import ej;
import hs;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import jf;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import wk;
import wm;

@NetworkMod(clientSideRequired=true, serverSideRequired=true, versionBounds="[2.2,2.3)", channels={"DiamondMeter"}, packetHandler=mod_DiamondMeter.class, connectionHandler=mod_DiamondMeter.class)
@Mod(modid="DiamondMeter", name="Diamond Meter", version="2.2.2")
public class mod_DiamondMeter
  implements IPacketHandler, IConnectionHandler
{
  public static wk diamondMeter;

  @Mod.PreInit
  public void load(FMLPreInitializationEvent evt)
  {
    diamondMeter = new ItemDiamondMeter(1028, evt.getSide().equals(Side.CLIENT));

    ModLoader.addName(diamondMeter, "Diamond Meter");
    ModLoader.addRecipe(new wm(diamondMeter, 1), new Object[] { " # ", "dDd", "***", Character.valueOf('#'), apa.aU, Character.valueOf('d'), wk.o, Character.valueOf('D'), apa.aB, Character.valueOf('*'), apa.x });
  }

  @Mod.ServerStarted
  public void serverStarted(FMLServerStartedEvent evt)
  {
    ((hs)MinecraftServer.D().E()).a(new CommandDiamondMeter());
  }

  public static void command_config(String[] command, ab sender)
  {
    String channel = "DiamondMeter";
    String out = "";

    if (command.length > 0)
    {
      if (command[0].equals("reload"))
      {
        sender.a("DiamondMeter: Reloading config file");

        ((ItemDiamondMeter)diamondMeter); ItemDiamondMeter.prop.clear();
        ((ItemDiamondMeter)diamondMeter).loadConfig();

        if (FMLCommonHandler.instance().getSide().equals(Side.SERVER))
        {
          ((ItemDiamondMeter)diamondMeter); Iterator it = ItemDiamondMeter.prop.entrySet().iterator();

          while (it.hasNext())
          {
            Map.Entry i = (Map.Entry)it.next();

            if (out.length() > 0) out = out + "\r\n";

            out = out + (String)i.getKey() + "=" + (String)i.getValue();
          }

          dk packet = new dk(channel, out.getBytes());

          PacketDispatcher.sendPacketToAllPlayers(packet);
        }

        sender.a("DiamondMeter: Reloading config file finished!");
      }
      else if (command[0].equals("current"))
      {
        sender.a("DiamondMeter: Current config:");
        ((ItemDiamondMeter)diamondMeter); sender.a("DiamondMeter: distanceMax: " + ItemDiamondMeter.prop.getProperty("distanceMax"));
        ((ItemDiamondMeter)diamondMeter); sender.a("DiamondMeter: toFind: " + ItemDiamondMeter.prop.getProperty("toFind"));
        ((ItemDiamondMeter)diamondMeter); sender.a("DiamondMeter: playSound: " + ItemDiamondMeter.prop.getProperty("playSound"));
        ((ItemDiamondMeter)diamondMeter); sender.a("DiamondMeter: soundCloser: " + ItemDiamondMeter.prop.getProperty("soundCloser"));
        ((ItemDiamondMeter)diamondMeter); sender.a("DiamondMeter: soundFurther: " + ItemDiamondMeter.prop.getProperty("soundFurther"));
        ((ItemDiamondMeter)diamondMeter); sender.a("DiamondMeter: soundVolume: " + ItemDiamondMeter.prop.getProperty("soundVolume"));
      }
      else if (command[0].equals("set"))
      {
        if (command.length == 3)
        {
          ((ItemDiamondMeter)diamondMeter); ItemDiamondMeter.prop.put(command[1], command[2]);
          ((ItemDiamondMeter)diamondMeter).initProps();
          ((ItemDiamondMeter)diamondMeter).saveConfig();

          if (FMLCommonHandler.instance().getSide().equals(Side.SERVER))
          {
            out = out + command[1] + "=" + command[2];

            dk packet = new dk(channel, out.getBytes());

            PacketDispatcher.sendPacketToAllPlayers(packet);
          }

          sender.a("DiamondMeter: Set " + command[1] + " to " + command[2]);
        }
        else
        {
          sender.a("Usage: /diamondmeter set [distanceMax | toFind | playSound | soundCloser | soundFurther | soundVolume] value");
        }
      }
      else
      {
        sender.a("Usage: /diamondmeter [reload | current | set]");
      }
    }
    else
    {
      sender.a("Usage: /diamondmeter [reload | current | set]");
    }
  }

  public void onPacketData(cg manager, dk packet, Player player)
  {
    if (FMLCommonHandler.instance().getSide().equals(Side.CLIENT))
    {
      String channel = packet.a;

      if (channel.equals("DiamondMeter"))
      {
        ((ItemDiamondMeter)diamondMeter); ItemDiamondMeter.serverProps.clear();

        String[] config = new String(packet.c).split("\r\n");

        for (int i = 0; i < config.length; i++)
        {
          String[] configEntry = config[i].split("=");

          if (configEntry.length == 2)
          {
            System.out.println("DiamondMeter config from server: " + configEntry[0] + " = " + configEntry[1]);
            ((ItemDiamondMeter)diamondMeter); ItemDiamondMeter.serverProps.put(configEntry[0], configEntry[1]);
          }
        }

        ((ItemDiamondMeter)diamondMeter).initProps();
      }
    }
  }

  public void playerLoggedIn(Player player, ej netHandler, cg manager)
  {
    if (FMLCommonHandler.instance().getSide().equals(Side.SERVER))
    {
      String channel = "DiamondMeter";
      String out = "";

      ((ItemDiamondMeter)diamondMeter); Iterator it = ItemDiamondMeter.prop.entrySet().iterator();

      while (it.hasNext())
      {
        Map.Entry i = (Map.Entry)it.next();

        if (out.length() > 0) out = out + "\r\n";

        out = out + (String)i.getKey() + "=" + (String)i.getValue();
      }

      dk packet = new dk(channel, out.getBytes());

      manager.a(packet);
    }
  }

  public void clientLoggedIn(ej clientHandler, cg manager, dz login)
  {
    if ((ModLoader.getMinecraftInstance().C()) || (ModLoader.getMinecraftInstance().e == null))
    {
      ((ItemDiamondMeter)diamondMeter).initProps();
    }
  }

  public String connectionReceived(jf netHandler, cg manager) {
    return null;
  }

  public void connectionOpened(ej netClientHandler, String server, int port, cg manager)
  {
  }

  public void connectionOpened(ej netClientHandler, MinecraftServer server, cg manager)
  {
  }

  public void connectionClosed(cg manager)
  {
  }
}
