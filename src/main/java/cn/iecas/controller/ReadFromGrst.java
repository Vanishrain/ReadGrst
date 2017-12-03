package cn.iecas.controller;

import cn.iecas.controller.GrstInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class ReadFromGrst {
    private GrstInfo grstInfo;
    private String grstFileName;
    final private Logger log = LoggerFactory.getLogger(ReadFromGrst.class);



    public ReadFromGrst(String grstFile){
        this.grstFileName = grstFile;
        grstInfo = new GrstInfo();
        Initialization();

    }

    /**
     * 从grst文件名读取grst信息
     */
    private void readInfoFromGrstFileName(){
        File file = new File(this.grstFileName);
        String[] infoFromName = file.getName().split("[_.]");
        this.grstInfo.setLevel(Integer.parseInt(infoFromName[0]));
        this.grstInfo.setRow(Integer.parseInt(infoFromName[1]));
        this.grstInfo.setCol(Integer.parseInt(infoFromName[2]));
        this.grstInfo.setLength(file.length());
    }


    /**
     * grst文件初始化
     * @return
     */
    private boolean Initialization(){
        RandomAccessFile grstFile = null;
        try {
            grstFile = new RandomAccessFile(this.grstFileName,"r");
            readInfoFromGrstFileName();
            this.grstInfo.setMinLevel(Integer.reverseBytes(grstFile.readInt()));
            this.grstInfo.setMaxLevel(Integer.reverseBytes(grstFile.readInt()));
            this.grstInfo.setMinLon(Double.longBitsToDouble(Long.reverseBytes(grstFile.readLong())));
            this.grstInfo.setMaxLon(Double.longBitsToDouble(Long.reverseBytes(grstFile.readLong())));
            this.grstInfo.setMinLat(Double.longBitsToDouble(Long.reverseBytes(grstFile.readLong())));
            this.grstInfo.setMaxLat(Double.longBitsToDouble(Long.reverseBytes(grstFile.readLong())));
            CaculateTilePosition(grstFile);
            return true;
        } catch (FileNotFoundException e) {
            log.error("grst文件：{}打开错误",this.grstFileName);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            log.error("读取grst文件:{}信息错误",this.grstFileName);
            e.printStackTrace();
            return false;
        }finally {
            if(grstFile!=null)
                try {
                    grstFile.close();
                    log.info("grst文件:{}初始化完毕",this.grstFileName);
                } catch (IOException e) {
                    log.error("关闭grst文件:{}错误",grstFileName);
                    e.printStackTrace();
                }
        }
    }



    private void CaculateTilePosition(RandomAccessFile grstFile) throws IOException {
        long seekLength,pos;
        int minCol,maxCol,minRow,maxRow;
        int minlevel = this.grstInfo.getMinLevel();
        int maxlevel = this.grstInfo.getMaxLevel();
        double minLon = this.grstInfo.getMinLon();
        double minLat = this.grstInfo.getMinLat();
        double maxLon = this.grstInfo.getMaxLon();
        double maxLat = this.grstInfo.getMaxLat();

        for (int k = minlevel; k <= maxlevel; k++) {
            long bandoffset = 0;

            for (int j = maxlevel; j > k; j--) {
                double tileSize = (double) 180 / (1 << j);
                minCol = (int) Math.floor((minLon + 180) / tileSize);
                maxCol = (int) Math.ceil((maxLon + 180) / tileSize) - 1;
                minRow = (int) Math.floor((minLat + 90) / tileSize);
                maxRow = (int) Math.ceil((maxLat + 90) / tileSize) - 1;
                bandoffset += (maxRow - minRow + 1) * (maxCol - minCol + 1) * 12;
            }

            double tileSize = (double) 180 / (1 << k);
            minCol = (int) Math.floor((minLon + 180) / tileSize);
            maxCol = (int) Math.ceil((maxLon + 180) / tileSize) - 1;
            minRow = (int) Math.floor((minLat + 90) / tileSize);
            maxRow = (int) Math.ceil((maxLat + 90) / tileSize) - 1;

            for (int n = minRow; n <= maxRow; n++) {
                for (int m = minCol; m <= maxCol; m++) {
                    seekLength = 1024 + bandoffset + ((n - minRow)
                            * (maxCol - minCol + 1) + m - minCol) * 12;

                    grstFile.seek(seekLength);
                    pos = Long.reverseBytes(grstFile.readLong());
                    int posoffset = Integer.reverseBytes(grstFile.readInt());

                    if (posoffset <= 0 || grstFile.length() < pos) {
                        continue;
                    }
                    String tileIndex = k + "-" + n + "-" + m;
                    GrstTileInfo grstTileInfo = new GrstTileInfo();
                    grstTileInfo.setLevel(k);
                    grstTileInfo.setRow(n);
                    grstTileInfo.setColumn(m);
                    grstTileInfo.setOffSet(pos);
                    grstTileInfo.setSize(posoffset);
                    this.grstInfo.addTileIndex(tileIndex,grstTileInfo);

                }
            }
        }
    }

    public byte[] getTile(String tileIndex){
        GrstTileInfo grstTileInfo = this.grstInfo.getTileIndex(tileIndex);
        if (grstTileInfo ==null)
            return null;
        byte [] tileBuffer = new byte[grstTileInfo.getSize()];

        try {
            RandomAccessFile grstFile = new RandomAccessFile(this.grstFileName,"r");
            grstFile.seek(grstTileInfo.getOffSet());
            grstFile.read(tileBuffer,0, grstTileInfo.getSize());
            return tileBuffer;
        } catch (FileNotFoundException e) {
            log.error("读取grst文件:{}错误",grstFileName);
            e.printStackTrace();
        }finally {
            return tileBuffer;
        }
    }

}
