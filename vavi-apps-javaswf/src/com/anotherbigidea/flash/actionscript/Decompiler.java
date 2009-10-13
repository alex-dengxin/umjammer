/*
 * Copyright (c) 2001, David N. Main, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. The name of the author may not be used to endorse or
 * promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.anotherbigidea.flash.actionscript;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Stack;

import com.anotherbigidea.flash.interfaces.SWFActions;


/**
 * An implementation of SWFActions that decompiles the Action Code
 */
public class Decompiler extends com.anotherbigidea.flash.writers.SWFActionsImpl implements SWFActions {
    protected Writer writer;
    protected int indent = 0;
    protected Stack<String> labelStack = new Stack<String>();
    protected Stack<Object> stack = new Stack<Object>();
    protected String[] lookupTable = null;
    // actually only 4 registers ??
    protected Object[] registers = new Object[10];
    protected Stack<Writer> writerStack = new Stack<Writer>();
    protected boolean duplicated = false;

    public Decompiler(Writer writer) {
        this.writer = writer;
    }

    public Decompiler(Writer writer, int indent) {
        this.writer = writer;
        this.indent = indent;
    }

    protected void indent() throws IOException {
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    protected String destring(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            if (s.length() >= 3) {
                return s.substring(1, s.length() - 1);
            }
            return "";
        }

        return s;
    }

    protected String string(String s) {
        StringBuffer buff = new StringBuffer("\"");

        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            switch (c) {
            case '\b':
                buff.append("\\b");
                break;
            case '\f':
                buff.append("\\f");
                break;
            case '\n':
                buff.append("\\n");
                break;
            case '\r':
                buff.append("\\r");
                break;
            case '\t':
                buff.append("\\t");
                break;
            case '\"':
                buff.append("\\\"");
                break;
            case '\\':
                buff.append("\\\\");
                break;
            default:
                buff.append(c);
                break;
            }
        }

        buff.append("\"");

