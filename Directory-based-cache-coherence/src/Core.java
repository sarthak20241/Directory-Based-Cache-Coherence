
import java.util.*;
class Cache_Schema{
    String memoryAddr;
    String read_write_bit;
    String Memory_val;
    String valid;

    Cache_Schema(String memoryAddr, String read_write_bit, String Memory_val){
        this.memoryAddr = memoryAddr;
        this.read_write_bit=read_write_bit;
        this.Memory_val=Memory_val;
        this.valid="1";
    }
}
public class Core {
    //contains the set of addresses the key is address and value-1 implies used value-0 implies not used
    public HashMap<String,Integer> address= new HashMap<String,Integer>();
    //L1 cache
    public HashMap<String,Cache_Schema> L1_cache;
    //L1 cache controller
    public String s0_lru= null;
    public String s1_lru=null;
    public Cache_Controller cc;
    //max cache size
    int maxSize=4;
    //core id
    int cid;
    // calculate number of hits and misses
    int hits=0;
    int misses=0;
    Core(int cid){
        this.cid=cid;
        L1_cache = new HashMap<String,Cache_Schema>();
        cc=new Cache_Controller(cid);

        //adding set of addresses that will be used in the L1 cache
        for(int i=0;i<maxSize;i++){
            String addr=Integer.toBinaryString(i);
            while(addr.length()<2){
                addr="0"+addr;
            }
            address.put(addr,0);  //initially all addresses are 0 unused
        }
        // for(Map.Entry<String,Integer> e:address.entrySet()){
        //   System.out.println(e.getKey()+" "+e.getValue());
        // }
    }

    public Cache_Schema getDataUsingMemoryAddress(String memAddress){
        for(Map.Entry<String,Cache_Schema> e:L1_cache.entrySet()){
            if(e.getValue().memoryAddr.equals(memAddress)){
                return e.getValue();
            }
        }
        return null;
    }
    public void updateDataUsingMemoryAddress(String memAddress,Cache_Schema cm){
        for(Map.Entry<String,Cache_Schema> e:L1_cache.entrySet()){
            if(e.getValue().memoryAddr.equals(memAddress)){
                L1_cache.put(e.getKey(),cm);
            }
        }
    }
    // only 2 sets since 4 locations
    //implementation of 2 way set associative cache
    public String getAvailableAddress(Cache_Schema cs){

        for(Map.Entry<String,Integer> e:address.entrySet()){
            if(e.getValue()==0 && (Integer.parseInt(cs.memoryAddr,2)%2==Integer.parseInt(e.getKey().substring(0,1),2))){

                return e.getKey();
            }
        }
        return null;
    }

    public String getInvalidAddress(Cache_Schema cs){

        for(Map.Entry<String,Cache_Schema> e:L1_cache.entrySet()){
            if(e.getValue().valid.equals("0") && (Integer.parseInt(cs.memoryAddr,2)%2==Integer.parseInt(e.getKey().substring(0,1),2))){

                return e.getKey();
            }
        }
        return null;
    }

