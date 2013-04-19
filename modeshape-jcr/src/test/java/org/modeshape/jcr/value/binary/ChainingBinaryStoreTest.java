package org.modeshape.jcr.value.binary;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.modeshape.common.annotation.ThreadSafe;
import org.modeshape.common.util.FileUtil;
import org.modeshape.common.util.IoUtil;
import org.modeshape.jcr.value.BinaryValue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertThat;

import static org.hamcrest.core.Is.is;

/**
 * A {@link BinaryStore} that chains multiple BinaryStore implementation together.
 */
@ThreadSafe
public class ChainingBinaryStoreTest extends AbstractBinaryStoreTest {

    ChainingBinaryStore store;
    BinaryStore defaultStore;
    BinaryStore alternativeStore;
    File directory;
    File altDirectory;

    protected static final int MIN_BINARY_SIZE = 20;

    @Before
    public void beforeClass() {
        Map<String, BinaryStore> stores = new LinkedHashMap<String, BinaryStore>();


        directory = new File("target/cfsbs/");
        FileUtil.delete(directory);
        directory.mkdirs();
        defaultStore = new FileSystemBinaryStore(directory);
        defaultStore.setMinimumBinarySizeInBytes(MIN_BINARY_SIZE);

        stores.put("default", defaultStore);

        altDirectory = new File("target/afsbs/");
        FileUtil.delete(altDirectory);
        altDirectory.mkdirs();
        alternativeStore = new FileSystemBinaryStore(altDirectory);
        alternativeStore.setMinimumBinarySizeInBytes(MIN_BINARY_SIZE);

        stores.put("alternative", alternativeStore);

        store = new ChainingBinaryStore(stores);
    }

    @After
    public void afterClass() {
        FileUtil.delete(directory);
    }

    @Override
    @Test(expected = BinaryStoreException.class)
    public void shouldStoreZeroLengthBinary() throws BinaryStoreException, IOException {
        //the file system binary store will not store a 0 byte size content
        super.shouldStoreZeroLengthBinary();
    }

    @Test
    public void tryItOut() throws BinaryStoreException, IOException {

        String text = randomString();

        BinaryValue v = store.storeValue(new ByteArrayInputStream(text.getBytes()));

        InputStream is = store.getInputStream(v.getKey());
        String s = IoUtil.read(is);

        assertThat(s, is(text));
    }


    @Test
    public void tryOutAltStore() throws BinaryStoreException, IOException {

        String text = randomString();

        BinaryValue v = alternativeStore.storeValue(new ByteArrayInputStream(text.getBytes()));

        InputStream is = store.getInputStream(v.getKey());
        String s = IoUtil.read(is);

        assertThat(s, is(text));
    }

    private String randomString() {

        final String textBase = "The quick brown fox jumps over the lazy dog";
        StringBuilder builder = new StringBuilder();
        Random rand = new Random();
        while (builder.length() <= MIN_BINARY_SIZE) {
            builder.append(textBase.substring(0, rand.nextInt(textBase.length())));
        }
        return builder.toString();

    }

    @Override
    protected BinaryStore getBinaryStore() {
        return store;
    }
}
