package at.ac.tuwien.student.e01526624.backend.domain.excel;

import java.util.HashMap;
import java.util.Map;

public class Util {
    private static Map<Character, Integer> letterMap;

    public static int columnIndexFromColumnString(String column) {
        if (column.length() > 1) {
            int i = 0;
        }
        if (letterMap == null) {
            initLetterMap();
        }
        int columnIndex = 0;
        for (int i = 0; i < column.length(); i++) {
            columnIndex += Math.pow(26, column.length() - 1 - i) * letterMap.get(column.charAt(i));
        }
        return columnIndex - 1;
    }

    private static void initLetterMap() {
        char[] indexChars = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        letterMap = new HashMap<>();
        for (int i = 0; i < indexChars.length; i++) {
            letterMap.put(indexChars[i], i + 1);
        }
    }
}
