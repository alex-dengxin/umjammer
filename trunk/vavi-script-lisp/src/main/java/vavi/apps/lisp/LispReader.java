/*
 * Lisp Interpreter
 *
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Original by SunSoft Java Series CD-ROM "Java by Example"
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;


/**
 * This class read S expression from a stream.
 * 
 * TODO comment implementation is bad.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 *          0.01 971203 nsano add ready <br>
 *          0.02 980130 nsano add the encoding to the reader <br>
 *          0.10 990212 nsano deprecate ready, change catch EOF <br>
 */
public final class LispReader {

    private static final char LPAREN = '(';

    private static final char RPAREN = ')';

    private static final char DOT = '.';

    private static final char QUOTE = '\'';

    private static final char DQUOTE = '"';

    private static final char SPACE = ' ';

    private static final char TAB = '\t';

    private static final char NEWLINE = '\n';

    private static final char NEWLINE2 = '\r';

    private static final char SHARP = '#';

    private static final char SEMICOLON = ';';

    private static final char EOF = (char) -1;

    /** The line number of input stream. */
    private int lineNumber;

    /**
     * Creates a Reader object which can read S expression.
     * 
     * @param reader the reader of S expression
     * @param symbolMap the Map object of lisp symbols
     */
    public LispReader(Reader reader, Map<String, LispSymbol> symbolMap) {
        this.reader = reader;
        this.symbolMap = symbolMap;

        ch = ' ';
        charsSoFar = new StringBuilder();
        quote = LispSymbol.intern(symbolMap, "quote");

        lineNumber = 1;
    }

    /**
     * Reads a next token of S expression.
     * 
     * @return the token read
     * @throws IncompleteExpressionException If a S expression reading is not completed
     * @throws InvalidLispExpressionException If a syntax error be caught.
     * @throws IOException If an IO error occurs.
     */
    public Object read() throws IncompleteExpressionException, InvalidLispExpressionException, IOException {
        charsSoFar.setLength(0);
        return readInternal();
    }

    /**
     * Reads a next token of S expression skipping white space and comment.
     * 
     * @return the token read
     * @throws IncompleteExpressionException If a S expression reading is not completed.
     * @throws InvalidLispExpressionException If a syntax error be caught.
     * @throws IOException If an IO error occurs.
     */
    public Object skipRead() throws IncompleteExpressionException, InvalidLispExpressionException, IOException {

        charsSoFar.setLength(0);
        skipWhite();
        skipComment();

        if (ch != EOF) {
            return readInternal();
        } else {
            return null;
        }
    }

    /**
     * Implements reading.
     * 
     * @throws IncompleteExpressionException If a S expression reading is not completed.
     * @throws InvalidLispExpressionException If a syntax error be caught.
     * @throws IOException If an IO error occurs.
     */
    private Object readInternal() throws IncompleteExpressionException, InvalidLispExpressionException, IOException {

        skipWhite();
        skipComment();

        switch (ch) {
        case LPAREN:
            ch = getChar();
            return readList();
        case QUOTE:
            ch = getChar();
            return new LispCons(quote, new LispCons(readInternal(), LispCons.emptyList));
        case DQUOTE:
            return readString();
        case SHARP:
            char nextch = getChar();
            ch = ' ';

            if (nextch == 't') {
                return LispBoolean.trueValue;
            } else if (nextch == 'f') {
                return LispBoolean.falseValue;
            } else {
                ch = ' ';
                readError("'" + nextch + "' unexpected:");
                break;
            }
        case RPAREN:
            ch = ' ';
            readError("')' unexpected:");
            break;
        case EOF:
// System.err.println("EOF catched");
            throw new EOFException();
        default:
            if (Character.isDigit(ch) || ch == '-') {
                return readNumber();
            } else {
                return readSymbol();
            }
        }

        return LispCons.emptyList;
    }

    /**
     * Reads a number.
     * 
     * @throws IncompleteExpressionException If a S expression reading is not completed.
     * @throws IOException If an IO error occurs.
     */
    private Object readNumber() throws IncompleteExpressionException, IOException {

        StringBuilder buf = new StringBuilder();

        while (Character.isDigit(ch) || ch == '.' || ch == '-') {
            buf.append(ch);
            ch = getChar();
        }

        String numString = buf.toString();

        if (numString.equals("-")) {
            return LispSymbol.intern(symbolMap, numString);
        }

// System.err.println("number: " + numString);
        try {
            return new Integer(numString);
        } catch (NumberFormatException e) {
            return Double.valueOf(numString);
        }
    }

    /**
     * Reads a symbol.
     * 
     * @throws IncompleteExpressionException If a S expression reading is not completed.
     * @throws IOException If an IO error occurs.
     */
    private Object readSymbol() throws IncompleteExpressionException, IOException {

        StringBuilder buf = new StringBuilder();

        while (ch != SPACE && ch != TAB && ch != NEWLINE && //
               ch != NEWLINE2 && //
               ch != SEMICOLON && ch != RPAREN && ch != LPAREN) {

            buf.append(ch);
            ch = getChar();
        }

// System.err.println("symbol: " + buf.toString().length() + ": " + buf.toString());
        return LispSymbol.intern(symbolMap, buf.toString());
    }

    /**
     * Reads a string.
     * 
     * @throws IncompleteExpressionException If a S expression reading is not completed.
     * @throws IOException If an IO error occurs.
     */
    private Object readString() throws IncompleteExpressionException, IOException {

        StringBuilder buf = new StringBuilder();

        while ((ch = getStringChar()) != '"') {
            if (ch == '\\') {
                buf.append(getStringChar());
            } else {
                buf.append(ch);
            }
        }

        ch = ' '; // Don't read another char so we don't block.
// System.err.println("string: " + buf.toString());
        return buf.toString();
    }

