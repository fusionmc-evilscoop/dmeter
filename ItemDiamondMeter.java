package pcchazter.DiamondMeter;

import ModLoader;
import aab;
import apa;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import lx;
import ly;
import mp;
import net.minecraft.client.Minecraft;
import so;
import sq;
import ve;
import wk;
import wm;

public class ItemDiamondMeter extends wk
{
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
  private static lx[] iconIndexes;
  private static int distanceShortest = -1;
  private static int lastState = 0;

  private static ArrayList found = new ArrayList();
  private static int[] vectorShortest = new int[3];
  private static File configFile;
  public static Properties prop = new Properties();

  protected static Properties serverProps = new Properties();

  protected ItemDiamondMeter(int par1, boolean client)
  {
    super(par1);
    this.cq = 1;
    isClient = client;
    a(ve.i);

    if (isClient)
    {
      configFile = new File(Minecraft.b(), "DiamondMeter.txt");
      configComment = "Diamond Meter Config";

      iconIndexes = new lx[5];
    }
    else
    {
      configFile = new File("DiamondMeter_Server.txt");
      configComment = "Diamond Meter Server Config";
    }

    loadConfig();
  }

  @SideOnly(Side.CLIENT)
  public void a(ly par1IconRegister)
  {
    for (int i = 0; i <= 4; i++)
    {
      iconIndexes[i] = par1IconRegister.a(new StringBuilder().append("DiamondMeter:diamond_meter").append(i).toString());
    }

    this.ct = iconIndexes[0];
  }

