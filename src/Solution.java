import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Solution {

    // Complete the matrixRotation function below.
    static void matrixRotation(List<List<Integer>> matrix, int r) {
        int h = matrix.size();
        int w = matrix.get(0).size();

        int[][] m = new int[h][w];
        int i = 0;
        for (List<Integer> row: matrix) {
            int k = 0;
            for(int e: row) {
                m[i][k] = e;
                k++;
            }
            i++;
        }

        //System.out.println(Arrays.deepToString(m).replace("],","],\n"));

        int[][] m2 = new int[h][w];

        int o = 0;
        while(o<h/2 && o<w/2) {
            int sqLen = (h - o*2) * 2 + (w - o*2) * 2 - 4;
            int[] sq = new int[sqLen];
            i = 0;
            for (int j = o; j < w - o; j++) sq[i++] = m[o][j];
            for (int j = o + 1; j < h - o; j++) sq[i++] = m[j][w - 1 - o];
            for (int j = o + 1; j < w - o; j++) sq[i++] = m[h - 1 - o][w - 1 - j];
            for (int j = o + 1; j < h - 1 - o; j++) sq[i++] = m[h - 1 - j][o];

            int[] sq2 = new int[sqLen];
            for (int j = 0; j < sqLen; j++) sq2[j] = sq[(j + r) % sqLen];
            //System.out.println(Arrays.toString(sq));
            //System.out.println(Arrays.toString(sq2));

            i = 0;
            for (int j = o; j < w-o; j++) m2[o][j] = sq2[i++];
            for (int j = o+1; j < h-o; j++) m2[j][w - 1-o] = sq2[i++];
            for (int j = o+1; j < w-o; j++) m2[h - 1-o][w - 1 - j] = sq2[i++];
            for (int j = o+1; j < h - 1-o; j++) m2[h - 1 - j][o] = sq2[i++];
            o++;
        }

        System.out.println(Arrays.deepToString(m2).replace("], ","\n").replace("[","").replace(",","").replace("]",""));
    }

    public static void main(String[] args) throws IOException {
        List<List<Integer>> matrix = List.of(
                List.of(1, 2, 3, 4),
                List.of(5, 6, 7, 8),
                List.of(9, 10, 11, 12),
                List.of(13, 14, 15, 16)
        );
        matrixRotation(matrix, 2);
    }
}
