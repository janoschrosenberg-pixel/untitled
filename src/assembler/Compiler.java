package assembler;

import main.Utils;

import javax.swing.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;

import static main.Utils.getHexFromString;


public class Compiler {
    private ArrayList<Byte> byteCode = new ArrayList<>();

    int publicMethodIdCounter = 0;

    private Map<String, Integer> methodMapping = new HashMap<>();

    public static void main(String[] args) throws IOException, URISyntaxException {
        new Compiler().compile("/editor1.easm");
    }

    public void compile(String file) throws IOException, URISyntaxException {
     var strings=   Utils.readLines(Path.of(Objects.requireNonNull(Compiler.class.getResource(file)).toURI()));

     List<String> clearedList = strings.stream().filter(e->!e.isBlank()).toList();

     List<String> assembledList = new ArrayList<>();
     List<String> constants = new ArrayList<>();

    for (int i=0; i<clearedList.size();i++) {
        var current = clearedList.get(i).trim();

        if(current.equals("END")) continue;

        if(Opcodes.GLOBAL_FUNC.name().equals(current)){
            String methodName = clearedList.get(i+1);

            methodMapping.put(methodName, publicMethodIdCounter);

            int bytes = countBytes(clearedList, i+2);

            String stringConstant = Opcodes.ADD_STRING_CONST.name()+" "+methodName.length()+" "+publicMethodIdCounter+" "+methodName;
            String newGlobalFunc = current+" "+bytes+" "+publicMethodIdCounter;

            constants.add(stringConstant);
            assembledList.add(newGlobalFunc);

            publicMethodIdCounter += 1;
            i++;
        }else{
            if(current.startsWith(Opcodes.SET_KEY_MAP.name())){
                String[] parts = current.split("\\s+");

                int code = KeyStroke.getKeyStroke(parts[1]).getKeyCode();
                int methodRef = methodMapping.get(parts[2]);

                assembledList.add(parts[0]+" "+code+" "+methodRef);

            }else{
                assembledList.add(current);
            }

        }

    }
        constants.forEach(System.out::println);
        assembledList.forEach(System.out::println);

        String hexConstants = generateHexByteCodeConstants(constants);
        System.out.println(hexConstants);
    }

    private String generateHexByteCodeConstants(List<String> constants) {
        StringBuilder sb = new StringBuilder();
        for(String current:constants){
            if(current.startsWith(Opcodes.ADD_STRING_CONST.name())){
                String[] parts = current.split("\\s+");
                sb.append(Opcodes.valueOf(parts[0]).getId()+" ");

                var stringLength =  String.format("%04X", Integer.parseInt(parts[1]));
                var index = String.format("%04X", Integer.parseInt(parts[2]));
                sb.append(stringLength+" ");
                sb.append(index+" ");
                sb.append(getHexFromString(parts[3])+"\n");
            }
        }

        return sb.toString();
    }


    private int countBytes( List<String> list, int index) {
        int counter = 0;

        for (int i = index; i<list.size(); i++){
            var current = list.get(i);
            if("END".equals(current)) {
                return counter;
            }

            counter+= Opcodes.valueOf(current).getBytes();

        }

        throw new IllegalStateException("Es muss ein Ende vorkommen");
    }

}
