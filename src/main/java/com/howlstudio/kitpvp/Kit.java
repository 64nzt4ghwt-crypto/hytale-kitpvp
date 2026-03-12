package com.howlstudio.kitpvp;
public class Kit {
    private final String name, description, contents, permission;
    private final int cooldownSec;
    public Kit(String name,String description,String contents,String permission,int cooldownSec){
        this.name=name;this.description=description;this.contents=contents;this.permission=permission;this.cooldownSec=cooldownSec;
    }
    public String getName(){return name;} public String getDescription(){return description;}
    public String getContents(){return contents;} public String getPermission(){return permission;}
    public int getCooldownSec(){return cooldownSec;}
    public boolean isPermRequired(){return !permission.isBlank()&&!permission.equals("none");}
    public String toConfig(){return name+"|"+description+"|"+contents+"|"+permission+"|"+cooldownSec;}
    public static Kit fromConfig(String s){String[]p=s.split("\\|",5);return p.length>=5?new Kit(p[0],p[1],p[2],p[3],Integer.parseInt(p[4])):null;}
}
