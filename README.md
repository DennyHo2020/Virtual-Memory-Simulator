# Virtual-Memory-Simulator

Virtual Memory Simulator

Program simulates four Page Replacement Algorithms: 
1. The Optimal
2. First In First Out(FIFO) 
3. Random
4. Clock

The algorithms are for page replacements done in the operating system. Program assumes user will provide 
number of frames, the algorithm(opt|clock|fifo|rand), and a trace file in the command line arguments.

Each algorithm handles what to do when:
1. There are empty frames to fill
    - Page is in RAM
    - Page is not in RAM
2. No empty frames
    - Page is in RAM
    - Page is not in RAM
    
Optimal Algorithm:
Replaced page is determined with the help of the map. Method calculates the
distance from this current instruction to the next usage for each page in
RAM. The farthest page that will need to be used will be replaced.

FIFO:
Replaced page is the page that was first in.

Random:
Radomly replace a page.

CLOCK:
Pages that are referenced get a second chance. The first page found that is not referenced will be replaced.

PTE is a page table entry class that keeps track of the dirty, reference, valid bits plus the frame number and page number.

![Screen Shot 2020-02-01 at 4 57 31 PM](https://user-images.githubusercontent.com/31720526/73600861-fd9f6b80-4513-11ea-8747-1e19e705d745.png)

