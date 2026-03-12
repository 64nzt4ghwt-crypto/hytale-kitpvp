package com.howlstudio.kitpvp;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
public class KitListener {
    private final KitManager mgr;
    public KitListener(KitManager m){this.mgr=m;}
    public void register(){
        HytaleServer.get().getEventBus().registerGlobal(PlayerReadyEvent.class,e->{
            Player p=e.getPlayer();if(p==null)return;
            PlayerRef ref=p.getPlayerRef();if(ref==null)return;
            ref.sendMessage(com.hypixel.hytale.server.core.Message.raw("[Kit] Type /kits to view available kits."));
        });
    }
}
