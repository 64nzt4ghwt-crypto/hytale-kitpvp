package com.howlstudio.kitpvp;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
/** KitPvP — Combat kit selection with cooldown. /kit select, admin kit management. */
public final class KitPvPPlugin extends JavaPlugin {
    private KitManager mgr;
    public KitPvPPlugin(JavaPluginInit init){super(init);}
    @Override protected void setup(){
        System.out.println("[KitPvP] Loading...");
        mgr=new KitManager(getDataDirectory());
        CommandManager cmd=CommandManager.get();
        cmd.register(mgr.getKitCommand());
        cmd.register(mgr.getKitAdminCommand());
        System.out.println("[KitPvP] Ready. "+mgr.getKitCount()+" kits.");
    }
    @Override protected void shutdown(){if(mgr!=null)mgr.save();System.out.println("[KitPvP] Stopped.");}
}
