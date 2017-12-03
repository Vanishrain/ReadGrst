package cn.iecas.controller;

import lombok.Data;

import java.util.*;


@Data
public class GrstInfo {

    private int row;
    private int col;
    private int level;
    private long length;
    private int minLevel;
    private int maxLevel;
    private double minLon;
    private double maxLon;
    private double minLat;
    private double maxLat;
    private Map<String, GrstTileInfo> grstTileInfoIndex = new HashMap<String, GrstTileInfo>();

    public void addTileIndex(String index, GrstTileInfo grstTileInfo){
        if(grstTileInfo!=null&&index!=null)
            this.grstTileInfoIndex.put(index,grstTileInfo);
    }

    public void deleteTileIndex(String index){
        if(index!=null)
            this.grstTileInfoIndex.remove(index);
    }

    public GrstTileInfo getTileIndex(String index){
            return this.grstTileInfoIndex.get(index);
    }



}
