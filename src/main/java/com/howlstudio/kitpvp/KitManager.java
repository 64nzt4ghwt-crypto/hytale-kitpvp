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
        kits.put("starter",new Kit("starter","Starter","Basic starter kit for new players","",0));
        kits.put("warrior",new Kit("warrior","Warrior","Heavy combat kit with armor and sword","",30));
        kits.put("archer",new Kit("archer","Archer","Ranged kit with bow and arrows","",30));
        kits.put("vip",new Kit("vip","VIP","Premium kit for VIP players","vip",60));
    }
    public boolean isOnCooldown(UUID uid,String kitId){
        Map<String,Long> cd=cooldowns.get(uid);if(cd==null)return false;
        Long last=cd.get(kitId);if(last==null)return false;
        Kit k=kits.get(kitId);if(k==null)return false;
        return System.currentTimeMillis()-last<k.getCooldownMin()*60_000L;
    }
    public long secondsLeft(UUID uid,String kitId){
        Map<String,Long> cd=cooldowns.get(uid);if(cd==null)return 0;
        Long last=cd.get(kitId);if(last==null)return 0;
        Kit k=kits.get(kitId);if(k==null)return 0;
        return Math.max(0,(k.getCooldownMin()*60_000L-(System.currentTimeMillis()-last))/1000);
    }
    public void applyKit(UUID uid,String kitId){cooldowns.computeIfAbsent(uid,k->new HashMap<>()).put(kitId,System.currentTimeMillis());}
    public void save(){try{StringBuilder sb=new StringBuilder();for(Kit k:kits.values())sb.append(k.toConfig()).append("\n");Files.writeString(dataDir.resolve("kits.txt"),sb.toString());}catch(Exception e){}}
    private void load(){try{Path f=dataDir.resolve("kits.txt");if(!Files.exists(f))return;kits.clear();for(String l:Files.readAllLines(f)){Kit k=Kit.fromConfig(l);if(k!=null)kits.put(k.getId(),k);}}catch(Exception e){}}
    public AbstractPlayerCommand getKitsCommand(){
        return new AbstractPlayerCommand("kits","List available kits. /kits"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> s,Ref<EntityStore> r,PlayerRef pr,World w){
                pr.sendMessage(Message.raw("=== Available Kits ==="));
                for(Kit k:kits.values()){
                    String cd=k.getCooldownMin()==0?"no cooldown":k.getCooldownMin()+"m cooldown";
                    String status=isOnCooldown(pr.getUuid(),k.getId())?"§c("+secondsLeft(pr.getUuid(),k.getId())+"s)§r":"§a(ready)§r";
                    pr.sendMessage(Message.raw("  §6/kit "+k.getId()+"§r — "+k.getName()+": "+k.getDescription()+" ["+cd+"] "+status));
                }
            }
        };
    }
    public AbstractPlayerCommand getKitCommand(){
        return new AbstractPlayerCommand("kit","Select a kit. /kit <id>"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> s,Ref<EntityStore> r,PlayerRef pr,World w){
                String id=ctx.getInputString().trim().toLowerCase();
                if(id.isEmpty()){pr.sendMessage(Message.raw("Usage: /kit <id> — /kits to list"));return;}
                Kit k=kits.get(id);if(k==null){pr.sendMessage(Message.raw("[Kit] Unknown kit: "+id));return;}
                if(isOnCooldown(pr.getUuid(),id)){pr.sendMessage(Message.raw("[Kit] Cooldown: "+secondsLeft(pr.getUuid(),id)+"s remaining."));return;}
                applyKit(pr.getUuid(),id);
                pr.sendMessage(Message.raw("[Kit] §6"+k.getName()+"§r applied!"+(k.getCooldownMin()>0?" ("+k.getCooldownMin()+"m cooldown)":"")));
                System.out.println("[KitPvP] "+pr.getUsername()+" selected kit: "+id);
            }
        };
    }
    public AbstractPlayerCommand getKitAdminCommand(){
        return new AbstractPlayerCommand("kitadmin","[Admin] Create/delete kits. /kitadmin create|delete|reload"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> s,Ref<EntityStore> r,PlayerRef pr,World w){
                String[]args=ctx.getInputString().trim().split("\\s+",5);
                String sub=args.length>0?args[0].toLowerCase():"help";
                if(sub.equals("reload")){load();pr.sendMessage(Message.raw("[Kit] Reloaded "+kits.size()+" kits."));}
                else if(sub.equals("delete")&&args.length>1){kits.remove(args[1]);save();pr.sendMessage(Message.raw("[Kit] Deleted: "+args[1]));}
                else if(sub.equals("create")&&args.length>=4){
                    int cd=0;try{if(args.length>4)cd=Integer.parseInt(args[4]);}catch(Exception e){}
                    kits.put(args[1].toLowerCase(),new Kit(args[1].toLowerCase(),args[2],args[3],"",cd));save();
                    pr.sendMessage(Message.raw("[Kit] Created: "+args[2]));
                }else pr.sendMessage(Message.raw("Usage: /kitadmin create <id> <name> <desc> [cooldown_min] | delete <id> | reload"));
            }
        };
    }
}
