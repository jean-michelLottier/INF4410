/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.commontools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class PropUtils {

    /**
     * <p>Ordonne la map de mots par ordre alphabétique.</p>
     *
     * @param messMap
     * @return sort map
     */
    public HashMap<String, Integer> sortMapByKey(HashMap<String, Integer> messMap) {
        if (messMap == null || messMap.isEmpty()) {
            return null;
        }

        ArrayList<String> keys = new ArrayList<>(messMap.keySet());
        Collections.sort(keys);

        HashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        for (String key : keys) {
            sortedMap.put(key, messMap.get(key));
        }
        return sortedMap;
    }

    /**
     * <p>A partir d'un texte, cette méthode extrait la partie du texte
     * commençant par startLine et se terminant par endLine</p>
     *
     * @param text
     * @param startLine
     * @param endLine
     * @return null or peace of text
     */
    public String extractPeaceOfTextByLinesSelected(String text, int startLine, int endLine) {
        if (text == null || text.isEmpty() || startLine < 0 || endLine < 0 || endLine < startLine) {
            return null;
        }

        String[] linesOfText = text.trim().split("\n");

        if (endLine > linesOfText.length) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = startLine; i < endLine; i++) {
            if (!linesOfText[i].trim().isEmpty()) {
                sb.append(linesOfText[i].trim()).append("\n");
            }

        }

        return sb.toString();
    }

    /**
     * <p>Cette méthode épure un texte pour avoir seulement un texte sans
     * pontuation.</p>
     *
     * @param text
     * @return
     */
    public String cleanText(String text) {
        //return text.toLowerCase().trim().replaceAll("[,:;\"!?]|\\.\\s|\\.\\n|\\.$", "");
        return text.toLowerCase().trim().replaceAll("\\p{Punct}|\n\n|\t|  ", "");
    }
}
