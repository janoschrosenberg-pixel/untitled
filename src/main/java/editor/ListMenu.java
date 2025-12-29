package editor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ListMenu extends Menu{
    private List<String> elems = new ArrayList<>();
    private final Font font = new Font("Monospaced", Font.PLAIN, 16);
    private int index = 0;
    private int topLine = 0;

    private List<String> filteredList = null;

    @Override
    public void fireMenuCommand(String command) {

        switch(command) {
            case "show" ->   show();
            case "up" -> up();
            case "down" -> down();
            case "select" -> select();
            case "filter" -> filter();
        }
    }

    private void show() {
        elems = super.inter.getStackObjects().stream().map(Object::toString).toList();
        filteredList = elems;
    }

    private void filter() {
        filteredList = this.elems.stream().filter(e -> e.toLowerCase().contains(TempBuffer.INSTANCE.text().toLowerCase())).toList();
    }

    private void select() {
        super.inter.push(filteredList.get(index));
        String onSelectFunction = super.menufunctions.get("onSelect");
        if(onSelectFunction != null) {
            super.inter.executeCommand(onSelectFunction);
        }
    }

    private void down() {
        if(index < filteredList.size()-1) {
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

        int margin = 50;
        g.setFont(font);
        int height =  g.getFontMetrics().getHeight();

        Rectangle clip = g.getClipBounds();
        int maxHeight = clip.height-margin;
        int maxRows = maxHeight / height;


        if(index+1>maxRows + topLine){
            topLine++;
        }

        if(index>0 && topLine == index){
            topLine--;
        }

        int maxElem = maxRows + topLine;

        if(maxElem>filteredList.size()) {
            maxElem = filteredList.size();
        }
        g.drawString(TempBuffer.INSTANCE.text(), margin,20);

        for (int i=topLine; i<maxElem; i++) {
            if(i == this.index) {
                g.setColor(Color.red);
            }else{
                g.setColor(Color.black);
            }
            g.drawString((i+1)+" "+filteredList.get(i), margin, margin+((i-topLine)*height));
        }
    }
}
