package pcchazter.DiamondMeter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class ItemDiamondMeter extends Item {

   private static Long lastUpdate = Long.valueOf(0L);
   private static Long millisPerUpdate = Long.valueOf(100L);
   private static boolean isClient;
   private static int distanceMax;
   private static int toFind;
   private static String soundCloser;
   private static String soundFurther;
   private static Float soundVolume;
   private static Boolean playSound;
   private static String configComment;
   private static int distancePerLevel;
   private static Icon[] iconIndexes;
   private static int distanceShortest = -1;
   private static int lastState = 0;
   private static ArrayList found = new ArrayList();
   private static int[] vectorShortest = new int[3];
   private static File configFile;
   public static Properties prop = new Properties();
   protected static Properties serverProps = new Properties();


   protected ItemDiamondMeter(int var1, boolean var2) {
      super(var1);
      this.field_77777_bU = 1;
      isClient = var2;
      this.func_77637_a(CreativeTabs.field_78040_i);
      if(isClient) {
         configFile = new File(Minecraft.func_71380_b(), "DiamondMeter.txt");
         configComment = "Diamond Meter Config";
         iconIndexes = new Icon[5];
      } else {
         configFile = new File("DiamondMeter_Server.txt");
         configComment = "Diamond Meter Server Config";
      }

      this.loadConfig();
   }

   @SideOnly(Side.CLIENT)
   public void func_94581_a(IconRegister var1) {
      for(int var2 = 0; var2 <= 4; ++var2) {
         iconIndexes[var2] = var1.func_94245_a("DiamondMeter:diamond_meter" + var2);
      }

      this.field_77791_bV = iconIndexes[0];
   }

   protected void loadConfig() {
      try {
         if((configFile.exists() || configFile.createNewFile()) && configFile.isFile() && configFile.canRead()) {
            FileReader var1 = new FileReader(configFile);
            prop.load(var1);
            var1.close();
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      this.initProps();
      this.saveConfig();
   }

   private String getProperty(String var1, String var2) {
      return this.getProperty(var1, var2, 0);
   }

   private String getProperty(String var1, String var2, int var3) {
      String var4 = var2;
      if(isClient && !ModLoader.getMinecraftInstance().func_71356_B() && ModLoader.getMinecraftInstance().field_71441_e != null) {
         try {
            if(serverProps.get(var1) != null) {
               var4 = (String)serverProps.get(var1);
            }
         } catch (NumberFormatException var8) {
            ;
         }
      } else {
         try {
            if(prop.get(var1) != null) {
               var4 = (String)prop.get(var1);
            }
         } catch (NumberFormatException var9) {
            ;
         }
      }

      switch(var3) {
      case 0:
      default:
         break;
      case 1:
         try {
            Integer.parseInt(var4);
         } catch (NumberFormatException var7) {
            var7.printStackTrace();
            var4 = var2;
         }
         break;
      case 2:
         try {
            Float.parseFloat(var4);
         } catch (NumberFormatException var6) {
            var6.printStackTrace();
            var4 = var2;
         }
      }

      if(!isClient || ModLoader.getMinecraftInstance().func_71356_B() || ModLoader.getMinecraftInstance().field_71441_e == null) {
         prop.put(var1, var4);
      }

      return var4;
   }

   protected void initProps() {
      distanceMax = Integer.valueOf(this.getProperty("distanceMax", String.valueOf(8), 1)).intValue();
      toFind = Integer.valueOf(this.getProperty("toFind", String.valueOf(Block.field_72073_aw.field_71990_ca), 1)).intValue();
      playSound = Boolean.valueOf(this.getProperty("playSound", "false"));
      soundCloser = this.getProperty("soundCloser", "random.pop");
      soundFurther = this.getProperty("soundFurther", "note.bd");
      soundVolume = Float.valueOf(this.getProperty("soundVolume", "1.0", 2));
      distancePerLevel = distanceMax / 4;
      if(distancePerLevel < 1) {
         distancePerLevel = 1;
      }

      System.out.println("DiamondMeter: Loaded " + (isClient && (ModLoader.getMinecraftInstance().func_71356_B() || ModLoader.getMinecraftInstance().field_71441_e == null)?"Client":"Server") + " config.");
      if(isClient) {
         Minecraft var1 = ModLoader.getMinecraftInstance();
         this.update(var1);
      }

   }

   protected void saveConfig() {
      if(!isClient || ModLoader.getMinecraftInstance().func_71356_B() || ModLoader.getMinecraftInstance().field_71441_e == null) {
         try {
            if(!configFile.exists() && !configFile.createNewFile() || !configFile.isFile()) {
               throw new IOException("Could not init " + configFile.getAbsolutePath());
            }

            if(!configFile.canWrite() || !configFile.canRead()) {
               throw new IOException("Wrong permissions on " + configFile.getAbsolutePath());
            }

            FileWriter var1 = new FileWriter(configFile);
            prop.store(var1, configComment);
            var1.close();
         } catch (Exception var2) {
            var2.printStackTrace();
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public void func_77663_a(ItemStack var1, World var2, Entity var3, int var4, boolean var5) {
      Minecraft var6 = ModLoader.getMinecraftInstance();
      this.update(var6);
   }

   @SideOnly(Side.CLIENT)
   public void update(Minecraft var1) {
      if((new Date()).getTime() > lastUpdate.longValue() + millisPerUpdate.longValue()) {
         lastUpdate = Long.valueOf((new Date()).getTime());
         found.clear();
         distanceShortest = -1;
         EntityClientPlayerMP var2 = var1.field_71439_g;
         WorldClient var3 = var1.field_71441_e;
         if(var2 != null && var3 != null) {
            double var4 = var2.field_70165_t;
            double var6 = var2.field_70163_u;
            double var8 = var2.field_70161_v;
            int var10 = (int)var4 - distanceMax - 1;
            int var11 = (int)var6 - distanceMax;
            int var12 = (int)var8 - distanceMax;
            int var13 = (int)var4 + distanceMax;
            int var14 = (int)var6 + distanceMax;
            int var15 = (int)var8 + distanceMax + 1;

            int var16;
            for(var16 = var12; var16 < var15; ++var16) {
               for(int var17 = var10; var17 < var13; ++var17) {
                  for(int var18 = var11; var18 < var14; ++var18) {
                     if(var3.func_72798_a(var17, var18, var16) == toFind) {
                        found.add(new int[]{var17, var18, var16});
                     }
                  }
               }
            }

            for(var16 = 0; var16 < found.size(); ++var16) {
               int[] var28 = (int[])((int[])found.get(var16));
               double var29 = (double)var28[0] - var4;
               double var20 = (double)var28[1] - var6 + 1.0D;
               double var22 = (double)var28[2] - var8;
               var29 += var29 > 0.0D?1.0D:0.0D;
               var22 += var22 > 0.0D?1.0D:0.0D;
               double var24 = Math.sqrt(Math.pow(var29, 2.0D) + Math.pow(var22, 2.0D));
               double var26 = Math.sqrt(Math.pow(var24, 2.0D) + Math.pow(var20, 2.0D));
               if((int)var26 > distanceMax) {
                  found.remove(var16);
                  --var16;
               } else if((double)distanceShortest > var26 || distanceShortest == -1) {
                  distanceShortest = (int)var26;
                  vectorShortest = new int[]{var28[0], var28[1], var28[2]};
               }
            }

            if(distanceShortest > -1) {
               var16 = (distanceMax - distanceShortest + 1) / distancePerLevel;
               if(distanceMax < 4) {
                  var16 += 4 - distanceMax;
               }

               switch(var16) {
               case 0:
               case 1:
                  this.field_77791_bV = iconIndexes[1];
                  lastState = this.playDistanceSound(var1, vectorShortest, 1, lastState);
                  break;
               case 2:
                  this.field_77791_bV = iconIndexes[2];
                  lastState = this.playDistanceSound(var1, vectorShortest, 2, lastState);
                  break;
               case 3:
                  this.field_77791_bV = iconIndexes[3];
                  lastState = this.playDistanceSound(var1, vectorShortest, 3, lastState);
                  break;
               case 4:
                  this.field_77791_bV = iconIndexes[4];
                  lastState = this.playDistanceSound(var1, vectorShortest, 4, lastState);
               }
            } else {
               this.field_77791_bV = iconIndexes[0];
               lastState = this.playDistanceSound(var1, vectorShortest, 0, lastState);
            }

         }
      }
   }

   @SideOnly(Side.CLIENT)
   private int playDistanceSound(Minecraft var1, int[] var2, int var3, int var4) {
      EntityClientPlayerMP var5 = var1.field_71439_g;
      WorldClient var6 = var1.field_71441_e;
      float var7 = 1.0F;
      boolean var8 = false;
      String var9 = "";
      if(var4 < var3) {
         var7 = (float)(var3 * 3) / 10.0F + 0.8F;
         var9 = soundCloser;
         var8 = true;
      } else if(var4 > var3) {
         var7 = (float)(var3 * 3) / 10.0F + 0.8F;
         var9 = soundFurther;
         var8 = true;
      }

      if(!var5.field_71071_by.func_70450_e(this.field_77779_bT)) {
         var8 = false;
      }

      if(var8 && playSound.booleanValue() && var9 != "") {
         var6.func_72980_b(var5.field_70165_t, var5.field_70163_u, var5.field_70161_v, var9, soundVolume.floatValue(), var7, false);
      }

      return var3;
   }

   @SideOnly(Side.CLIENT)
   public ItemStack func_77659_a(ItemStack var1, World var2, EntityPlayer var3) {
      prop.clear();
      this.loadConfig();
      return var1;
   }

   @SideOnly(Side.CLIENT)
   public boolean onDroppedByPlayer(ItemStack var1, EntityPlayer var2) {
      return true;
   }

}
