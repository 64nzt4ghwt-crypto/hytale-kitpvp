package com.howlstudio.kitpvp;
public class Kit {
    private final String id,name,description,permission;
    private final int cooldownMin;
    public Kit(String id,String name,String description,String permission,int cooldownMin){
        this.id=id;this.name=name;this.description=description;this.permission=permission;this.cooldownMin=cooldownMin;
    }
    public String getId(){return id;} public String getName(){return name;}
    public String getDescription(){return description;} public String getPermission(){return permission;}
    public int getCooldownMin(){return cooldownMin;}
    public String toConfig(){return id+"|"+name+"|"+description+"|"+permission+"|"+cooldownMin;}
    public static Kit fromConfig(String s){String[]p=s.split("\\|",5);return p.length>=5?new Kit(p[0],p[1],p[2],p[3],Integer.parseInt(p[4])):null;}
}