  protected void loadConfig()
  {
    try
    {
      if (((configFile.exists()) || (configFile.createNewFile())) && (configFile.isFile()))
      {
        if (configFile.canRead())
        {
          FileReader reader = new FileReader(configFile);
          prop.load(reader);
          reader.close();
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    initProps();
    saveConfig();
  }

  private String getProperty(String name, String defaultValue)
  {
    return getProperty(name, defaultValue, 0);
  }

  private String getProperty(String name, String defaultValue, int type)
  {
    String out = defaultValue;

    if ((!isClient) || (ModLoader.getMinecraftInstance().C()) || (ModLoader.getMinecraftInstance().e == null))
      try {
        if (prop.get(name) != null) out = (String)prop.get(name); 
      }
      catch (NumberFormatException e) {
      }
    else try { if (serverProps.get(name) != null) out = (String)serverProps.get(name); 
      }
      catch (NumberFormatException e)
      {
      }
    switch (type)
    {
    case 0:
      break;
    case 1:
      try { Integer.parseInt(out);
      } catch (NumberFormatException e)
      {
        e.printStackTrace();
        out = defaultValue;
      }
    case 2:
      try {
        Float.parseFloat(out);
      }
      catch (NumberFormatException e) {
        e.printStackTrace();
        out = defaultValue;
      }

    }

    if ((!isClient) || (ModLoader.getMinecraftInstance().C()) || (ModLoader.getMinecraftInstance().e == null))
    {
      prop.put(name, out);
    }

    return out;
  }

  protected void initProps()
  {
    distanceMax = Integer.valueOf(getProperty("distanceMax", String.valueOf(8), 1)).intValue();
    toFind = Integer.valueOf(getProperty("toFind", String.valueOf(apa.aA.cz), 1)).intValue();

    playSound = Boolean.valueOf(getProperty("playSound", "false"));
    soundCloser = getProperty("soundCloser", "random.pop");
    soundFurther = getProperty("soundFurther", "note.bd");
    soundVolume = Float.valueOf(getProperty("soundVolume", "1.0", 2));

    distancePerLevel = distanceMax / 4;

    if (distancePerLevel < 1) distancePerLevel = 1;

    System.out.println(new StringBuilder().append("DiamondMeter: Loaded ").append((isClient) && ((ModLoader.getMinecraftInstance().C()) || (ModLoader.getMinecraftInstance().e == null)) ? "Client" : "Server").append(" config.").toString());

    if (isClient)
    {
      Minecraft mc = ModLoader.getMinecraftInstance();
      update(mc);
    }
  }

  protected void saveConfig()
  {
    if ((isClient) && (!ModLoader.getMinecraftInstance().C()) && (ModLoader.getMinecraftInstance().e != null)) return;

    try
    {
      if (((configFile.exists()) || (configFile.createNewFile())) && (configFile.isFile()))
      {
        if ((configFile.canWrite()) && (configFile.canRead()))
        {
          FileWriter writer = new FileWriter(configFile);
          prop.store(writer, configComment);
          writer.close();
        }
        else
        {
          throw new IOException(new StringBuilder().append("Wrong permissions on ").append(configFile.getAbsolutePath()).toString());
        }
      }
      else
      {
        throw new IOException(new StringBuilder().append("Could not init ").append(configFile.getAbsolutePath()).toString());
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @SideOnly(Side.CLIENT)
  public void a(wm par1ItemStack, aab par2World, mp par3Entity, int par4, boolean par5)
  {
    Minecraft mc = ModLoader.getMinecraftInstance();
    update(mc);
  }

  @SideOnly(Side.CLIENT)
  public void update(Minecraft mc)
  {
    if (new Date().getTime() <= lastUpdate.longValue() + millisPerUpdate.longValue()) return;

    lastUpdate = Long.valueOf(new Date().getTime());

    found.clear();
    distanceShortest = -1;

    sq player = mc.g;
    aab world = mc.e;

    if ((player == null) || (world == null)) return;

    double cur_x = player.u;
    double cur_y = player.v;
    double cur_z = player.w;

    int min_x = (int)cur_x - distanceMax - 1;
    int min_y = (int)cur_y - distanceMax;
    int min_z = (int)cur_z - distanceMax;

    int max_x = (int)cur_x + distanceMax;
    int max_y = (int)cur_y + distanceMax;
    int max_z = (int)cur_z + distanceMax + 1;

    for (int z1 = min_z; z1 < max_z; z1++)
    {
      for (int x1 = min_x; x1 < max_x; x1++)
      {
        for (int y1 = min_y; y1 < max_y; y1++)
        {
          if (world.a(x1, y1, z1) == toFind)
          {
            found.add(new int[] { x1, y1, z1 });
          }
        }
      }

    }

    for (int i = 0; i < found.size(); i++)
    {
      int[] block = (int[])found.get(i);

      double distanceX = block[0] - cur_x;
      double distanceY = block[1] - cur_y + 1.0D;
      double distanceZ = block[2] - cur_z;

      distanceX += (distanceX > 0.0D ? 1.0D : 0.0D);
      distanceZ += (distanceZ > 0.0D ? 1.0D : 0.0D);

      double distance2D = Math.sqrt(Math.pow(distanceX, 2.0D) + Math.pow(distanceZ, 2.0D));
      double distance3D = Math.sqrt(Math.pow(distance2D, 2.0D) + Math.pow(distanceY, 2.0D));

      if ((int)distance3D > distanceMax)
      {
        found.remove(i);
        i--;
      }
      else if ((distanceShortest > distance3D) || (distanceShortest == -1))
      {
        distanceShortest = (int)distance3D;
        vectorShortest = new int[] { block[0], block[1], block[2] };
      }
    }

    if (distanceShortest > -1)
    {
      int level = (distanceMax - distanceShortest + 1) / distancePerLevel;

      if (distanceMax < 4)
      {
        level += 4 - distanceMax;
      }

      switch (level)
      {
      case 0:
      case 1:
        this.ct = iconIndexes[1];
        lastState = playDistanceSound(mc, vectorShortest, 1, lastState);
        break;
      case 2:
        this.ct = iconIndexes[2];
        lastState = playDistanceSound(mc, vectorShortest, 2, lastState);
        break;
      case 3:
        this.ct = iconIndexes[3];
        lastState = playDistanceSound(mc, vectorShortest, 3, lastState);
        break;
      case 4:
        this.ct = iconIndexes[4];
        lastState = playDistanceSound(mc, vectorShortest, 4, lastState);
      }

    }
    else
    {
      this.ct = iconIndexes[0];
      lastState = playDistanceSound(mc, vectorShortest, 0, lastState);
    }
  }

  @SideOnly(Side.CLIENT)
  private int playDistanceSound(Minecraft mc, int[] vector, int curState, int lastState)
  {
    sq player = mc.g;
    aab world = mc.e;

    float pitch = 1.0F;
    boolean play = false;

    String soundPlayed = "";

    if (lastState < curState)
    {
      pitch = curState * 3 / 10.0F + 0.8F;
      soundPlayed = soundCloser;
      play = true;
    }
    else if (lastState > curState)
    {
      pitch = curState * 3 / 10.0F + 0.8F;
      soundPlayed = soundFurther;
      play = true;
    }

    if (!player.bK.e(this.cp)) play = false;

    if ((play) && (playSound.booleanValue()) && (soundPlayed != ""))
    {
      world.a(player.u, player.v, player.w, soundPlayed, soundVolume.floatValue(), pitch, false);
    }

    return curState;
  }

  @SideOnly(Side.CLIENT)
  public wm a(wm itemStackIn, aab world, sq player)
  {
    prop.clear();
    loadConfig();
    return itemStackIn;
  }

  @SideOnly(Side.CLIENT)
  public boolean onDroppedByPlayer(wm item, sq player)
  {
    return true;
  }
}
