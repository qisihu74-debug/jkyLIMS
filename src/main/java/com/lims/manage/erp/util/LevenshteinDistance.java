package com.lims.manage.erp.util;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2024-11-08 10:47
 * @Copyright © 河南交科院
 */
public class LevenshteinDistance {
    public static int computeLevenshteinDistance(CharSequence str1, CharSequence str2) {
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= str2.length(); j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                distance[i][j] = Math.min(Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1), distance[i - 1][j - 1] + cost);
            }
        }

        return distance[str1.length()][str2.length()];
    }

    /**
     * 相似度
     * @param str1
     * @param str2
     * @return
     */
    public static double similarityRatio(CharSequence str1, CharSequence str2) {
        int distance = computeLevenshteinDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        return (maxLength - distance) / (double) maxLength;
    }
}
