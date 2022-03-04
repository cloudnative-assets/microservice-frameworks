package com.ibm.epricer.svclib.objectstore.solr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converter of JSCCOLLECTION strings to objects of either Object[] or Collection<Map<String,String>> types 
 *   
 * @author Kiran Chowdhury
 */

public class JscCollectionConverter implements GenericConverter {
    private static final String PREFIX = "JSCCOLLECTION:";
    
    @Autowired
    private ObjectMapper mapper;

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(String.class, Object[].class), 
                new ConvertiblePair(String.class, Collection.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        String original = (String) source;
        if (!original.startsWith(PREFIX)) {
            throw new IllegalStateException("Invalid JSCCOLLECTION string, does not start with " + PREFIX);
        }
        
        String encoded = original.substring(PREFIX.length());
        byte[] decodedBytes = Base64.getMimeDecoder().decode(encoded);
        
        ZipInputStream unzipStream = new ZipInputStream(new ByteArrayInputStream(decodedBytes));
        try {
            ZipEntry ze = unzipStream.getNextEntry();
            if (!ze.getName().equalsIgnoreCase("zip")) {
                throw new IllegalStateException("Invalid JSCCOLLECTION string, first zip entry is '" 
                            + ze.getName() + "', while 'zip' is expected");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Invalid JSCCOLLECTION string, failed to unzip", e);
        }

        String decoded = new BufferedReader(new InputStreamReader(unzipStream)).lines().collect(Collectors.joining("\n"));

        try {
            return mapper.readValue(decoded, targetType.getType());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSCCOLLECTION string " + decoded 
                    + " cannot be converted to " + targetType, e);
        }
    }
}
