import java.sql.SQLOutput;
import java.util.*;
class dataMemory{
    String directoryInfo;
    String memoryValue;

    dataMemory(String directoryInfo,String memoryValue){
        this.directoryInfo=directoryInfo;
        this.memoryValue=memoryValue;
    }
}
public class Directory {
    HashMap<String,dataMemory> dir= new HashMap<String,dataMemory>();
    ArrayList<Core> cores=new ArrayList<Core>();
    int size=0;
    int maxSize=64;
    String naddress="000000";  //next location
    Directory(){
        for(int i=0;i<maxSize;i++){
            addData("00000000");
        }

    }
    HashMap<String,dataMemory> getMap(){
        return dir;
    }
    //adds new data into the data memory along with its directory info
    void addData(String data){
        String dInfo="10000000";//2 bits for state, (1 valid bit?) , 2 bits for owners, 4 bits for sharer list
        dataMemory d=new dataMemory(dInfo,data);
        if(size<64){
            dir.put(naddress,d);
            size++;
            naddress=nextAddress(naddress);
        }
    }

    void updateData(String address,dataMemory d){
        dir.put(address,d);
    }
    //next address of the data memory
    String nextAddress(String naddress){
        String naddress1=naddress;
        int n=Integer.parseInt(naddress,2);
        n++;
        naddress1=Integer.toBinaryString(n);
        while(naddress1.length()<6){
            naddress1="0"+naddress1;
        }
        return naddress1;
    }

    dataMemory getAddress(String address){

        return dir.get(address);
    }

    void addCore(Core c){
        cores.add(c);
    }

    Core getCore(int cId){
        return cores.get(cId);
    }

    public void directoryLog() {
        System.out.println("Directory Log:");

        System.out.println(String.format("%-15s%-20s%-20s", "Address", "Directory Info", "Memory Value"));
        System.out.println("-----------------------------------------------------------");
        for(int i=0;i<size;i++){
            String add=Integer.toBinaryString(i);
            //System.out.println(add);
            while(add.length()<6){
                add="0"+add;
            }

            dataMemory value = dir.get(add);
            String formattedAddress = String.format("%-15s", add);
            String formattedDirInfo = String.format("%-20s", value.directoryInfo);
            String formattedMemoryValue = String.format("%-20s", value.memoryValue);

            System.out.println(formattedAddress + formattedDirInfo + formattedMemoryValue);
        }

    }
}
