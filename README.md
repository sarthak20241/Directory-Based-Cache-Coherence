The two classes defined in the code are Directory class and Core class. Directory class interacts with multiple cores created with the help of core class.
Directory class
dataMemory class has been defined to keep track of directory information and the memory value associated. Directory information comprises 2 bits about the state of the block, 2 bits for owner, and 4 bits for sharer list.
We have then defined a hashmap dir whose key value is a string indicating the directory address and value indicating the dataMemory for storing memory blocks indexed by the address of the directory.
The constructor initializes the directory with a maximum size of 64 with a memory value of 0 for each address.
Finally, we have the directoryLog for printing the final state of directory entries after processing the entire program.
Directory has access to all the cores.
Core class
Each core class has a cache controller a l1 cache. L1 cache is a 2 way set associative cache with LRU implementation.
Class Cache_Schema represents a cache block in the L1 cache of a particular core. It keeps track of memory address, read/write bit, memory value associated, and valid bit.
Core will be initialized with the constructor with the attributes core Id, L1 cache, cache controller and a set of addresses to be used in L1 cache.
Cache_Controller is a nested class that contains the following methods for cache-related instructions:
● instrLS - executes LS instruction - initiates getShared() transaction
● instrLM - executes LM instruction - initiates getModified() and put() transactions
● instrIN - executes IN instruction - initiates put() transaction
● instrADD - executes ADD instruction - initiates getModified() and put() transactions
This class handles the cache's working and cache coherence transactions in the quad-core architecture with directory-based coherence protocol. The cache controller interacts with the main memory and updates the cache based on the cache coherence protocol.
Main.py
A new directory and four cores are initialized with an instance of Directory class and Core class respectively.
each instruction is decoded from the instruction.txt and sent to appropriate cores by interconnect(assumed). The cache and directory are updated accordingly.
cacheMemoryDump showcases the status of L1 cache of each core after processing an instruction.
Finally, directoryLog is called for printing the final state of directory entries.
