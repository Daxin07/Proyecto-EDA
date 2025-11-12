import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.concurrent.ThreadLocalRandom;

public class ProyectoEDA {
    // MergeSort
    public static void mergeSort(double[] a, double[] aux, int lo, int hi) {
        if (hi - lo <= 0) return;
        int mid = (lo + hi) >>> 1;
        mergeSort(a, aux, lo, mid);
        mergeSort(a, aux, mid+1, hi);
        int i = lo, j = mid+1, k = lo;
        while (i <= mid || j <= hi) {
            if (j > hi || (i <= mid && a[i] <= a[j])) aux[k++] = a[i++];
            else aux[k++] = a[j++];
        }
        System.arraycopy(aux, lo, a, lo, hi - lo + 1);
    }

    // QuickSort
    public static void quickSort(double[] a, int lo, int hi) {
        if (lo >= hi) return;
        int i = lo, j = hi;
        double pivot = a[lo + (hi - lo) / 2];
        while (i <= j) {
            while (a[i] < pivot) i++;
            while (a[j] > pivot) j--;
            if (i <= j) {
                double t = a[i]; a[i] = a[j]; a[j] = t;
                i++; j--;
            }
        }
        if (lo < j) quickSort(a, lo, j);
        if (i < hi) quickSort(a, i, hi);
    }

    //lector de archivo
    public static double[] readBudgets(String path, int minSize) throws Exception {
        BufferedReader br = Files.newBufferedReader(Paths.get(path), java.nio.charset.StandardCharsets.ISO_8859_1);
        String header = br.readLine();
        boolean hasHeader = header != null && header.toLowerCase().contains("budget");
        int budgetCol = -1;
        List<Double> vals = new ArrayList<>();
        if (hasHeader) {
            String[] cols = header.split("[,;]");
            for (int i = 0; i < cols.length; i++) if (cols[i].trim().toLowerCase().equals("budget")) budgetCol = i;
        } else {
            budgetCol = 4;
            br = Files.newBufferedReader(Paths.get(path));
        }
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("[,;]");
            if (budgetCol >= parts.length) continue;
            try {
                double b = Double.parseDouble(parts[budgetCol].replaceAll("[^0-9.\\-]", ""));
                vals.add(b);
            } catch (Exception e) { /* saltar */ }
            if (vals.size() >= minSize) break;
        }
        // Aplicar valores random
        while (vals.size() < minSize) vals.add( ThreadLocalRandom.current().nextDouble(0, 1e7) );
        double[] arr = new double[vals.size()];
        for (int i = 0; i < vals.size(); i++) arr[i] = vals.get(i);
        return arr;
    }

    public static void main(String[] args) throws Exception {
        String ruta = (args.length>0)? args[0] : "C:\\Users\\LENOVO\\Downloads\\ProyectoEDA\\BasedeDatosMoviesEDA.csv";
        int MIN = 1_000_000;
        System.out.println("Leyendo budgets (hasta "+MIN+")...");
        double[] budgets = readBudgets(ruta, MIN);

        // Aplicar fórmula: variacion = budget * (1 + r) , impuesto in {0.05,0.10,0.15}, valor_final = variacion*(1-impuesto)
        double[] valorFinal = new double[budgets.length];
        Random rnd = new Random(12345);
        double[] impuestos = {0.05,0.10,0.15};
        for (int i=0;i<budgets.length;i++){
            double r = 0.05 + rnd.nextDouble()*(0.10); // [0.05,0.15)
            double variacion = budgets[i] * (1.0 + r);
            double imp = impuestos[rnd.nextInt(impuestos.length)];
            valorFinal[i] = variacion * (1.0 - imp);
        }

        // Preparamos copias para cada sort
        double[] aForMerge = Arrays.copyOf(valorFinal, valorFinal.length);
        double[] aux = new double[aForMerge.length];
        double[] aForQuick = Arrays.copyOf(valorFinal, valorFinal.length);

        // Tiempo Merge Sort
        long t0 = System.nanoTime();
        mergeSort(aForMerge, aux, 0, aForMerge.length-1);
        long t1 = System.nanoTime();
        double mergeSec = (t1 - t0) / 1e9;
        System.out.printf("Mergesort: %.6f s%n", mergeSec);

        // Tiempo Quick Sort
        t0 = System.nanoTime();
        quickSort(aForQuick, 0, aForQuick.length-1);
        t1 = System.nanoTime();
        double quickSec = (t1 - t0) / 1e9;
        System.out.printf("Quicksort: %.6f s%n", quickSec);

        System.out.printf("Mejor ordenamiento: %s%n", (mergeSec < quickSec) ? "Mergesort" : "Quicksort");
    }
}
