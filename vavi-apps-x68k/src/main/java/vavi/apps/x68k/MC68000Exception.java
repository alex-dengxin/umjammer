/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class MC68000Exception extends Exception {
    public static final MC68000Exception BUS_ERROR_ON_WRITE = new MC68000Exception(2, 0);
    public static final MC68000Exception BUS_ERROR_ON_READ = new MC68000Exception(2, 1);
    public static final MC68000Exception ADDRESS_ERROR_ON_WRITE = new MC68000Exception(3, 0);
    public static final MC68000Exception ADDRESS_ERROR_ON_READ = new MC68000Exception(3, 1);
    public static final MC68000Exception ILLEGAL_INSTRUCTION = new MC68000Exception(4);
    public static final MC68000Exception ZERO_DIVIDE = new MC68000Exception(5);
    public static final MC68000Exception CHK_INSTRUCTION = new MC68000Exception(6);
    public static final MC68000Exception TRAPV_INSTRUCTION = new MC68000Exception(7);
    public static final MC68000Exception PRIVILEGE_VIOLATION = new MC68000Exception(8);
    public static final MC68000Exception TRACE = new MC68000Exception(9);
    public static final MC68000Exception LINE_1010_EMULATOR = new MC68000Exception(10);
    public static final MC68000Exception LINE_1111_EMULATOR = new MC68000Exception(11);
    public static final MC68000Exception UNINITIALIZED_INTERRUPT_VECTOR = new MC68000Exception(15);
    public static final MC68000Exception SPURIOUS_INTERRUPT = new MC68000Exception(24);
    public static final MC68000Exception LEVEL_1_INTERRUPT = new MC68000Exception(25);
    public static final MC68000Exception LEVEL_2_INTERRUPT_AUTOVECTOR = new MC68000Exception(26);
    public static final MC68000Exception LEVEL_3_INTERRUPT_AUTOVECTOR = new MC68000Exception(27);
    public static final MC68000Exception LEVEL_4_INTERRUPT_AUTOVECTOR = new MC68000Exception(28);
    public static final MC68000Exception LEVEL_5_INTERRUPT_AUTOVECTOR = new MC68000Exception(29);
    public static final MC68000Exception LEVEL_6_INTERRUPT_AUTOVECTOR = new MC68000Exception(30);
    public static final MC68000Exception LEVEL_7_INTERRUPT_AUTOVECTOR = new MC68000Exception(31);
    public static final MC68000Exception TRAP_0_INSTRUCTION = new MC68000Exception(32);
    public static final MC68000Exception TRAP_1_INSTRUCTION = new MC68000Exception(33);
    public static final MC68000Exception TRAP_2_INSTRUCTION = new MC68000Exception(34);
    public static final MC68000Exception TRAP_3_INSTRUCTION = new MC68000Exception(35);
    public static final MC68000Exception TRAP_4_INSTRUCTION = new MC68000Exception(36);
    public static final MC68000Exception TRAP_5_INSTRUCTION = new MC68000Exception(37);
    public static final MC68000Exception TRAP_6_INSTRUCTION = new MC68000Exception(38);
    public static final MC68000Exception TRAP_7_INSTRUCTION = new MC68000Exception(39);
    public static final MC68000Exception TRAP_8_INSTRUCTION = new MC68000Exception(40);
    public static final MC68000Exception TRAP_9_INSTRUCTION = new MC68000Exception(41);
    public static final MC68000Exception TRAP_10_INSTRUCTION = new MC68000Exception(42);
    public static final MC68000Exception TRAP_11_INSTRUCTION = new MC68000Exception(43);
    public static final MC68000Exception TRAP_12_INSTRUCTION = new MC68000Exception(44);
    public static final MC68000Exception TRAP_13_INSTRUCTION = new MC68000Exception(45);
    public static final MC68000Exception TRAP_14_INSTRUCTION = new MC68000Exception(46);
    public static final MC68000Exception TRAP_15_INSTRUCTION = new MC68000Exception(47);

    public int value;

    public int rw;

    public MC68000Exception(int value) {
        this.value = value;
        this.rw = 0;
    }

    public MC68000Exception(int value, int rw) {
        this.value = value;
        this.rw = rw;
    }

    public String toString() {
        switch (value) {
        case 0:
            return "Initial SSP";
        case 1:
            return "Initial PC";
        case 2:
            return "Bus Error";
        case 3:
            return "Address Error";
        case 4:
            return "Illegal Instruction";
        case 5:
            return "Zero Divide";
        case 6:
            return "CHK Instruction";
        case 7:
            return "TRAPV Instruction";
        case 8:
            return "Privilege Violation";
        case 9:
            return "Trace";
        case 10:
            return "Line 1010 Emulator";
        case 11:
            return "Line 1111 Emulator";
        case 15:
            return "Uninitialized Interrupt Vector";
        case 24:
            return "Spurious Interrupt";
        case 25:
            return "Level 1 Interrupt Autovector";
        case 26:
            return "Level 2 Interrupt Autovector";
        case 27:
            return "Level 3 Interrupt Autovector";
        case 28:
            return "Level 4 Interrupt Autovector";
        case 29:
            return "Level 5 Interrupt Autovector";
        case 30:
            return "Level 6 Interrupt Autovector";
        case 31:
            return "Level 7 Interrupt Autovector";
        case 32:
            return "Trap #0 Instruction Vector";
        case 33:
            return "Trap #1 Instruction Vector";
        case 34:
            return "Trap #2 Instruction Vector";
        case 35:
            return "Trap #3 Instruction Vector";
        case 36:
            return "Trap #4 Instruction Vector";
        case 37:
            return "Trap #5 Instruction Vector";
        case 38:
            return "Trap #6 Instruction Vector";
        case 39:
            return "Trap #7 Instruction Vector";
        case 40:
            return "Trap #8 Instruction Vector";
        case 41:
            return "Trap #9 Instruction Vector";
        case 42:
            return "Trap #10 Instruction Vector";
        case 43:
            return "Trap #11 Instruction Vector";
        case 44:
            return "Trap #12 Instruction Vector";
        case 45:
            return "Trap #13 Instruction Vector";
        case 46:
            return "Trap #14 Instruction Vector";
        case 47:
            return "Trap #15 Instruction Vector";
        }
        if (value < 64) {
            return "Unassigned, Reserved";
        }
        return "User Interrupt Vector";
    }
}