    // note : when there is no availabe address then first look for all the invalid locations in cache and add data to those locations
    // once there is no invalid location left then start lru implementation
    public void addDataToCache(Cache_Schema cs){
        if(getAvailableAddress(cs)==null){

            if(getInvalidAddress(cs)==null){
                if(Integer.parseInt(cs.memoryAddr,2)%2==0){
                    String addr=s0_lru;
                    evictFromCache("0"+addr);
                    L1_cache.put("0"+addr,cs);  //adding data
                    address.put("0"+addr,1);  //address now in
                    String comp_addr=null;
                    if(addr.substring(1).equals("0")) comp_addr="1";
                    else comp_addr="0";
                    s0_lru=comp_addr;
                }
                else{
                    String addr=s1_lru;//addr is in binary string
                    evictFromCache("1"+addr);
                    L1_cache.put("1"+addr,cs);  //adding data
                    address.put("1"+addr,1);  //address now in use
                    String comp_addr=null;
                    if(addr.substring(1).equals("0")) comp_addr="1";
                    else comp_addr="0";
                    s1_lru=comp_addr;

                }
            }
            else{
                String addr=getInvalidAddress(cs);
                L1_cache.put(addr,cs);  //adding data
                address.put(addr,1);  //address now in use
                if(Integer.parseInt(cs.memoryAddr,2)%2==0){
                    String comp_addr=null;
                    if(addr.substring(1).equals("0")) comp_addr="1";
                    else comp_addr="0";
                    s0_lru=comp_addr;
                }
                else{
                    String comp_addr=null;
                    if(addr.substring(1).equals("0")) comp_addr="1";
                    else comp_addr="0";
                    s1_lru=comp_addr;

                }
            }

        }
        else{
            String addr=getAvailableAddress(cs);
            L1_cache.put(addr,cs);  //adding data
            address.put(addr,1);  //address now in use
            if(Integer.parseInt(cs.memoryAddr,2)%2==0){ //for set 0
                String comp_addr=null;
                if(addr.substring(1).equals("0")) comp_addr="1";
                else comp_addr="0";
                s0_lru=comp_addr;

            }
            else{                      //for set 1
                String comp_addr=null;
                if(addr.substring(1).equals("0")) comp_addr="1";
                else comp_addr="0";
                s1_lru=comp_addr;

            }
        }
    }
    //function to evict cache block from cache corresponding to a memory address
    public void evictFromCacheMemAddress(String addr){
        for(Map.Entry<String,Cache_Schema> e:L1_cache.entrySet()){
            if(e.getValue().memoryAddr.equals(addr)){

                evictFromCache(e.getKey());
                break;
            }
        }
    }

    //function to invalidate cache block from cache corresponding to a memory address
    public void invalidateFromCacheMemAddress(String addr){
        for(Map.Entry<String,Cache_Schema> e:L1_cache.entrySet()){
            if(e.getValue().memoryAddr.equals(addr)){
                Cache_Schema cs=e.getValue();
                // invalidate from cache
                cs.valid="0";
                L1_cache.put(e.getKey(),cs);
                //evictFromCache(e.getKey());
                break;
            }
        }
    }
    //function to evict cache block from give cache address
    public void evictFromCache(String addr){
        //check if addr is present in L1 cache
        if(L1_cache.containsKey(addr)){
            //if present evict from cache
            L1_cache.remove(addr);
            //this address of cache is now available
            address.put(addr,0);
            //update the L1 cache
            //cc.updateCache(addr);
        }
    }
    // public void Cache_Control_LS(int Core,String Address){
    //   for (String key : Main_Memory.keySet()) {
    //     Memory_Schema value = Main_Memory.get(key);
    //       if(L1_cache.containsKey(key)==false && (value.substring(0, 3).equals("01"))){
    //           Cache_Schema obj = new Cache_Schema();
    //           obj.read_write_bit="0";
    //           obj.Memory_val = "";
    //           L1_cache.put(key,obj);

    //       }
    //   }
    // }








    class Cache_Controller {
        int cid;

        Cache_Controller(int cid) {
            this.cid = cid;
        }