        return buff.toString();
    }

    /**
     * Start of actions
     */
    public void start(int flags) throws IOException {
        writeConditions(flags);
        labelStack.clear();
        stack.clear();
        writerStack.clear();
        lookupTable = null;
    }

    protected void writeConditions(int flags) throws IOException {
        indent();
        writer.write("Conditions: " + Integer.toBinaryString(flags) + "\n\n");
    }

    protected Object pop2() {
        if (stack.isEmpty()) {
            return "void()";
        }
        duplicated = false;
        return stack.pop();
    }

    protected Object peek() {
        if (stack.isEmpty()) {
            return "void()";
        }
        return stack.peek();
    }

    /**
     * End of all action blocks
     */
    public void done() {
        //nothing
    }

    /**
     * End of actions
     */
    public void end() throws IOException {
        writer.write("\n");
    }

    /**
     * Pass through a blob of actions
     */
    public void blob(byte[] blob) {
        //nothing
    }

    /**
     * Unrecognized action code
     * @param data may be null
     */
    public void unknown(int code, byte[] data) {
        //nothing
    }

    /**
     * Target label for a jump - this method call immediately precedes the
     * target action.
     */
    public void jumpLabel(String label) throws IOException {
        //TODO:
        if (!labelStack.isEmpty()) {
            while ((!labelStack.isEmpty()) && labelStack.peek().equals(label)) {
                labelStack.pop();
                indent--;
                indent();
                writer.write("}\n\n");
            }
        }
    }

    /**
     * Comment Text - useful for debugging purposes
     */
    public void comment(String comment) throws IOException {
        writer.write("\n");
        indent();
        writer.write("/* ");
        writer.write(comment);
        writer.write(" */\n");
    }

    protected String gotoFrame;

    public void gotoFrame(int frameNumber) throws IOException {
        gotoFrame = Integer.toString(frameNumber + 1);
    }

    public void gotoFrame(String label) throws IOException {
        gotoFrame = string(label);
    }

    public void getURL(String url, String target) throws IOException {
        if (url == null) {
            url = "";
        }

        indent();
        writer.write("getURL( ");
        writer.write(string(url));

        if (target != null) {
            writer.write(", ");
            writer.write(string(target));
        }

        writer.write(" );\n");
    }

    public void nextFrame() throws IOException {
        indent();
        writer.write("nextFrame();\n");
    }

    public void prevFrame() throws IOException {
        indent();
        writer.write("prevFrame();\n");
    }

    public void play() throws IOException {
        indent();

        if (gotoFrame != null) {
            writer.write("gotoAndPlay( ");
            writer.write(gotoFrame);
            writer.write(" );\n");
            gotoFrame = null;
        } else {
            writer.write("play();\n");
        }
    }

    public void stop() throws IOException {
        indent();

        if (gotoFrame != null) {
            writer.write("gotoAndStop( ");
            writer.write(gotoFrame);
            writer.write(" );\n");
            gotoFrame = null;
        } else {
            writer.write("stop();\n");
        }
    }

    public void toggleQuality() throws IOException {
        indent();
        writer.write("toggleHighQuality();\n");
    }

    public void stopSounds() throws IOException {
        indent();
        writer.write("stopAllSounds();\n");
    }

    public void waitForFrame(int frameNumber, String jumpLabel) throws IOException {
        writer.write("\n");
        indent();
        writer.write("ifFrameLoaded( ");
        writer.write(Integer.toString(frameNumber + 1));
        writer.write(" )\n");
        indent();
        writer.write("{\n");
        indent++;

        labelStack.push(jumpLabel);
    }

    public void setTarget(String target) throws IOException {
        if ((target == null) || (target.length() == 0)) {
            indent--;
            indent();
            writer.write("}\n\n");
        } else {
            writer.write("\n");
            indent();
            writer.write("tellTarget( ");
            writer.write(string(target));
            writer.write(" )\n");
            indent();
            writer.write("{\n");
            indent++;
        }
    }

    protected void operator(String op) throws IOException {
        Object one = pop2();
        Object two = pop2();

        if (one instanceof StringBuffer) {
            one = "(" + one + ")";
        }

        if (two instanceof StringBuffer) {
            two = "(" + two + ")";
        }

        StringBuffer buff = new StringBuffer();

        buff.append(two.toString());
        buff.append(op);
        buff.append(one.toString());

        stack.push(buff);
    }

    public void add() throws IOException {
        operator(" + ");
    }

    public void substract() throws IOException {
        operator(" - ");
    }

    public void multiply() throws IOException {
        operator(" * ");
    }

    public void divide() throws IOException {
        operator(" / ");
    }

    public void equals() throws IOException {
        operator(" == ");
    }

    public void lessThan() throws IOException {
        operator(" < ");
    }

    public void and() throws IOException {
        operator(" && ");
    }

    public void or() throws IOException {
        operator(" || ");
    }

    public void not() throws IOException {
        StringBuffer buff = new StringBuffer("not( ");
        buff.append(pop2());
        buff.append(" )");

        stack.push(buff);
    }

/*
    public void stringEquals() throws IOException;
    public void stringLength() throws IOException;
    public void concat() throws IOException;
    public void substring() throws IOException;
    public void stringLessThan() throws IOException;
    public void stringLengthMB() throws IOException;
    public void substringMB() throws IOException;
*/
    public void toInteger() throws IOException {
        stack.push("int( " + pop2().toString() + " )");
    }

/*
    public void charToAscii() throws IOException;
    public void asciiToChar() throws IOException;
    public void charMBToAscii() throws IOException;
    public void asciiToCharMB() throws IOException;

    public void jump( String jumpLabel ) throws IOException;
    public void ifJump( String jumpLabel ) throws IOException;

    public void call() throws IOException;
 */
    public void getVariable() throws IOException {
        Object name = pop2();

        if ((name instanceof String) && ((String) name).startsWith("\"")) {
            name = destring((String) name);
        } else {
            name = "eval( " + name.toString() + " )";
        }

        stack.push(name);
    }

    public void setVariable() throws IOException {
        indent();

        String value = (stack.size() < 2) ? "void()" : pop2().toString();
        String varname = destring(pop2().toString());
        writer.write(varname + " = " + value);
        if (!value.trim().endsWith("}")) {
            writer.write(";\n");
        }
    }

