package pcchazter.DiamondMeter;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod$PreInit;
import cpw.mods.fml.common.Mod$ServerStarted;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
import pcchazter.DiamondMeter.CommandDiamondMeter;
import pcchazter.DiamondMeter.ItemDiamondMeter;

@NetworkMod(
   clientSideRequired = true,
   serverSideRequired = true,
   versionBounds = "[2.2,2.3)",
   channels = {"DiamondMeter"},
   packetHandler = mod_DiamondMeter.class,
   connectionHandler = mod_DiamondMeter.class
)
@Mod(
   modid = "DiamondMeter",
   name = "Diamond Meter",
   version = "2.2.2"
)
public class mod_DiamondMeter implements IPacketHandler, IConnectionHandler {

   public static Item diamondMeter;


   @Mod$PreInit
   public void load(FMLPreInitializationEvent var1) {
      diamondMeter = new ItemDiamondMeter(1028, var1.getSide().equals(Side.CLIENT));
      ModLoader.addName(diamondMeter, "Diamond Meter");
      ModLoader.addRecipe(new ItemStack(diamondMeter, 1), new Object[]{" # ", "dDd", "***", Character.valueOf('#'), Block.field_72035_aQ, Character.valueOf('d'), Item.field_77702_n, Character.valueOf('D'), Block.field_72071_ax, Character.valueOf('*'), Block.field_71981_t});
   }

   @Mod$ServerStarted
   public void serverStarted(FMLServerStartedEvent var1) {
      ((ServerCommandManager)MinecraftServer.func_71276_C().func_71187_D()).func_71560_a(new CommandDiamondMeter());
   }

   public static void command_config(String[] var0, ICommandSender var1) {
      String var3 = "DiamondMeter";
      String var4 = "";
      if(var0.length > 0) {
         Packet250CustomPayload var2;
         ItemDiamondMeter var10000;
         if(var0[0].equals("reload")) {
            var1.func_70006_a("DiamondMeter: Reloading config file");
            var10000 = (ItemDiamondMeter)diamondMeter;
            ItemDiamondMeter.prop.clear();
            ((ItemDiamondMeter)diamondMeter).loadConfig();
            if(FMLCommonHandler.instance().getSide().equals(Side.SERVER)) {
               var10000 = (ItemDiamondMeter)diamondMeter;

               Entry var6;
               for(Iterator var5 = ItemDiamondMeter.prop.entrySet().iterator(); var5.hasNext(); var4 = var4 + (String)var6.getKey() + "=" + (String)var6.getValue()) {
                  var6 = (Entry)var5.next();
                  if(var4.length() > 0) {
                     var4 = var4 + "\r\n";
                  }
               }

               var2 = new Packet250CustomPayload(var3, var4.getBytes());
               PacketDispatcher.sendPacketToAllPlayers(var2);
            }

            var1.func_70006_a("DiamondMeter: Reloading config file finished!");
         } else if(var0[0].equals("current")) {
            var1.func_70006_a("DiamondMeter: Current config:");
            StringBuilder var10001 = (new StringBuilder()).append("DiamondMeter: distanceMax: ");
            ItemDiamondMeter var10002 = (ItemDiamondMeter)diamondMeter;
            var1.func_70006_a(var10001.append(ItemDiamondMeter.prop.getProperty("distanceMax")).toString());
            var10001 = (new StringBuilder()).append("DiamondMeter: toFind: ");
            var10002 = (ItemDiamondMeter)diamondMeter;
            var1.func_70006_a(var10001.append(ItemDiamondMeter.prop.getProperty("toFind")).toString());
            var10001 = (new StringBuilder()).append("DiamondMeter: playSound: ");
            var10002 = (ItemDiamondMeter)diamondMeter;
            var1.func_70006_a(var10001.append(ItemDiamondMeter.prop.getProperty("playSound")).toString());
            var10001 = (new StringBuilder()).append("DiamondMeter: soundCloser: ");
            var10002 = (ItemDiamondMeter)diamondMeter;
            var1.func_70006_a(var10001.append(ItemDiamondMeter.prop.getProperty("soundCloser")).toString());
            var10001 = (new StringBuilder()).append("DiamondMeter: soundFurther: ");
            var10002 = (ItemDiamondMeter)diamondMeter;
            var1.func_70006_a(var10001.append(ItemDiamondMeter.prop.getProperty("soundFurther")).toString());
            var10001 = (new StringBuilder()).append("DiamondMeter: soundVolume: ");
            var10002 = (ItemDiamondMeter)diamondMeter;
            var1.func_70006_a(var10001.append(ItemDiamondMeter.prop.getProperty("soundVolume")).toString());
         } else if(var0[0].equals("set")) {
            if(var0.length == 3) {
               var10000 = (ItemDiamondMeter)diamondMeter;
               ItemDiamondMeter.prop.put(var0[1], var0[2]);
               ((ItemDiamondMeter)diamondMeter).initProps();
               ((ItemDiamondMeter)diamondMeter).saveConfig();
               if(FMLCommonHandler.instance().getSide().equals(Side.SERVER)) {
                  var4 = var4 + var0[1] + "=" + var0[2];
                  var2 = new Packet250CustomPayload(var3, var4.getBytes());
                  PacketDispatcher.sendPacketToAllPlayers(var2);
               }

               var1.func_70006_a("DiamondMeter: Set " + var0[1] + " to " + var0[2]);
            } else {
               var1.func_70006_a("Usage: /diamondmeter set [distanceMax | toFind | playSound | soundCloser | soundFurther | soundVolume] value");
            }
         } else {
            var1.func_70006_a("Usage: /diamondmeter [reload | current | set]");
         }
      } else {
         var1.func_70006_a("Usage: /diamondmeter [reload | current | set]");
      }

   }

