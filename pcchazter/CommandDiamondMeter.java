package pcchazter.DiamondMeter;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import pcchazter.DiamondMeter.mod_DiamondMeter;

public class CommandDiamondMeter extends CommandBase {

   public String func_71517_b() {
      return "diamondmeter";
   }

   public String func_71518_a(ICommandSender var1) {
      return "/" + this.func_71517_b() + " [reload | current | set]";
   }

   public void func_71515_b(ICommandSender var1, String[] var2) {
      if(var2.length > 0 && var1.func_70003_b(0, this.func_71517_b())) {
         mod_DiamondMeter.command_config(var2, var1);
      } else {
         var1.func_70006_a("Usage: " + this.func_71518_a(var1));
      }

   }
}