        //execute instructions
        public void instrLS(String address) {
            //check if already in shared state or modified state
            Core c = Main.dir.getCore(cid);
            for (String key : c.L1_cache.keySet()) {
                Cache_Schema obj = c.L1_cache.get(key);
                if (obj.memoryAddr.equals(address) && obj.valid.equals("1")) {
                    c.hits++;   //wanted read acces and is available in read access
                    return;
                }

            }

            //if no, then


            dataMemory d = Main.dir.getAddress(address);
            String dirInfo = d.directoryInfo;
            String value = d.memoryValue;

            String state = dirInfo.substring(0, 2);
            String owner = dirInfo.substring(2, 4);
            String sharerList = dirInfo.substring(4, 8);

            //intiating shared transaction
            //getting address in read access in cache
            System.out.println("------------------generated Transaction(s),-----------------");
            System.out.println("Generated Transaction->" + "Get Shared on Adrress"+ address );
            System.out.println("------------------generated Transaction(s),-----------------");
            getShared(address, c);

            //initiating put transaction
            String temp = sharerList;//for updating directory later so can see which was in shared state before
            String nstate="01";
            if(state.equals("00")){
                //make state as owned and for the owner make the read write bit of that owner cache as 0
                int cid=Integer.parseInt(owner,2);
                Core rc=Main.dir.getCore(cid);
                Cache_Schema cs = rc.getDataUsingMemoryAddress(address);
                cs.read_write_bit="0";
                rc.updateDataUsingMemoryAddress(address,cs);

                nstate="11";

            }
            else if(state.equals("11")){
                nstate="11";
            }

            //updating directory
            String newSList = "";
            for (int i = 0; i < sharerList.length(); i++) {
                if (i == c.cid || (temp.charAt(i) == '1')) {
                    newSList += "1";
                } else {
                    newSList += "0";
                }
            }

            String newState = nstate;

            String newDirInfo = newState + owner+ newSList;

            c.misses++;
            // update the constructor
            dataMemory newd = new dataMemory(newDirInfo, value);
            //check this function
            Main.dir.updateData(address, newd);

        }

        public void instrLM(String address) {
            //check if already in modified state
            Core c = Main.dir.getCore(cid);
            for (String key : c.L1_cache.keySet()) {
                Cache_Schema obj = c.L1_cache.get(key);
                // if obj is valid and read write bit is 1 for the given address then simply return
                if (obj.memoryAddr.equals(address) && obj.valid.equals("1") && obj.read_write_bit.equals("1")) {
                    c.hits++;  //wanted in modified state and is currently in modified state
                    return;

                }

            }

            //if no, then
            dataMemory d = Main.dir.getAddress(address);
            String dirInfo = d.directoryInfo;
            String value = d.memoryValue;

            String state = dirInfo.substring(0, 2);
            String owner = dirInfo.substring(2, 4);
            String sharerList = dirInfo.substring(4, 8);

            //first obtain data in modified state
            //intiating modified transaction
            System.out.println("------------------generated Transaction(s),-----------------");
            System.out.println("Generated Transaction->" + "Get Modified on Adrress"+ address );
            System.out.println("------------------generated Transaction(s),-----------------");
            getModified(address, c);


            //initiating put transaction
            for (int i = 0; i < sharerList.length(); i++) {
                if (sharerList.charAt(i) == '1') {
                    Core ci = Main.dir.getCore(i);
                    System.out.println("------------------generated Transaction(s),-----------------");
                    System.out.println("Generated Transaction->" + "Put on Address"+address+"on Core "+ (i+1));
                    System.out.println("------------------generated Transaction(s),-----------------");
                    put(address, ci);
                }
            }



            //updating directory
            String newSList = "";
            for (int i = 0; i < sharerList.length(); i++) {
                if (i == c.cid) {
                    newSList += "1";
                } else {
                    newSList += "0";
                }
            }

            String newState = "00";
            String newOwner = Integer.toBinaryString(cid);
            while (newOwner.length() < 2) {
                newOwner = "0" + newOwner;
            }
            String newDirInfo = newState + newOwner + newSList;
            c.misses++; // updating misses
            dataMemory newd = new dataMemory(newDirInfo, value);

            Main.dir.updateData(address, newd);
        }

