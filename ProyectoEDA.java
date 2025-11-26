import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.concurrent.ThreadLocalRandom;

public class ProyectoEDA {

    // merge sort para fechas
    public static void mergeSortFechas(long[] a, long[] aux, int lo, int hi) {
        if (hi - lo <= 0) return;
        int mid = (lo + hi) >>> 1;

        mergeSortFechas(a, aux, lo, mid);
        mergeSortFechas(a, aux, mid+1, hi);

        int i = lo, j = mid+1, k = lo;
        while (i<=mid || j<=hi) {
            if (j>hi || (i<=mid && a[i] <= a[j])) aux[k++] = a[i++];
            else aux[k++] = a[j++];
        }
        System.arraycopy(aux, lo, a, lo, hi-lo+1);
    }

    // quick sort para valor final (valor final)
    public static void quickSort(double[] a, int lo, int hi){
        if(lo >= hi) return;
        int i = lo, j = hi;
        double pivot = a[lo + (hi-lo)/2];

        while(i<=j){
            while(a[i] < pivot) i++;
            while(a[j] > pivot) j--;
            if(i<=j){
                double tmp = a[i]; a[i]=a[j]; a[j]=tmp;
                i++; j--;
            }
        }
        if(lo < j) quickSort(a, lo, j);
        if(i < hi) quickSort(a, i, hi);
    }

    // leer datos
    public static long[] fechas;
    public static double[] budgets;

    public static void readData(String path, int minSize) throws Exception {
        BufferedReader br = Files.newBufferedReader(Paths.get(path), java.nio.charset.StandardCharsets.ISO_8859_1);

        String header = br.readLine();
        String[] columnas = header.split("[,;]");

        int posFecha=-1, posBudget=-1;

        for(int i=0;i<columnas.length;i++){
            String col= columnas[i].trim().toLowerCase();
            if(col.equals("release_date")) posFecha=i;
            if(col.equals("budget"))       posBudget=i;
        }

        if(posFecha==-1 || posBudget==-1) throw new RuntimeException("No se encuentran columnas release_date o budget");

        List<Long> fechasList = new ArrayList<>();
        List<Double> budgetList = new ArrayList<>();

        String line;
        while((line=br.readLine())!=null){
            String[] p = line.split("[,;]");
            if(p.length <= Math.max(posFecha,posBudget)) continue;
            try {
                String f = p[posFecha].trim(); // yyyy-mm-dd
                long fechaNum = Long.parseLong(f.replace("-","")); // 20200214
                double bg = Double.parseDouble(p[posBudget].replaceAll("[^0-9.\\-]",""));

                fechasList.add(fechaNum);
                budgetList.add(bg);
            }catch(Exception e){}

            if(fechasList.size()>=minSize) break;
        }

        // rellenar sintéticos
        while(fechasList.size()<minSize){
            fechasList.add(Math.abs(ThreadLocalRandom.current().nextLong(19800101,20301231)));
            budgetList.add(ThreadLocalRandom.current().nextDouble(0,1e7));
        }

        fechas = new long[fechasList.size()];
        budgets = new double[budgetList.size()];
        for(int i=0;i<fechas.length;i++){
            fechas[i]=fechasList.get(i);
            budgets[i]=budgetList.get(i);
        }
    }

    public static void main(String[] args)throws Exception {
        String ruta = (args.length>0)? args[0]: "C:\\Users\\LENOVO\\Downloads\\ProyectoEDA\\BasedeDatosMoviesEDA.csv";
        int MIN = 1_000_000;

        System.out.println("Datos cargandose...");
        readData(ruta,MIN);

        // primero ordenamos por fecha
        long[] fechasOrden = Arrays.copyOf(fechas, fechas.length);
        long[] auxF = new long[fechasOrden.length];

        long t0=System.nanoTime();
        mergeSortFechas(fechasOrden,auxF,0,fechasOrden.length-1);
        long t1=System.nanoTime();
        double mergeSec = (t1-t0)/1e9;
        System.out.printf("Ordenamiento por Fecha (MergeSort): %.6f s%n", mergeSec);

        // ahora ordenamos segun el valor final(formula aplicada)
        double[] valorFinal=new double[budgets.length];
        Random rnd=new Random(12345);
        double[] imp = {0.05,0.10,0.15};

        for(int i=0;i<budgets.length;i++){
            double r=0.05+rnd.nextDouble()*0.10;
            double variacion = budgets[i]*(1+r);
            double impuesto = imp[rnd.nextInt(3)];
            valorFinal[i]=variacion*(1-impuesto);
        }

        double[] valorQuick=Arrays.copyOf(valorFinal,valorFinal.length);

        t0=System.nanoTime();
        quickSort(valorQuick,0,valorQuick.length-1);
        t1=System.nanoTime();
        double quickSec=(t1-t0)/1e9;

        System.out.printf("Ordenamiento por formula con quick sort: %.6f s%n", quickSec);
        System.out.println("\nMejor método: " + (mergeSec < quickSec ? "MergeSort con fechas" : "QuickSort con valor final "));
    }
}
