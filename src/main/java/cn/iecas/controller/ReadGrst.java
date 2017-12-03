package cn.iecas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping(value = "/imggrst", method = RequestMethod.GET)
public class ReadGrst {

    private static final String GRST_FILE_PATH = "/Users/vanishrain/Desktop/1_0_0_IMG_GE.grst";
    @RequestMapping(value = "/{level}/{row}/{col}", method = RequestMethod.GET)
    public void printHello(@PathVariable int level,@PathVariable int row,@PathVariable int col,HttpServletResponse response) {
        String tileIndex = level + "-" + row + "-" + col;
        ReadFromGrst readFromGrst  = new ReadFromGrst(GRST_FILE_PATH);
        byte[] tile = readFromGrst.getTile(tileIndex);
        response.setContentType("image/png");
        try {
            OutputStream stream = response.getOutputStream();
            stream.write(tile);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}