        public void instrIN(String address) {
            Core c = Main.dir.getCore(cid);

            Cache_Schema cs=getDataUsingMemoryAddress(address);
            // already in invalid state or evicted from cache
            if(cs==null){
                return;
            }

            // in case of invalid valid bit in cache memory is updated to invalid
            // directory is updated as if the data was deleted from cache

            // invalidates the data in cache

            //transaction initiated

            System.out.println("------------------generated Transaction(s),-----------------");
            System.out.println("Generated Transaction->" + "Put on Adrress"+ address+"on Core "+(cid +1));
            System.out.println("------------------generated Transaction(s),-----------------");
            put(address, c);

            //if main memory in shared state --- make ith cid ith bit 0 in sharer list
            // main memory state update-- if there is no other cache having that memory in shared state
            // if modified state
            dataMemory d = Main.dir.getAddress(address);

            String dirInfo = d.directoryInfo;
            String value = d.memoryValue;

            String state = dirInfo.substring(0, 2);
            String owner = dirInfo.substring(2, 4);
            String sharerList = dirInfo.substring(4,8);



            // if(state.equals("01")){
            //   String nSlist=sharerList.substring(0,c.cid)+sharerList.substring(c.cid);
            // }
            String nSlist="";
            String nState="";
            //shared state
            if(state.equals("01")){
                // updating the new sharer list
                for(int i=0;i<sharerList.length();i++){
                    if(i==cid){
                        nSlist=nSlist+"0";
                    }
                    else{
                        nSlist=nSlist+sharerList.charAt(i);
                    }
                }
                // if no sharer then make the new state invalid
                if(Integer.parseInt(nSlist,2)==0){
                    nState="10";
                }
                // if there is some sharer available then the state remains in shared state
                else{
                    nState="01";
                }
            }
            // modified state
            else if(state.equals("00")){
                //new state is invalid
                nState="10";
                // there is no sharer
                nSlist="0000";

            }
            // owned state
            else if(state.equals("11")){
                // if owner is disowned and other sharers are there then make the new state shared

                // if owner is disowned and no other sharers then new state is invalid
                // else any other is invalidated then the state should remain owned

                // updating the new sharer list
                for(int i=0;i<sharerList.length();i++){
                    if(i==cid){
                        nSlist=nSlist+"0";
                    }
                    else{
                        nSlist=nSlist+sharerList.charAt(i);
                    }
                }

                // owner is disowned
                if(Integer.parseInt(owner,2)==cid){
                    // if no other sharers then new state is invalid
                    if(Integer.parseInt(nSlist,2)==0){
                        nState="10";
                    }
                    // if some sharer is left then new state is shared
                    else{
                        nState="01";
                    }
                }
                else{
                    // if owner is not disowned the state remains owned
                    nState="11";
                }
                // if no sharer then make the new state invalid
                if(Integer.parseInt(nSlist,2)==0){
                    nState="10";
                }
            }


            String newDirInfo = nState+ owner + nSlist;

            dataMemory newd = new dataMemory(newDirInfo, value);
            Main.dir.updateData(address, newd);


        }

        public void instrADD(String address, String immediate) {
            // get data in write state using modified
            // if already in write state then continue with next steps

            dataMemory d = Main.dir.getAddress(address);
            String dirInfo = d.directoryInfo;
            String value = d.memoryValue;

            String state = dirInfo.substring(0, 2);
            String owner = dirInfo.substring(2, 4);
            String sharerList = dirInfo.substring(4, 8);

            Core c = Main.dir.getCore(cid);
            Boolean exists=false;
            //checking wethere memory address asked for is available in modified state
            for(String key : c.L1_cache.keySet()){
                Cache_Schema obj = c.L1_cache.get(key);
                if(obj.memoryAddr.equals(address) && obj.valid.equals("1") && obj.read_write_bit.equals("1")){
                    exists=true;
                    c.hits++;
                    break;
                }

            }
            //if not modified state then get in modified state
            String newSList="";
            if(!exists){
                //initiating put transaction
                c.misses++;
                for(int i=0;i<sharerList.length();i++){
                    if(sharerList.charAt(i)=='1'){
                        Core ci=Main.dir.getCore(i);
                        System.out.println("------------------generated Transaction(s),-----------------");
                        System.out.println("Generated Transaction->" + "Put on Adrress"+ address +"on Core "+(i+1));
                        System.out.println("------------------generated Transaction(s),-----------------");
                        put(address,ci);


                    }
                }
                System.out.println("------------------generated Transaction(s),-----------------");
                System.out.println("Generated Transaction->" + "Get Modified on Adrress"+ address+"on Core "+(cid+1) );
                System.out.println("------------------generated Transaction(s),-----------------");
                getModified(address,c);

                //updating directory

            }
            for(int i=0;i<sharerList.length();i++){
                if(i==c.cid){
                    newSList+="1";
                }
                else{
                    newSList+="0";
                }
            }
            String newOwner = Integer.toBinaryString(cid);
            while (newOwner.length() < 2) {
                newOwner = "0" + newOwner;
            }
            //add value of immediate to memory value
            int imm=Integer.parseInt(immediate,2);
            Cache_Schema cm = c.getDataUsingMemoryAddress(address);
            int val=Integer.parseInt(cm.Memory_val,2);
            int sum=val+imm;
            String bin=Integer.toBinaryString(sum);

            while (bin.length() < 8) {
                bin = "0" + bin;
            }
            int n=bin.length();
            bin=bin.substring(n-8);
            //update data in cache
            cm.Memory_val = bin;
            c.updateDataUsingMemoryAddress(address,cm);

            //updating data in directory -- write through implementation
//        dataMemory dm = Main.dir.getAddress(address);
            String newState="00";
            String newDirInfo = newState+ newOwner+ newSList;
            dataMemory newd=new dataMemory(newDirInfo,bin);
            //d.memoryValue=bin;

            Main.dir.updateData(address,newd);

        }

