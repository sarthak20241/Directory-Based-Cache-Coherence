import java.util.*;
import java.io.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BasicStroke;

import org.knowm.xchart.*;

class Plot extends JPanel {

    private double[] xPoints;
    private double[] yPoints;

    public Plot(double[] xPoints, double[] yPoints) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;

        // Calculate the minimum and maximum values of x and y
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (int i = 0; i < xPoints.length; i++) {
            minX = Math.min(minX, xPoints[i]);
            maxX = Math.max(maxX, xPoints[i]);
            minY = Math.min(minY, yPoints[i]);
            maxY = Math.max(maxY, yPoints[i]);
        }

        // Set the scaling factors for x and y
        double xScale = (getWidth() - 20) / (maxX - minX);
        double yScale = (getHeight() - 20) / (maxY - minY);

        // Draw the x and y axes
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.BLACK);
        g2.drawLine(10, getHeight() - 20, getWidth() - 10, getHeight() - 20);
        g2.drawLine(10, 10, 10, getHeight() - 20);

        // Draw the x and y labels
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(String.format("%.2f", minX), 10, getHeight() - 10);
        g2.drawString(String.format("%.2f", maxX), getWidth() - 30, getHeight() - 10);
        g2.drawString(String.format("%.2f", minY), 5, 15);
        g2.drawString(String.format("%.2f", maxY), 5, getHeight() - 30);

        g2.drawString("X-Axis", (getWidth() - 20) / 2, getHeight() - 10 + 15);

        // Label the y-axis
        g2.rotate(-Math.PI / 2, 10, 10);
        g2.drawString("Y-Axis", 10, 20);
        g2.rotate(Math.PI / 2, 10, 10);
        // Draw the data points
        g2.setPaint(Color.BLUE);
        g2.setStroke(new BasicStroke(2));
        for (int i = 0; i < xPoints.length - 1; i++) {
            Point p1 = new Point((int) (10 + xPoints[i] * xScale), (int) (getHeight() - 10 - yPoints[i] * yScale));
            Point p2 = new Point((int) (10 + xPoints[i + 1] * xScale), (int) (getHeight() - 10 - yPoints[i + 1] * yScale));
            g2.draw(new Line2D.Double(p1, p2));
        }
    }
}