    /**
     * Reads a "list" primitive.
     * 
     * @throws IncompleteExpressionException If a S expression reading is not completed.
     * @throws InvalidLispExpressionException If a syntax error be caught.
     * @throws IOException If an IO error occurs.
     */
    private Object readList() throws IncompleteExpressionException, InvalidLispExpressionException, IOException {

        skipWhite();
        skipComment();

        if (ch == RPAREN) {
            ch = ' ';
            return LispCons.emptyList;
        }

        LispCons listHead = new LispCons(readInternal(), null);

        return readCdr(listHead, listHead);
    }

    /**
     * Reads "cdr" primitive.
     * 
     * @throws IncompleteExpressionException If a S expression reading is not completed.
     * @throws InvalidLispExpressionException If a syntax error be caught.
     * @throws IOException If an IO error occurs.
     */
    private Object readCdr(LispCons head, LispCons currentCons) throws IncompleteExpressionException, InvalidLispExpressionException, IOException {

        skipWhite();
        skipComment();

        while (ch != RPAREN && ch != DOT) {
            Object next = readInternal();
            LispCons newCons = new LispCons(next, null);
            currentCons.setCdr(newCons);
            currentCons = newCons;
            skipWhite();
            skipComment();
        }

        if (ch == DOT) {
            ch = getChar();
            currentCons.setCdr(readInternal());
            skipWhite();
            skipComment();

            if (ch != RPAREN) {
                readError("Expected ')':");
            }

            ch = ' ';
            return head;
        }

        ch = ' ';
        currentCons.setCdr(LispCons.emptyList);
        return head;
    }

    /**
     * Skips comment from the input stream. Comment starts by semicolon and continues to the end of line.
     * 
     * @throws IOException If an IO error occurs.
     */
    private void skipComment() throws IOException {

        if (ch == SEMICOLON) {
// System.err.println("LispReader::skipComment: " + (int) ch);

            while (ch != NEWLINE && ch != NEWLINE2 && ch != EOF) {
                ch = getCh();
// System.err.print(ch);
            }
            if (ch == NEWLINE || ch == NEWLINE2) {
                ch = getCh(); // check!
            }
        }
    }

    /**
     * Skips white spaces from the input stream. White space means Space, Tab, CR and LF.
     * 
     * @throws IOException If an IO error occurs.
     */
    private void skipWhite() throws IOException {

        while (ch == SPACE || ch == TAB || ch == NEWLINE || ch == NEWLINE2) {

            ch = getCh();
        }
// System.err.println("LispReader::skipWhite: " + (int) ch);
    }

    /**
     * Gets a character from the stream.
     * 
     * @throws IOException If an IO error occurs.
     */
    private char getCh() throws IOException {

        if (!ready()) {
            return EOF;
        }

        int next = reader.read();
// System.err.println("LispReader::getCh: " + next);

//      if (next == -1) {
//          return EOF;
//      } else {
            char nextChar = (char) next;
            charsSoFar.append(nextChar);
            return Character.toLowerCase(nextChar);
//      }
    }

    /**
     * Gets a char.
     * 
     * @throws IncompleteExpressionException If a S expression reading is not completed.
     * @throws IOException If an IO error occurs.
     */
    private char getChar() throws IncompleteExpressionException, IOException {

        if (!ready()) {
            throw new IncompleteExpressionException("Incomplete Expression:\n\n" + charsSoFar.toString().trim());
        }

// System.err.println("LispReader::getChar: enter");
        int next = reader.read();

// if (next == -1) { throw new IncompleteExpressionException("Incomplete Expression:\n\n" + charsSoFar.toString().trim()); }
         
        char nextChar = Character.toLowerCase((char) next);
        charsSoFar.append(nextChar);
        return nextChar;
    }

    /**
     * Gets a string char.
     * 
     * @throws IncompleteExpressionException If a S expression reading is not completed.
     * @throws IOException If an IO error occurs.
     */
    private char getStringChar() throws IncompleteExpressionException, IOException {

        int next = reader.read();

        if (next == -1) {
            throw new IncompleteExpressionException("Incomplete Expression:\n\n" + charsSoFar.toString().trim());
        }

        char nextChar = (char) next;
        charsSoFar.append(nextChar);
        return nextChar;
    }

    /**
     * Reads an error.
     * 
     * @throws InvalidLispExpressionException If a syntax error be caught.
     */
    private void readError(String message) throws InvalidLispExpressionException {

        String EOL = System.getProperty("line.separator");
        String errorMessage = message + EOL;

        StringBuilder sb = new StringBuilder();
        String soFar = charsSoFar.toString().trim();
        int numSpaces = soFar.length() - 1; // TODO i18n

        errorMessage += soFar + EOL;

        for (int i = 0; i < numSpaces; i++) {
            sb.append(' ');
        }

        sb.append("^ here (line:" + lineNumber + ")" + EOL);

        throw new InvalidLispExpressionException(errorMessage + sb.toString());
    }

    /**
     * Tell whether this stream is ready to be read. A buffered character stream is ready if the buffer is not empty, or if the
     * underlying character stream is ready.
     * 
     * @throws IOException If an IO error occurs
     * @deprecated
     */
    public boolean ready() throws IOException {
        return reader.ready();
    }

    /** シンボルのキャッシュ */
    private Map<String, LispSymbol> symbolMap;

    /** リーダ */
    private Reader reader;

    /** カレントの文字 */
    private char ch;

    /** 今まで読みこんだ文字列 */
    private StringBuilder charsSoFar;

    /** クオート */
    private LispSymbol quote;
}

/* */
