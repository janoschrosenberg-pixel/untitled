package editor;

import stackmachine.Inter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Menu {

     protected Inter inter;
     protected Map<String, String> menufunctions = new HashMap<>();

     public abstract void fireMenuCommand(String command);

     public abstract void paint(Graphics g);

    public void registerFunction(String name, String function) {
        menufunctions.put(name, function);
    }
    public void setInter(Inter inter) {
        this.inter = inter;
    }
}