/*
    // --------------------------------------------------------
    public static final int GET_URL_SEND_VARS_NONE = 0;  //don't send variables
    public static final int GET_URL_SEND_VARS_GET  = 1;  //send vars using GET
    public static final int GET_URL_SEND_VARS_POST = 2;  //send vars using POST

    public static final int GET_URL_MODE_LOAD_MOVIE_INTO_LEVEL  = 0;
    public static final int GET_URL_MODE_LOAD_MOVIE_INTO_SPRITE = 1;
    public static final int GET_URL_MODE_LOAD_VARS_INTO_LEVEL   = 3;
    public static final int GET_URL_MODE_LOAD_VARS_INTO_SPRITE  = 4;

    public void getURL( int sendVars, int loadMode ) throws IOException;
    // --------------------------------------------------------

    public void gotoFrame( boolean play ) throws IOException;
    public void setTarget() throws IOException;
    public void getProperty() throws IOException;
    public void setProperty() throws IOException;
    public void cloneSprite() throws IOException;
    public void removeSprite() throws IOException;
    public void startDrag() throws IOException;
    public void endDrag() throws IOException;
    public void waitForFrame( String jumpLabel ) throws IOException;

*/
    public void trace() throws IOException {
        indent();
        writer.write("trace( " + pop2().toString() + " );\n");
    }

    public void getTime() throws IOException {
        stack.push("getTimer()");
    }

    public void randomNumber() throws IOException {
        stack.push("random( " + destring(pop2().toString()) + " )");
    }

    public void callFunction() throws IOException {
        String name = destring(pop2().toString());
        int numargs = ((Number) pop2()).intValue();

        StringBuffer buff = new StringBuffer();
        buff.append(name);
        buff.append("(");

        for (int i = 0; i < numargs; i++) {
            if (i > 0) {
                buff.append(",");
            }
            buff.append(" ");
            buff.append(destring(pop2().toString()));
        }

        if (numargs > 0) {
            buff.append(" ");
        }
        buff.append(")");
        stack.push(buff);
    }

    public void callMethod() throws IOException {
        String name = pop2().toString();
        Object obj = pop2().toString();
        int numargs = ((Number) pop2()).intValue();

        if (obj instanceof StringBuffer) {
            obj = "(" + obj + ")";
        }

        if (name.startsWith("\"")) {
            name = "." + destring(name);
        } else {
            name = "[" + name + "]";
        }

        StringBuffer buff = new StringBuffer();
        buff.append(obj.toString());
        buff.append(name);
        buff.append("(");

        for (int i = 0; i < numargs; i++) {
            if (i > 0) {
                buff.append(",");
            }
            buff.append(" ");
            buff.append(destring(pop2().toString()));
        }

        if (numargs > 0) {
            buff.append(" ");
        }
        buff.append(")");
        stack.push(buff);
    }

    public void lookupTable(String[] values) throws IOException {
        lookupTable = values;
    }

    public void startFunction(String name, String[] paramNames) throws IOException {
        if ((name == null) || (name.trim().length() == 0)) {
            writerStack.push(writer);
            writer = new StringWriter();
            writer.write("function(");
        } else {
            indent();
            writer.write("function " + name + "(");
        }

        if (paramNames == null) {
            paramNames = new String[0];
        }
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0) {
                writer.write(",");
            }
            writer.write(" ");
            writer.write(paramNames[i]);
        }

        if (paramNames.length > 0) {
            writer.write(" ");
        }
        writer.write(")\n");
        indent();
        writer.write("{\n");
        indent++;
    }

    public void endBlock() throws IOException {
        indent--;
        indent();
        writer.write("}\n\n");

        if (!writerStack.isEmpty()) {
            stack.push(((StringWriter) writer).getBuffer());
            writer = writerStack.pop();
        }
    }

    public void defineLocalValue() throws IOException {
        String value = pop2().toString();
        String name = destring(pop2().toString());

        indent();
        writer.write("var " + name + " = " + value + ";\n");
    }

    public void defineLocal() throws IOException {
        String name = destring(pop2().toString());

        indent();
        writer.write("var " + name + ";\n");
    }