public class Main {
    static Directory dir;
    private static List<String> readLinesFromFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }
    public static void main(String[] args) throws IOException {
        dir = new Directory();

        Core c1 = new Core(0);
        Core c2 = new Core(1);
        Core c3 = new Core(2);
        Core c4 = new Core(3);
        dir.addCore(c1);
        dir.addCore(c2);
        dir.addCore(c3);
        dir.addCore(c4);
        String filePath = "src/instructions.txt";
        List<String> sentences = readLinesFromFile(filePath);

        ArrayList<Double> directoryUpdates = new ArrayList<>();
        ArrayList<Double> directoryUpdates_x = new ArrayList<>();

        for (int i = 0; i < sentences.size(); i++) {
            HashMap<String, dataMemory> prev= new HashMap<>();
            prev.putAll(dir.getMap());

            String sentence = sentences.get(i);
            System.out.println(i+1+") "+sentence);
            int coreId = Integer.parseInt(sentence.substring(0, 2), 2);
            if (coreId >= 0 && coreId < 4) {
                Core currentCore = dir.getCore(coreId);

                if (sentence.substring(3, 5).equals("LS")) {
                    currentCore.cc.instrLS(sentence.substring(6));
                } else if (sentence.substring(3, 5).equals("LM")) {
                    currentCore.cc.instrLM(sentence.substring(6));
                } else if (sentence.substring(3, 5).equals("IN")) {
                    currentCore.cc.instrIN(sentence.substring(6));
                } else if (sentence.substring(3, 6).equals("ADD")) {
                    String x = sentence.substring(15);
                    currentCore.cc.instrADD(sentence.substring(7, 13), x);
                }
            }

            cacheMemoryDump(c1, c2, c3, c4);

            directoryUpdates.add(Dir_Updates(prev,dir.getMap())*1.0);
            directoryUpdates_x.add(i*1.0);





        }
        double[] hitRates = {
                Hit_Rate(c1.hits, c1.misses),
                Hit_Rate(c2.hits, c2.misses),
                Hit_Rate(c3.hits, c3.misses),
                Hit_Rate(c4.hits, c4.misses)
        };
        double[] core_x_axis = { 1,2,3,4 };
        double[] memoryAccessLatency = new double[4];
        double[] missRate = new double[4];

        double hitTime = 1;
        double missPenalty = 2 * hitTime;
        double[] directory_y_axis = convertDoubleArrayListToArray(directoryUpdates);
        for(int i=1;i<directory_y_axis.length;i++){
            directory_y_axis[i] += directory_y_axis[i-1];
        }
        double[] directory_x_axis = convertDoubleArrayListToArray(directoryUpdates_x);

        // Calculate and display memory access latency for each core
        System.out.println("--------------------------------------");
        for (int i = 0; i < 4; i++) {
            missRate[i] = 1 - hitRates[i];
            memoryAccessLatency[i] = (double)(hitTime * hitRates[i]) + (double)(missPenalty * missRate[i]);

        }
        // Graph_plotter(directory_x_axis, directory_y_axis);
         XYChart chart = QuickChart.getChart("Miss Rate", "X", "Y", "y(x)", core_x_axis, missRate);

             // Show it
             new SwingWrapper<>(chart).displayChart();
        XYChart chart2 = QuickChart.getChart("Memory Access Latency", "X", "Y", "y(x)", core_x_axis, memoryAccessLatency);

        // Show it
        new SwingWrapper<>(chart2).displayChart();

        XYChart chart3 = QuickChart.getChart("Directory Updates", "Cycles", "No. of Directory Updates", "y(x)", directory_x_axis, directory_y_axis);

        // Show it
        new SwingWrapper<>(chart3).displayChart();

        //directory logs
        dir.directoryLog();


    }
    static double Hit_Rate(double hits,double miss){
        // System.out.println("hits "+hits);
        // System.out.println("miss "+miss);
        return (double)hits/(hits+miss);
    }

    static void cacheMemoryDump(Core core1, Core core2, Core core3, Core core4){
        System.out.println("L1 Cache Log for core 1");
        System.out.println("--------------------");

        HashMap<String, Cache_Schema> core1_L1 = core1.L1_cache;

        System.out.println(String.format("%-15s%-20s%-20s%-20s%-20s", "Address", "Read/Write bit", "Memory Value","Memory Address", "Valid Bit"));
        System.out.println("------------------------------------------------------------------------");
        for (String key : core1_L1.keySet()) {
            Cache_Schema value = core1_L1.get(key);
            String address = String.format("%-15s", key);
            String rw = String.format("%-20s", value.read_write_bit);
            String memValue = String.format("%-20s", value.Memory_val);
            String memAddress = String.format("%-20s", value.memoryAddr);
            String validBit = String.format("%-20s", value.valid);

            System.out.println(address + rw + memValue + memAddress + validBit);
        }

        //Core 2
        System.out.println("L1 Cache Log for core 2");
        System.out.println("--------------------");
        HashMap<String, Cache_Schema> core2_L1 = core2.L1_cache;

        System.out.println(String.format("%-15s%-20s%-20s%-20s%-20s", "Address", "Read/Write bit", "Memory Value","Memory Address", "Valid Bit"));
        System.out.println("------------------------------------------------------------------------");
        for (String key : core2_L1.keySet()) {
            Cache_Schema value = core2_L1.get(key);
            String address = String.format("%-15s", key);
            String rw = String.format("%-20s", value.read_write_bit);
            String memValue = String.format("%-20s", value.Memory_val);
            String memAddress = String.format("%-20s", value.memoryAddr);
            String validBit = String.format("%-20s", value.valid);

            System.out.println(address + rw + memValue + memAddress + validBit);
        }

        //Core3
        System.out.println("L1 Cache Log for core 3");
        System.out.println("--------------------");
        HashMap<String, Cache_Schema> core3_L1 = core3.L1_cache;

        System.out.println(String.format("%-15s%-20s%-20s%-20s%-20s", "Address", "Read/Write bit", "Memory Value","Memory Address", "Valid Bit"));
        System.out.println("------------------------------------------------------------------------");
        for (String key : core3_L1.keySet()) {
            Cache_Schema value = core3_L1.get(key);
            String address = String.format("%-15s", key);
            String rw = String.format("%-20s", value.read_write_bit);
            String memValue = String.format("%-20s", value.Memory_val);
            String memAddress = String.format("%-20s", value.memoryAddr);
            String validBit = String.format("%-20s", value.valid);

            System.out.println(address + rw + memValue + memAddress + validBit);
        }

        //Core4
        System.out.println("L1 Cache Log for core 4");
        System.out.println("--------------------");
        HashMap<String, Cache_Schema> core4_L1 = core4.L1_cache;

        System.out.println(String.format("%-15s%-20s%-20s%-20s%-20s", "Address", "Read/Write bit", "Memory Value","Memory Address", "Valid Bit"));
        System.out.println("------------------------------------------------------------------------");
        for (String key : core4_L1.keySet()) {
            Cache_Schema value = core4_L1.get(key);
            String address = String.format("%-15s", key);
            String rw = String.format("%-20s", value.read_write_bit);
            String memValue = String.format("%-20s", value.Memory_val);
            String memAddress = String.format("%-20s", value.memoryAddr);
            String validBit = String.format("%-20s", value.valid);

            System.out.println(address + rw + memValue + memAddress + validBit);
        }
    }
    public static double[] convertDoubleArrayListToArray(ArrayList<Double> arrayList) {
        double[] array = new double[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            array[i] = arrayList.get(i);
        }
        return array;
    }
    public static void Graph_plotter(double[] xPoints,double[] yPoints){
        JFrame frame = new JFrame("Graph Plotter");
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Plot(xPoints, yPoints));
        frame.setVisible(true);
    }
    static int Dir_Updates(HashMap<String,dataMemory> prev,HashMap<String,dataMemory> now){
        int count =0;

        for(String i:now.keySet()){

            if(!(prev.get(i).directoryInfo.equals(now.get(i).directoryInfo))){
                count++;
            }
        }

        return count;

    }
    public String generateRandomFiveBitAddress() {
        Random rand = new Random();
        int randomValue = rand.nextInt(32); // Generates a random integer between 0 and 31
        String binaryString = Integer.toBinaryString(randomValue);


        while (binaryString.length() < 5) {
            binaryString = "0" + binaryString;
        }

        return binaryString;
    }
}
