/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.dna.common.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class StringUtilTest {

    public void compareSeparatedLines( Object... lines ) {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(content);
        for (Object line : lines) {
            stream.println(line);
        }
        List<String> actualLines = StringUtil.splitLines(content.toString());
        assertArrayEquals(lines, actualLines.toArray());
    }

    @Test
    public void splitLinesShouldWorkCorrectly() {
        compareSeparatedLines("Line 1", "Line 2", "Line 3", "Line 4");
    }

    @Test( expected = IllegalArgumentException.class )
    public void createStringShouldFailIfNoPatternSupplied() {
        StringUtil.createString(null, (Object[])null);
    }

    @Test
    public void createStringShouldAllowNoParametersSupplied() {
        assertThat(StringUtil.createString("test", (Object[])null), is("test"));
    }

    @Test
    public void createStringShouldCreateStringFromPattern() {
        String pattern = "This {0} is {1} should {2} not {3} last {4}";
        assertEquals("This one is two should three not four last five", StringUtil.createString(pattern,
                                                                                                "one",
                                                                                                "two",
                                                                                                "three",
                                                                                                "four",
                                                                                                "five"));
    }

    @Test( expected = IllegalArgumentException.class )
    public void createStringShouldFailIfTooFewArgumentsSupplied() {
        String pattern = "This {0} is {1} should {2} not {3} last {4}";
        try {
            StringUtil.createString(pattern, "one", "two", "three", "four");
        } catch (IllegalArgumentException err) {
            System.err.println(err);
            throw err;
        }
    }

    @Test( expected = IllegalArgumentException.class )
    public void createStringShouldFailIfTooManyArgumentsSupplied() {
        String pattern = "This {0} is {1} should {2} not {3} last {4}";
        try {
            StringUtil.createString(pattern, "one", "two", "three", "four", "five", "six");
        } catch (IllegalArgumentException err) {
            System.err.println(err);
            throw err;
        }
    }

    @Test
    public void createStringExceptionMessageShouldbeGrammaticallyCorrect() {
        String pattern = "One = {0}";
        try {
            StringUtil.createString(pattern);
        } catch (IllegalArgumentException err) {
            assertThat(err.getMessage().startsWith("0 parameters supplied, but 1 parameter required"), is(true));
        }
        pattern = "One";
        try {
            StringUtil.createString(pattern, "one");
        } catch (IllegalArgumentException err) {
            assertThat(err.getMessage().startsWith("1 parameter supplied, but 0 parameters required"), is(true));
        }
        pattern = "One = {0}, Two = {1}";
        try {
            StringUtil.createString(pattern);
        } catch (IllegalArgumentException err) {
            assertThat(err.getMessage().startsWith("0 parameters supplied, but 2 parameters required"), is(true));
        }
    }

    @Test
    public void setLengthShouldTruncateStringsThatAreTooLong() {
        assertEquals("This is the st", StringUtil.setLength("This is the string", 14, ' '));
    }

    @Test
    public void setLengthShouldAppendCharacterForStringsThatAreTooShort() {
        assertEquals("This      ", StringUtil.setLength("This", 10, ' '));
    }

    @Test
    public void setLengthShouldNotRemoveLeadingWhitespace() {
        assertEquals(" This     ", StringUtil.setLength(" This", 10, ' '));
        assertEquals("\tThis     ", StringUtil.setLength("\tThis", 10, ' '));
    }

    @Test
    public void setLengthShouldAppendCharacterForEmptyStrings() {
        assertEquals("          ", StringUtil.setLength("", 10, ' '));
    }

    @Test
    public void setLengthShouldAppendCharacterForNullStrings() {
        assertEquals("          ", StringUtil.setLength(null, 10, ' '));
    }

    @Test
    public void setLengthShouldReturnStringsThatAreTheDesiredLength() {
        assertEquals("This is the string", StringUtil.setLength("This is the string", 18, ' '));
    }

    @Test
    public void justifyLeftShouldTruncateStringsThatAreTooLong() {
        assertEquals("This is the st", StringUtil.justifyLeft("This is the string", 14, ' '));
    }

    @Test
    public void justifyLeftShouldAppendCharacterForStringsThatAreTooShort() {
        assertEquals("This      ", StringUtil.justifyLeft("This", 10, ' '));
    }

    @Test
    public void justifyLeftShouldRemoveLeadingWhitespace() {
        assertEquals("This      ", StringUtil.justifyLeft(" This", 10, ' '));
        assertEquals("This      ", StringUtil.justifyLeft("\tThis", 10, ' '));
    }

    @Test
    public void justifyLeftShouldAppendCharacterForEmptyStrings() {
        assertEquals("          ", StringUtil.justifyLeft("", 10, ' '));
    }

    @Test
    public void justifyLeftShouldAppendCharacterForNullStrings() {
        assertEquals("          ", StringUtil.justifyLeft(null, 10, ' '));
    }

    @Test
    public void justifyLeftShouldReturnStringsThatAreTheDesiredLength() {
        assertEquals("This is the string", StringUtil.justifyLeft("This is the string", 18, ' '));
    }

    @Test
    public void justifyRightShouldTruncateStringsThatAreTooLong() {
        assertEquals(" is the string", StringUtil.justifyRight("This is the string", 14, ' '));
    }

    @Test
    public void justifyRightShouldPrependCharacterForStringsThatAreTooShort() {
        assertEquals("      This", StringUtil.justifyRight("This", 10, ' '));
    }

    @Test
    public void justifyRightShouldPrependCharacterForEmptyStrings() {
        assertEquals("          ", StringUtil.justifyRight("", 10, ' '));
    }

    @Test
    public void justifyRightShouldPrependCharacterForNullStrings() {
        assertEquals("          ", StringUtil.justifyRight(null, 10, ' '));
    }

    @Test
    public void justifyRightShouldReturnStringsThatAreTheDesiredLength() {
        assertEquals("This is the string", StringUtil.justifyRight("This is the string", 18, ' '));
    }

    @Test
    public void justifyCenterShouldTruncateStringsThatAreTooLong() {
        assertEquals("This is the st", StringUtil.justifyCenter("This is the string", 14, ' '));
    }

    @Test
    public void justifyCenterShouldPrependAndAppendSameNumberOfCharacterForStringsThatAreTooShortButOfAnEvenLength() {
        assertEquals("   This   ", StringUtil.justifyCenter("This", 10, ' '));
    }

    @Test
    public void justifyCenterShouldPrependOneMoreCharacterThanAppendingForStringsThatAreTooShortButOfAnOddLength() {
        assertEquals("   Thing  ", StringUtil.justifyCenter("Thing", 10, ' '));
    }

    @Test
    public void justifyCenterShouldPrependCharacterForEmptyStrings() {
        assertEquals("          ", StringUtil.justifyCenter("", 10, ' '));
    }

    @Test
    public void justifyCenterShouldPrependCharacterForNullStrings() {
        assertEquals("          ", StringUtil.justifyCenter(null, 10, ' '));
    }

    @Test
    public void justifyCenterShouldReturnStringsThatAreTheDesiredLength() {
        assertEquals("This is the string", StringUtil.justifyCenter("This is the string", 18, ' '));
    }

    @Test
    public void truncateShouldReturnEmptyStringIfNullReferenceIsSupplied() {
        assertThat(StringUtil.truncate(null, 0), is(""));
        assertThat(StringUtil.truncate(null, 1), is(""));
        assertThat(StringUtil.truncate(null, 100), is(""));
    }

    @Test( expected = IllegalArgumentException.class )
    public void truncateShouldNotAllowNegativeLength() {
        StringUtil.truncate("some string", -1);
    }

    @Test
    public void truncateShouldReturnEmptyStringForMaximumLengthOfZero() {
        String str = "This is the string with some text";
        assertThat(StringUtil.truncate(str, 0), is(""));
        assertThat(StringUtil.truncate("", 0), is(""));
        assertThat(StringUtil.truncate(str, 0, "123"), is(""));
        assertThat(StringUtil.truncate("", 0, "123"), is(""));
    }

    @Test
    public void truncateShouldNotTruncateStringShorterThanMaximumLength() {
        String str = "This is the string with some text";
        assertThat(StringUtil.truncate(str, str.length() + 2), is(str));
        assertThat(StringUtil.truncate(str, str.length() + 2, null), is(str));
        assertThat(StringUtil.truncate(str, str.length() + 2, "really long suffix"), is(str));
    }

    @Test
    public void truncateShouldNotTruncateStringWithLengthEqualToMaximumLength() {
        String str = "This is the string with some text";
        assertThat(StringUtil.truncate(str, str.length()), is(str));
        assertThat(StringUtil.truncate(str, str.length(), null), is(str));
        assertThat(StringUtil.truncate(str, str.length(), "really long suffix"), is(str));
    }

    @Test
    public void truncateShouldProperlyTruncateStringWithLengthGreaterThanMaximumLength() {
        String str = "This is the string";
        assertThat(StringUtil.truncate(str, str.length() - 1), is("This is the st..."));
        assertThat(StringUtil.truncate(str, str.length() - 1, null), is("This is the st..."));
        assertThat(StringUtil.truncate(str, str.length() - 1, "X"), is("This is the striX"));
    }

    @Test
    public void truncateShouldProperlyTruncateStringWithLengthGreaterThanMaximumLengthAndMaximumLengthLongerThanPrefixLength() {
        String str = "This is the string";
        assertThat(StringUtil.truncate(str, 2), is(".."));
        assertThat(StringUtil.truncate(str, 2, null), is(".."));
        assertThat(StringUtil.truncate(str, 1, "XX"), is("X"));
    }

    @Test
    public void readShouldReturnEmptyStringForNullInputStream() throws Exception {
        assertThat(StringUtil.read((InputStream)null), is(""));
    }

    @Test
    public void readShouldReturnEmptyStringForNullReader() throws Exception {
        assertThat(StringUtil.read((Reader)null), is(""));
    }

    @Test
    public void readShouldReadInputStreamCorrectlyAndShouldCloseStream() throws Exception {
        // Read content shorter than buffer size ...
        String content = "This is the way to grandma's house.";
        InputStream stream = new ByteArrayInputStream(content.getBytes());
        InputStreamWrapper wrapper = new InputStreamWrapper(stream);
        assertThat(wrapper.isClosed(), is(false));
        assertThat(StringUtil.read(wrapper), is(content));
        assertThat(wrapper.isClosed(), is(true));

        // Read content longer than buffer size ...
        for (int i = 0; i != 10; ++i) {
            content += content; // note this doubles each time!
        }
        stream = new ByteArrayInputStream(content.getBytes());
        wrapper = new InputStreamWrapper(stream);
        assertThat(wrapper.isClosed(), is(false));
        assertThat(StringUtil.read(wrapper), is(content));
        assertThat(wrapper.isClosed(), is(true));
    }

    @Test
    public void readShouldReadReaderCorrectlyAndShouldCloseStream() throws Exception {
        // Read content shorter than buffer size ...
        String content = "This is the way to grandma's house.";
        Reader reader = new StringReader(content);
        ReaderWrapper wrapper = new ReaderWrapper(reader);
        assertThat(wrapper.isClosed(), is(false));
        assertThat(StringUtil.read(wrapper), is(content));
        assertThat(wrapper.isClosed(), is(true));

        // Read content longer than buffer size ...
        for (int i = 0; i != 10; ++i) {
            content += content; // note this doubles each time!
        }
        reader = new StringReader(content);
        wrapper = new ReaderWrapper(reader);
        assertThat(wrapper.isClosed(), is(false));
        assertThat(StringUtil.read(wrapper), is(content));
        assertThat(wrapper.isClosed(), is(true));
    }

    @Test
    public void getStackTraceShouldReturnStackTrace() {
        String msg = "This is the message for a test exception";
        Throwable t = new IllegalArgumentException(msg);
        String trace = StringUtil.getStackTrace(t);
        assertThat(trace, containsString(msg));
        assertThat(trace, containsString(this.getClass().getName()));
    }

    @Test
    public void readableStringShouldReturnStringForNull() {
        assertThat(StringUtil.readableString((Object)null), is("null"));
    }

    @Test
    public void readableStringShouldReturnStringFormOfBoolean() {
        assertThat(StringUtil.readableString(true), is(Boolean.TRUE.toString()));
        assertThat(StringUtil.readableString(false), is(Boolean.FALSE.toString()));
        assertThat(StringUtil.readableString(Boolean.TRUE), is(Boolean.TRUE.toString()));
        assertThat(StringUtil.readableString(Boolean.FALSE), is(Boolean.FALSE.toString()));
    }

    @Test
    public void readableStringShouldReturnStringFormOfNumber() {
        assertThat(StringUtil.readableString(1), is("1"));
        assertThat(StringUtil.readableString(-513), is("-513"));
        assertThat(StringUtil.readableString(-513.3f), is("-513.3"));
        assertThat(StringUtil.readableString(-513.3d), is("-513.3"));
        assertThat(StringUtil.readableString(new Short((short)1)), is("1"));
        assertThat(StringUtil.readableString(new Integer(-513)), is("-513"));
        assertThat(StringUtil.readableString(new Float(-513.3f)), is("-513.3"));
        assertThat(StringUtil.readableString(new Double(-513.3d)), is("-513.3"));
    }

    @Test
    public void readableStringShouldWrapObjectArrayWithSquareBraces() {
        assertThat(StringUtil.readableString(new int[] {}), is("[]"));
        assertThat(StringUtil.readableString(new int[] {1}), is("[ 1 ]"));
        assertThat(StringUtil.readableString(new int[] {1, 2, 3, 4}), is("[ 1, 2, 3, 4 ]"));
        assertThat(StringUtil.readableString(new short[] {1, 2, 3, 4}), is("[ 1, 2, 3, 4 ]"));
        assertThat(StringUtil.readableString(new boolean[] {true, false}), is("[ true, false ]"));
        assertThat(StringUtil.readableString(new long[] {1, 2, 3, 4}), is("[ 1, 2, 3, 4 ]"));
        assertThat(StringUtil.readableString(new float[] {51.0f, 52.0f, 53.0f, 54.0f}), is("[ 51.0, 52.0, 53.0, 54.0 ]"));
        assertThat(StringUtil.readableString(new double[] {51.0d, 52.0d, 53.0d, 54.0d}), is("[ 51.0, 52.0, 53.0, 54.0 ]"));
    }

    @Test
    public void readableStringShouldHandleEmptyArraysOfArrays() {
        assertThat(StringUtil.readableString(new int[][] {}), is("[[]]"));
        assertThat(StringUtil.readableString(new boolean[][][][][][] {}), is("[[[[[[]]]]]]"));
        assertThat(StringUtil.readableString(new ArrayList<List<List<?>>>()), is("[]"));
    }

    @Test
    public void readableStringShouldHandleNestedObjects() {
        assertThat(StringUtil.readableString(new int[][] {new int[] {1, 2}, new int[] {3, 4}}), is("[ [ 1, 2 ], [ 3, 4 ] ]"));
        List<String> list1 = new ArrayList<String>();
        list1.add("a1");
        list1.add("a2");
        List<String> list2 = new ArrayList<String>();
        list2.add("b1");
        list2.add("b2");
        List<List<String>> list3 = new ArrayList<List<String>>();
        list3.add(list1);
        list3.add(list2);
        assertThat(StringUtil.readableString(list3), is("[ [ \"a1\", \"a2\" ], [ \"b1\", \"b2\" ] ]"));
    }

    @Test( expected = IllegalArgumentException.class )
    public void normalizeShouldFailIfTextNull() {
        StringUtil.normalize(null);
    }

    @Test
    public void normalizeShouldRemoveLeadingTrailingWhitespace() {
        assertThat(StringUtil.normalize(" \t\n test this \t"), is("test this"));
    }

    @Test
    public void normalizeShouldReduceInternalWhitespace() {
        assertThat(StringUtil.normalize("test \t\n\r this"), is("test this"));
    }

    @Test
    public void normalizeShouldReturnEqualStringIfNothingToNormalize() {
        assertThat(StringUtil.normalize("test this"), is("test this"));
    }

    protected class InputStreamWrapper extends InputStream {

        private boolean closed = false;
        private final InputStream stream;

        protected InputStreamWrapper( InputStream stream ) {
            this.stream = stream;
        }

        public boolean isClosed() {
            return closed;
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        @Override
        public void close() throws IOException {
            stream.close();
            this.closed = true;
        }

    }

    protected class ReaderWrapper extends Reader {

        private boolean closed = false;
        private final Reader reader;

        protected ReaderWrapper( Reader reader ) {
            this.reader = reader;
        }

        public boolean isClosed() {
            return closed;
        }

        @Override
        public void close() throws IOException {
            reader.close();
            this.closed = true;
        }

        @Override
        public int read( char[] cbuf,
                         int off,
                         int len ) throws IOException {
            return reader.read(cbuf, off, len);
        }
    }

}
