package org.modeshape.jcr.value.binary;

import org.modeshape.common.logging.Logger;
import org.modeshape.jcr.JcrI18n;
import org.modeshape.jcr.TextExtractors;
import org.modeshape.jcr.mimetype.MimeTypeDetector;
import org.modeshape.jcr.value.BinaryKey;
import org.modeshape.jcr.value.BinaryValue;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class StrategyBinaryStore implements BinaryStore {

    protected Logger logger = Logger.getLogger(getClass());

    private Map<String, BinaryStore> binaryStores;

    public StrategyBinaryStore(Map<String, BinaryStore> binaryStores) {
        this.binaryStores = binaryStores;
    }

    /**
     * Initialize the store and get ready for use.
     */
    public void start() {
        Iterator<Map.Entry<String,BinaryStore>> it = getBinaryStoreIterator();

        while(it.hasNext()) {
            BinaryStore bs = it.next().getValue();
            bs.start();
        }

    }

    public void shutdown() {
        Iterator<Map.Entry<String,BinaryStore>> it = getBinaryStoreIterator();

        while(it.hasNext()) {
            BinaryStore bs = it.next().getValue();
            bs.shutdown();
        }
    }

    private BinaryStore getDefaultBinaryStore() {
        return binaryStores.get("default");
    }

    private Iterator<Map.Entry<String,BinaryStore>> getBinaryStoreIterator() {
        return binaryStores.entrySet().iterator();
    }

    @Override
    public long getMinimumBinarySizeInBytes() {
        long minimumBinarySize = Long.MAX_VALUE;

        Iterator<Map.Entry<String,BinaryStore>> it = getBinaryStoreIterator();

        while(it.hasNext()) {
            BinaryStore bs = it.next().getValue();
            if(minimumBinarySize > bs.getMinimumBinarySizeInBytes()) {
                minimumBinarySize = bs.getMinimumBinarySizeInBytes();
            }
        }

        return minimumBinarySize;
    }

    @Override
    public void setMinimumBinarySizeInBytes(long minSizeInBytes) {
        getDefaultBinaryStore().setMinimumBinarySizeInBytes(minSizeInBytes);
    }

    @Override
    public void setTextExtractors(TextExtractors textExtractors) {
        Iterator<Map.Entry<String,BinaryStore>> it = getBinaryStoreIterator();

        while(it.hasNext()) {
            BinaryStore bs = it.next().getValue();
            bs.setTextExtractors(textExtractors);
        }
    }

    @Override
    public void setMimeTypeDetector(MimeTypeDetector mimeTypeDetector) {
        Iterator<Map.Entry<String,BinaryStore>> it = getBinaryStoreIterator();

        while(it.hasNext()) {
            BinaryStore bs = it.next().getValue();
            bs.setMimeTypeDetector(mimeTypeDetector);
        }
    }

    @Override
    public BinaryValue storeValue(InputStream stream) throws BinaryStoreException {
        return storeValue(stream, "default");
    }

<<<<<<< HEAD:modeshape-jcr/src/main/java/org/modeshape/jcr/value/binary/ChainingBinaryStore.java
    public BinaryValue storeValue(InputStream stream, Object storageHint) throws BinaryStoreException {
        return binaryStores.get(storageHint).storeValue(stream);
=======
    public BinaryValue storeValue(InputStream stream, Object strategyHint) throws BinaryStoreException {
        BinaryValue bv = binaryStores.get(strategyHint).storeValue(stream);

        Set<BinaryKey> keys = Collections.singleton(bv.getKey());

        Iterator<Map.Entry<String,BinaryStore>> it = getBinaryStoreIterator();

        while(it.hasNext()) {
            Map.Entry<String,BinaryStore> m = it.next();

            if(!m.getKey().equals(strategyHint)) {
                m.getValue().markAsUnused(keys);
            }
        }

        return bv;
>>>>>>> ee0edd6... rename chainingbinarystore to strategybinarystore:modeshape-jcr/src/main/java/org/modeshape/jcr/value/binary/StrategyBinaryStore.java
    }

    public void moveValue(BinaryKey key, Object strategyHintSource, Object strategyHintDestination) throws BinaryStoreException {
        storeValue(binaryStores.get(strategyHintSource).getInputStream(key), strategyHintDestination);
        binaryStores.get(strategyHintSource).markAsUnused(Collections.singleton(key));
    }

    @Override
    public InputStream getInputStream(BinaryKey key) throws BinaryStoreException {
        Iterator<Map.Entry<String,BinaryStore>> it = getBinaryStoreIterator();

        while(it.hasNext()) {
            BinaryStore bs = it.next().getValue();
            try {
              return bs.getInputStream(key);
            } catch(BinaryStoreException e) {
                logger.debug(e, "Looked in BinaryStore, got exception");
            }
        }

        throw new BinaryStoreException(JcrI18n.unableToFindBinaryValue.text(key, this.toString()));
    }

    @Override
    public void markAsUnused(Iterable<BinaryKey> keys) throws BinaryStoreException {
        Iterator<Map.Entry<String,BinaryStore>> it = getBinaryStoreIterator();

        while(it.hasNext()) {
            BinaryStore bs = it.next().getValue();
            try {
                bs.markAsUnused(keys);
            } catch(BinaryStoreException e) {
                logger.debug(e, "StrategyBinaryStore raised exception");
            }
        }
    }

    @Override
    public void removeValuesUnusedLongerThan(long minimumAge, TimeUnit unit) throws BinaryStoreException {
        Iterator<Map.Entry<String,BinaryStore>> it = getBinaryStoreIterator();

        while(it.hasNext()) {
            BinaryStore bs = it.next().getValue();
            try {
                bs.removeValuesUnusedLongerThan(minimumAge, unit);
            } catch(BinaryStoreException e) {
                logger.debug(e, "StrategyBinaryStore raised exception");
            }
        }
    }

    @Override
    public String getText(BinaryValue binary) throws BinaryStoreException {
        Iterator<Map.Entry<String,BinaryStore>> it = getBinaryStoreIterator();

        while(it.hasNext()) {
            BinaryStore bs = it.next().getValue();
            try {
                return bs.getText(binary);
            } catch(BinaryStoreException e) {
                logger.debug(e, "StrategyBinaryStore raised exception");
                if(!it.hasNext()) {
                    throw e;
                }
            }
        }

        return null;
    }

    @Override
    public String getMimeType(BinaryValue binary, String name) throws IOException, RepositoryException {
        Iterator<Map.Entry<String,BinaryStore>> it = getBinaryStoreIterator();

        while(it.hasNext()) {
            BinaryStore bs = it.next().getValue();
            try {
                return bs.getMimeType(binary, name);
            } catch(BinaryStoreException e) {
                logger.debug(e, "StrategyBinaryStore raised exception");
                if(!it.hasNext()) {
                    throw e;
                }
            }
        }

        return null;
    }

    @Override
    public Iterable<BinaryKey> getAllBinaryKeys() throws BinaryStoreException {
        HashSet<BinaryKey> generatedIterable = new HashSet<BinaryKey>();

        Iterator<Map.Entry<String,BinaryStore>> binaryStoreIterator = getBinaryStoreIterator();

        while(binaryStoreIterator.hasNext()) {
            BinaryStore bs = binaryStoreIterator.next().getValue();

            Iterator<BinaryKey> childIterator = bs.getAllBinaryKeys().iterator();

            while(childIterator.hasNext()) {
                generatedIterable.add(childIterator.next());
            }
        }

        return generatedIterable;
    }

}
