import java.io.File; 
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class CS3790_LAB1 {

    public int[] memory = new int[10000]; // 10000 words of memory
    public int page_size = 100;
    public int accumulator = 0;
    public int instructionCounter = 0; // Holds the address of the next instruction to execute.
    public int instructionRegister = 0; // Contains the current instruction being executed.
    public int indexRegister = 0; // For indexing and looping operations.
    
    
    
public Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        
        // Allows us to use the methods and variables defined in the CS3790_LAB1 class
        CS3790_LAB1 simpletron = new CS3790_LAB1();
        Scanner input = new Scanner(System.in);
        System.out.println("*** Welcome to Simpletron V2! ***");

        System.out.println("Do you have a file that contains your SML program (Y/N) ? ");
        String ans = input.next();
        try {
            if (ans.equalsIgnoreCase("Y")) {
               
                simpletron.loadInstructionsFromFile("arraysum.sml");
            }
            else if(ans.equalsIgnoreCase("N")){
                System.out.println("Give me the file: ");
                 simpletron.manualInput(); // Call method to manually input instructions
            }
else {
    System.out.println("Invalid input. Please enter 'Y' or 'N'.");
    return;
}

            
        } catch (IOException e) {
            System.out.println("An error occurred while loading the instructions: " + e.getMessage());
        }

        System.out.println("Instructions loaded into memory.");
        simpletron.execute();
    }
    
        public void manualInput() {
        System.out.println("*** Please enter your program one instruction (or data word) at a time ***");
        System.out.println("*** I will type the location number and a question mark (?). You then ***");
        System.out.println("*** type the word for that location. Type the word GO to execute your program ***");

        int location = 0;  // Start from memory location 0
        while (true) {
            System.out.printf("%06d ? ", location);  // Print the location number followed by a question mark
            String input = scanner.next();  // Read user input
            
            if (input.equalsIgnoreCase("GO")) {
                break;  // Exit the loop and begin execution if the user types "GO"
            }

            try {
                int instruction = Integer.parseInt(input);  // Parse the input as an instruction
                if (instruction >= -999999 && instruction <= 999999) {
                    memory[location] = instruction;  // Store the instruction in memory
                    location++;  // Move to the next memory location
                } else {
                    System.out.println("Invalid instruction range. Please enter a value between -999999 and 999999.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number or 'GO'.");
            }
        }
    }

    // Method to load instructions from a file
    public void loadInstructionsFromFile(String filename) throws IOException {
        try {
            File myFile = new File(filename);
            Scanner myReader = new Scanner(myFile);
            int memoryIndex = 0;

            while (myReader.hasNextInt() && memoryIndex < page_size) {
                int instruction = myReader.nextInt();
                if (instruction >= -999999 && instruction <= 999999) {
                    memory[memoryIndex++] = instruction;
                } else {
                    System.out.println("Invalid instruction range: " + instruction);
                }
            }
           // System.out.println("Instructions loaded into memory.");
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
            System.exit(1);
        }
    }

    // Method to execute loaded instructions
    public void execute() {
        boolean running = true;

        while (running) {
            instructionRegister = memory[instructionCounter];
            int operationCode = Math.abs(instructionRegister) / 10000;
            int operand = Math.abs(instructionRegister) % 10000;

            // Debugging: Print the instruction being executed
            System.out.printf("Executing instruction: %06d, Opcode: %d, Operand: %d%n", instructionRegister, operationCode, operand);

            switch (operationCode) {
                case 10: // READ
               System.out.print("? "+ memory[operand]);
                    int number = scanner.nextInt();
                    memory[operand] = number;
                    accumulator+= number;
                    break;

                case 11: // WRITE
                    System.out.println("Output: " + memory[operand]);
                    break;

                case 20: // LOAD
                    accumulator = memory[operand];
                    break;

                case 21: // LOADIM (Load Immediate)
                    accumulator = operand;
                    break;

                case 22: // LOADX (Load to index register)
                    indexRegister = memory[operand];
                    break;

                case 25: // STORE
                    memory[operand] = accumulator;
                    break;
                    
                case 26://STOREIDX
                    memory[indexRegister] = accumulator;
                    break;
                    
                case 30: // ADD
                    accumulator += memory[operand];
                    break;

                case 32: // SUBTRACT
                    accumulator -= memory[operand];
                    break;

                case 34: // DIVIDE
                    if (memory[operand] == 0) {
                        handleDivisionByZeroError();
                    } else {
                        accumulator /= memory[operand];
                    }
                    break;

                case 36: // MULTIPLY
                    accumulator *= memory[operand];
                    break;
                    
                case 37://MULTIPLYX
                    accumulator *= memory[indexRegister];
                if (accumulator > 999999 || accumulator < -999999) {
                handleAccumulatorOverflowError();
                }
                    break;
                    
                case 38://INC
                    indexRegister++;
                    break;
                case 39://DEC
                    indexRegister--;
                    break;

                case 40: // BRANCH
                    instructionCounter = operand-1 ;// -1 because we increment IC at the end of the loop
                    break;

                case 41: // BRANCHNEG
                    
                    if (accumulator < 0) {
                        instructionCounter = operand - 1;
                    }
                    else if (instructionCounter == 3) { // If we're at the BRANCHNEG instruction in the loop
                        instructionCounter = 0; // Go back to the start of the loop
                    }
                    break;

                case 42: // BRANCHZERO
                    if (accumulator == 0) {
                        instructionCounter = operand - 1;
                        continue;
                    }
                    break;
                    
                    case 43://SWAP
                    int temp = accumulator;
                    accumulator = indexRegister;
                    indexRegister = temp;
                    break;
                    
                    

                case 45: // HALT
                    System.out.println("*** Simpletron execution terminated ***");
                    dumpCore(operand / 100, operand % 100);
                    running = false;
                    

    
                default:
                    System.out.println("Invalid operation code: " + operationCode);
                    dumpCore(operand / 100, operand % 100);
                    running = false;
                    break;
            }

            // Check for accumulator overflow
            if (accumulator > 999999 || accumulator < -999999) {
                handleAccumulatorOverflowError();
            }

            instructionCounter++;
             if (instructionCounter >= memory.length) {
            System.out.println("Error: Instruction counter out of bounds.");
            running = false;
        }
        }
    }

    public void handleDivisionByZeroError() {
        System.out.println("Error: Division by zero.");
        dumpCore(0, 10);
        System.exit(1);
    }

    public void handleAccumulatorOverflowError() {
        System.out.println("Error: Accumulator overflow.");
        dumpCore(0, 10);
        System.exit(1);
    }

    public void dumpCore(int startPage, int endPage) {
        System.out.println("REGISTERS:");
       
        System.out.printf("accumulator          %06d%n", accumulator);
        System.out.printf("InstructionCounter   %06d%n", instructionCounter);
        System.out.printf("IndexRegister        %06d%n", indexRegister);
        System.out.printf("operationCode        %02d%n", instructionRegister / 100000);
        System.out.printf("operand              %05d%n", instructionRegister % 100000);

        System.out.println("MEMORY");
        for (int i = 0; i < 100; i += 10) {
            System.out.printf("%2d ", i);
            for (int j = 0; j < 10; j++) {
                System.out.printf("%06d ", memory[i + j]);
            }
            System.out.println();
        }
    }
     public void executeGCD() {
        System.out.println("Executing GCD...");

        // Let's assume operands for GCD are in memory[0] and memory[1]
        int a = memory[0];
        int b = memory[1];

        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }

        // Store the result in memory[2] (or any other location)
        memory[2] = a;

        System.out.println("GCD of " + memory[0] + " and " + memory[1] + " is: " + a);
    }

    // Finding Largest and Smallest Elements in an Array (triggered by OpCode 51)
    public void executeFindLargestSmallest() {
        System.out.println("Executing Find Largest and Smallest...");

        // Assuming the array starts at memory[0] and its length is stored in memory[99] (for example)
        int length = memory[99];
        int largest = memory[0];
        int smallest = memory[0];

        for (int i = 1; i < length; i++) {
            if (memory[i] > largest) {
                largest = memory[i];
            }
            if (memory[i] < smallest) {
                smallest = memory[i];
            }
        }

        // Store the results in memory[98] for largest and memory[97] for smallest
        memory[98] = largest;
        memory[97] = smallest;

        System.out.println("Largest element: " + largest);
        System.out.println("Smallest element: " + smallest);
    }
}