   public void onPacketData(INetworkManager var1, Packet250CustomPayload var2, Player var3) {
      if(FMLCommonHandler.instance().getSide().equals(Side.CLIENT)) {
         String var4 = var2.field_73630_a;
         if(var4.equals("DiamondMeter")) {
            ItemDiamondMeter var10000 = (ItemDiamondMeter)diamondMeter;
            ItemDiamondMeter.serverProps.clear();
            String[] var5 = (new String(var2.field_73629_c)).split("\r\n");

            for(int var6 = 0; var6 < var5.length; ++var6) {
               String[] var7 = var5[var6].split("=");
               if(var7.length == 2) {
                  System.out.println("DiamondMeter config from server: " + var7[0] + " = " + var7[1]);
                  var10000 = (ItemDiamondMeter)diamondMeter;
                  ItemDiamondMeter.serverProps.put(var7[0], var7[1]);
               }
            }

            ((ItemDiamondMeter)diamondMeter).initProps();
         }
      }

   }

   public void playerLoggedIn(Player var1, NetHandler var2, INetworkManager var3) {
      if(FMLCommonHandler.instance().getSide().equals(Side.SERVER)) {
         String var5 = "DiamondMeter";
         String var6 = "";
         ItemDiamondMeter var10000 = (ItemDiamondMeter)diamondMeter;

         Entry var8;
         for(Iterator var7 = ItemDiamondMeter.prop.entrySet().iterator(); var7.hasNext(); var6 = var6 + (String)var8.getKey() + "=" + (String)var8.getValue()) {
            var8 = (Entry)var7.next();
            if(var6.length() > 0) {
               var6 = var6 + "\r\n";
            }
         }

         Packet250CustomPayload var4 = new Packet250CustomPayload(var5, var6.getBytes());
         var3.func_74429_a(var4);
      }

   }

   public void clientLoggedIn(NetHandler var1, INetworkManager var2, Packet1Login var3) {
      if(ModLoader.getMinecraftInstance().func_71356_B() || ModLoader.getMinecraftInstance().field_71441_e == null) {
         ((ItemDiamondMeter)diamondMeter).initProps();
      }

   }

   public String connectionReceived(NetLoginHandler var1, INetworkManager var2) {
      return null;
   }

   public void connectionOpened(NetHandler var1, String var2, int var3, INetworkManager var4) {}

   public void connectionOpened(NetHandler var1, MinecraftServer var2, INetworkManager var3) {}

   public void connectionClosed(INetworkManager var1) {}
}