/*
    public void deleteProperty() throws IOException;
    public void deleteThreadVars() throws IOException;

    public void enumerate() throws IOException;
*/
    public void typedEquals() throws IOException {
        equals();
    }

/*
    public void getMember() throws IOException;

    public void initArray() throws IOException;
    public void initObject() throws IOException;
    public void newMethod() throws IOException;
    public void newObject() throws IOException;
    public void setMember() throws IOException;
    public void getTargetPath() throws IOException;
*/
    public void startWith() throws IOException {
        indent();
        writer.write("with( " + destring(pop2().toString()) + " )\n");
        indent();
        writer.write("{\n");
        indent++;
    }

    public void convertToNumber() throws IOException {
        stack.push("Number( " + pop2().toString() + " )");
    }

    public void convertToString() throws IOException {
        stack.push("String( " + pop2().toString() + " )");
    }

    public void typeOf() throws IOException {
        stack.push("typeOf( " + pop2().toString() + " )");
    }

    public void typedAdd() throws IOException {
        add();
    }

    public void typedLessThan() throws IOException {
        lessThan();
    }

    public void modulo() throws IOException {
        operator(" % ");
    }

    public void bitAnd() throws IOException {
        operator(" & ");
    }

    public void bitOr() throws IOException {
        operator(" | ");
    }

    public void bitXor() throws IOException {
        operator(" % ");
    }

    public void shiftLeft() throws IOException {
        operator(" << ");
    }

    public void shiftRight() throws IOException {
        operator(" >> ");
    }

    public void shiftRightUnsigned() throws IOException {
        operator(" >>> ");
    }

    public void decrement() throws IOException {
        Object value = pop2();

        if (value instanceof StringBuffer) {
            value = "(" + value + ")";
        }

        StringBuffer buff = new StringBuffer(value.toString());
        buff.append(" - 1");
        stack.push(buff);
    }

    public void increment() throws IOException {
        Object value = pop2();

        if (value instanceof StringBuffer) {
            value = "(" + value + ")";
        }

        StringBuffer buff = new StringBuffer(value.toString());
        buff.append(" + 1");
        stack.push(buff);
    }

    public void duplicate() throws IOException {
        stack.push(peek());
        duplicated = true;
    }

/*    public void returnValue() throws IOException;
    */
    public void swap() throws IOException {
        Object top = pop2();
        Object two = pop2();
        stack.push(top);
        stack.push(two);
    }

    public void storeInRegister(int registerNumber) throws IOException {
        if (registerNumber >= registers.length) {
            throw new IOException("Register index out of bounds");
        }

        registers[registerNumber] = peek();
    }

    public void push(double value) throws IOException {
        stack.push(new Double(value));
    }

    public void pushNull() throws IOException {
        stack.push("null");
    }

    public void pushRegister(int registerNumber) throws IOException {
        if (registerNumber >= registers.length) {
            throw new IOException("Register index out of bounds");
        }

        Object value = registers[registerNumber];
        if (value == null) {
            value = "null";
        }

        stack.push(value);
    }

    public void push(boolean value) throws IOException {
        stack.push(new Boolean(value));
    }

    public void push(int value) throws IOException {
        stack.push(new Integer(value));
    }

    public void push(String value) throws IOException {
        stack.push(string(value));
    }

    public void push(float value) throws IOException {
        stack.push(new Float(value));
    }

    public void pop() throws IOException {
        indent();
        writer.write(pop2().toString() + ";\n");
    }

    public void lookup(int dictionaryIndex) throws IOException {
        if ((lookupTable == null) || (dictionaryIndex >= lookupTable.length)) {
            throw new IOException("Lookup index is out of bounds of the lookup table");
        }

        stack.push(string(lookupTable[dictionaryIndex]));
    }
}