        //execute transactions
        public void getModified(String address, Core c) {
            dataMemory d = Main.dir.getAddress(address);
            String dirInfo = d.directoryInfo;
            String value = d.memoryValue;

            String state = dirInfo.substring(0, 2);
            String owner = dirInfo.substring(2,4);
            String sharerList = dirInfo.substring(4, 8);

            //if directory in invalid state
            if (state.equals("10")) {
                //adding to cache
                Cache_Schema cs = new Cache_Schema(address, "1", value);
                c.addDataToCache(cs);    //to be made
            }
            //shared state
            else if (state.equals("01")) {

                //adding to cache
                Cache_Schema cs = new Cache_Schema(address, "1", value);
                c.addDataToCache(cs);    //to be made


            }
            //modified state or owned state
            //get data or response from the owner
            else if (state.equals("00") || state.equals("11")) {
                int ci=Integer.parseInt(owner,2);
                Core rc=Main.dir.getCore(ci);
                Cache_Schema cs = rc.getDataUsingMemoryAddress(address);
                String val = cs.Memory_val;

                //response
                System.out.println("In getModified() transaction");
                System.out.println("Core "+(ci+1)+" responded with "+val+" to Core "+(cid+1));

                //adding to cache
                Cache_Schema ncs = new Cache_Schema(address, "1", val);
                c.addDataToCache(ncs);    //to be made

            }

        }

        public void getShared(String address, Core c) {
            dataMemory d = Main.dir.getAddress(address);
            String dirInfo = d.directoryInfo;
            String value = d.memoryValue;

            String state = dirInfo.substring(0, 2);
            String owner = dirInfo.substring(2, 4);
            String sharerList = dirInfo.substring(4, 8);

            //if directory in invalid state
            if (state.equals("10")) {
                //adding to cache
                Cache_Schema cs = new Cache_Schema(address, "0", value);
                c.addDataToCache(cs);    //to be made
            }
            //shared state
            else if (state.equals("01")) {

                //adding to cache
                Cache_Schema cs = new Cache_Schema(address, "0", value);
                c.addDataToCache(cs);    //to be made


            }
            //modified and owned state

            //if state is modified or owned get data from the owner and add to cache with read-write bit as 0 - done in LS
            //if state is owned treat it like shared state but get value from the owner cache instead of directory
            else if(state.equals("11") || state.equals("00")){
                //getResponse from owner
                int ci=Integer.parseInt(owner,2);
                Core rc=Main.dir.getCore(ci);
                Cache_Schema cs = rc.getDataUsingMemoryAddress(address);
                String val = cs.Memory_val;

                //response
                System.out.println("In getShared() transaction");
                System.out.println("Core "+(ci+1)+" responded with "+val+" to Core "+(cid+1));

                //adding to cache
                Cache_Schema ncs = new Cache_Schema(address, "0", val);
                c.addDataToCache(ncs);    //to be made
            }
        }

        public void put(String address, Core c) {
            c.invalidateFromCacheMemAddress(address);
        }
    }
}