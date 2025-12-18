package editor;


import editor.filemenu.ParentFile;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FileMenu extends Menu{

    private final List<List<File>> fileNames = new ArrayList<>();

    private int column = 0;
    private int row = 0;


    private Stack<ParentFile> parentFiles = new Stack<>();


    private File[] currentFiles;
    public FileMenu() {
        this.currentFiles = File.listRoots();
        updateFilenames();
    }

    private int currentWidth = 100;

    private Font font = new Font("Arial", Font.PLAIN, 16);
    private void updateFilenames() {
        fileNames.clear();
        List<File> line = new ArrayList<>();
        StringBuilder completeLine = new StringBuilder();
        for (var f:currentFiles) {
            String filename = f.getName();
            completeLine.append(filename);
            completeLine.append(" ");

            int width = Utils.stringWidth(completeLine.toString(), font);
            if(width > currentWidth-300) {
                fileNames.add(line);
                line = new ArrayList<>();
                completeLine = new StringBuilder();
            }

            line.add(f);
        }

        fileNames.add(line);

    }

    @Override
    public void fireMenuCommand(String command) {

        switch(command) {
            case "up"-> up();
            case "down" -> down();
            case "left" -> left();
            case "right" -> right();
            case "select" -> select();
            case "back" -> back();
            
        }
    }

    private void back() {
        if(parentFiles.isEmpty()) {
            return;
        }
        var parentFile = parentFiles.pop();
        column = parentFile.col();
        row = parentFile.row();
        var parentOfParent =  parentFile.file().getParentFile();
        if(parentOfParent == null) {
            return;
        }
        this.currentFiles = parentOfParent.listFiles();
        updateFilenames();
    }

    private void right() {

        if(column<fileNames.get(row).size()) {
            column++;
        }

        if (column == fileNames.get(row).size()) {
            if(row<fileNames.size()-1) {
                column = 0;
                row++;
            }else{
                column = fileNames.get(row).size() -1;
            }
        }
    }

    private void left() {

        if(column == 0) {
            if(row>0) {
                row--;
                column = fileNames.get(row).size();
            }

        }
        if(column > 0) {
            column--;
        }


    }

    private void select() {

        var selectedFile = fileNames.get(row).get(column);
        if(selectedFile.isDirectory()) {
            var listedFiles = selectedFile.listFiles();
            if(listedFiles == null || listedFiles.length == 0) {
                return;
            }
            currentFiles = listedFiles;
            var parentFile = new ParentFile(row, column, selectedFile);
            parentFiles.push(parentFile);
            row = 0;
            column = 0;
            updateFilenames();
        }else{
            super.inter.push(selectedFile.getPath());
            String onSelectFunction = super.menufunctions.get("onSelect");
            if(onSelectFunction != null) {
                super.inter.executeCommand(onSelectFunction);
            }
        }

    }

    private void down() {
        if(row < fileNames.size()-1) {
            row ++;
        }

        if(fileNames.get(row).size() <= column) {
            column = fileNames.get(row).size() - 1;
        }

    }

    private void up() {
        if(row>0) {
            row--;
        }
        if(fileNames.get(row).size() <= column) {
            column = fileNames.get(row).size() - 1;
        }

    }

    @Override
    public void paint(Graphics g) {
      g.setFont(font);
      int height =  g.getFontMetrics().getHeight();

        Rectangle clip = g.getClipBounds();
        if(currentWidth != clip.width) {
            currentWidth = clip.width;
            updateFilenames();
        }



     for (int i=0 ; i<fileNames.size(); i++) {

         List<File> line = fileNames.get(i);
         int size = 0;

         for(int j=0; j<line.size(); j++) {
             String text = line.get(j).getName()+" ";

             if(i==row && j==column) {
                 g.setColor(Color.RED);
             }else{
                 g.setColor(Color.BLACK);
             }
             g.drawString(text, size+30, i*height + 30);

             size +=  g.getFontMetrics().stringWidth(text);
         }

     }
    }
}
