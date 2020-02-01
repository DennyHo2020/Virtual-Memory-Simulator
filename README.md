# Virtual-Memory-Simulator

Virtual Memory Simulator

Program simulates four Page Replacement Algorithms: 
1. The Optimal
2. First In First Out(FIFO) 
3. Random
4. Clock

The algorithms are for page replacements done in the operating system. Program assumes user will provide 
number of frames, the algorithm(opt|clock|fifo|rand), and a trace file in the command line arguments.

PTE is a page table entry class that keeps track of the dirty, reference, valid bits plus the frame number and page number.

