package pcchazter.DiamondMeter;

import ab;
import x;

public class CommandDiamondMeter extends x
{
  public String c()
  {
    return "diamondmeter";
  }

  public String a(ab par1ICommandSender)
  {
    return "/" + c() + " [reload | current | set]";
  }

  public void b(ab sender, String[] command)
  {
    if ((command.length > 0) && (sender.a(0, c())))
    {
      mod_DiamondMeter.command_config(command, sender);
    }
    else
    {
      sender.a("Usage: " + a(sender));
    }
  }
}
