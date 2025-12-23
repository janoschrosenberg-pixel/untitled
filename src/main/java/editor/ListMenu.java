package editor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ListMenu extends Menu{
    private List<String> elems = new ArrayList<>();
    private Font font = new Font("Monospaced", Font.PLAIN, 16);
    private int index = 0;

    @Override
    public void fireMenuCommand(String command) {

        switch(command) {
            case "show" ->   elems = super.inter.getStackObjects().stream().map(Object::toString).toList();
            case "up" -> up();
            case "down" -> down();
            case "select" -> select();
        }
    }

    private void select() {
        super.inter.push(elems.get(index));
        String onSelectFunction = super.menufunctions.get("onSelect");
        if(onSelectFunction != null) {
            super.inter.executeCommand(onSelectFunction);
        }
    }

    private void down() {
        if(index < elems.size()-1) {
            index++;
        }
    }

    private void up() {
        if(index <= 0) {
            index = 0;
            return;
        }

        index--;
    }

    @Override
    public void paint(Graphics g) {
        g.setFont(font);
        int height =  g.getFontMetrics().getHeight();

        for (int i=0; i<this.elems.size(); i++) {
            if(i == this.index) {
                g.setColor(Color.red);
            }else{
                g.setColor(Color.black);
            }
            g.drawString(elems.get(i), 50, 50+(i*height));
        }
    }
}
