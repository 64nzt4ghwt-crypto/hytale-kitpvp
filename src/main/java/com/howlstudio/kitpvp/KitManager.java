package com.howlstudio.kitpvp;
import com.hypixel.hytale.component.Ref; import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.*; import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class KitManager {
    private final Path dataDir;
    private final Map<String,Kit> kits=new LinkedHashMap<>();
    private final Map<UUID,Map<String,Long>> cooldowns=new ConcurrentHashMap<>();
    public KitManager(Path d){this.dataDir=d;try{Files.createDirectories(d);}catch(Exception e){}loadDefaults();load();}
    public int getKitCount(){return kits.size();}
    private void loadDefaults(){
        kits.put("warrior",new Kit("warrior","Heavy melee fighter. High defense.","Iron Sword, Iron Helmet, Iron Chestplate, 16 Arrows","none",300));
        kits.put("archer",new Kit("archer","Ranged specialist. Fast movement.","Bow, 64 Arrows, Leather Armor","none",300));
        kits.put("assassin",new Kit("assassin","High damage, no armor. Risk vs reward.","Diamond Sword, Speed Potion","none",300));
        kits.put("tank",new Kit("tank","Maximum protection. Premium kit.","Diamond Armor, Iron Sword, Shield","vip",600));
    }
    public void save(){try{StringBuilder sb=new StringBuilder();for(Kit k:kits.values())sb.append(k.toConfig()).append("\n");Files.writeString(dataDir.resolve("kits.txt"),sb.toString());}catch(Exception e){}}
    private void load(){try{Path f=dataDir.resolve("kits.txt");if(!Files.exists(f))return;for(String l:Files.readAllLines(f)){Kit k=Kit.fromConfig(l);if(k!=null)kits.put(k.getName().toLowerCase(),k);}}catch(Exception e){}}
    private long getCooldownRemaining(UUID uid,String kit){Map<String,Long> m=cooldowns.getOrDefault(uid,Map.of());Long last=m.get(kit);if(last==null)return 0;Kit k=kits.get(kit);long elapsed=(System.currentTimeMillis()-last)/1000;return Math.max(0,(long)k.getCooldownSec()-elapsed);}
    private void setCooldown(UUID uid,String kit){cooldowns.computeIfAbsent(uid,k->new ConcurrentHashMap<>()).put(kit,System.currentTimeMillis());}
    public AbstractPlayerCommand getKitCommand(){
        return new AbstractPlayerCommand("kit","Select a combat kit. /kit list | /kit <name>"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String input=ctx.getInputString().trim();
                if(input.isEmpty()||input.equalsIgnoreCase("list")){
                    playerRef.sendMessage(Message.raw("=== Available Kits ==="));
                    for(Kit k:kits.values()){String tag=k.isPermRequired()?"§6["+k.getPermission()+"]§r ":"";playerRef.sendMessage(Message.raw("  /kit "+k.getName()+" — "+tag+k.getDescription()));}
                    return;
                }
                Kit k=kits.get(input.toLowerCase());
                if(k==null){playerRef.sendMessage(Message.raw("[Kit] Unknown kit. /kit list"));return;}
                long cd=getCooldownRemaining(playerRef.getUuid(),k.getName().toLowerCase());
                if(cd>0){playerRef.sendMessage(Message.raw("[Kit] §c"+k.getName()+"§r on cooldown: "+cd+"s"));return;}
                setCooldown(playerRef.getUuid(),k.getName().toLowerCase());
                playerRef.sendMessage(Message.raw("[Kit] §6"+k.getName()+"§r equipped! Contents: §7"+k.getContents()));
                playerRef.sendMessage(Message.raw("[Kit] Cooldown: §e"+k.getCooldownSec()+"s"));
            }
        };
    }
    public AbstractPlayerCommand getKitAdminCommand(){
        return new AbstractPlayerCommand("kitadmin","[Admin] Manage kits. /kitadmin list|remove <name>|reload"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String[]args=ctx.getInputString().trim().split("\\s+",2);
                String sub=args.length>0?args[0].toLowerCase():"list";
                switch(sub){
                    case"list"->{playerRef.sendMessage(Message.raw("[Kits] "+kits.size()+" kits: "+String.join(", ",kits.keySet())));}
                    case"remove"->{if(args.length<2)break;if(kits.remove(args[1])!=null){save();playerRef.sendMessage(Message.raw("[Kits] Removed: "+args[1]));}else{playerRef.sendMessage(Message.raw("[Kits] Not found: "+args[1]));}}
                    case"reload"->{kits.clear();loadDefaults();load();playerRef.sendMessage(Message.raw("[Kits] Reloaded. "+kits.size()+" kits."));}
                    default->playerRef.sendMessage(Message.raw("Usage: /kitadmin list|remove <name>|reload"));
                }
            }
        };
    }
}
