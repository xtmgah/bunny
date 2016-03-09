package org.rabix.common.helper;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import static com.google.common.base.Preconditions.*;

public class ChecksumHelper {

    public enum HashAlgorithm {
        SHA1, MD5, MURMUR3
    }

    private static final Logger logger = LoggerFactory.getLogger(ChecksumHelper.class);

    public static String checksum(File file, HashAlgorithm hashAlgo) {
        switch (hashAlgo) {
            case SHA1:
                return sha1(file);
            case MD5:
                return md5(file);
            case MURMUR3:
                return murmur3(file);
            default:
                // couldn't really happen but..
                throw new IllegalArgumentException("Unsupported hashing algorithm");
        }
    }

    public static String sha1(File file) {
        return standardHash(file, HashAlgorithm.SHA1);
    }

    public static String md5(File file) {
        return standardHash(file, HashAlgorithm.MD5);
    }

    public static String murmur3(File file) {
        checkNotNull(file);

        String hashed = null;
        HashFunction murmur3Hash = Hashing.murmur3_128();
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] hashedBytes = murmur3Hash.hashBytes(fileBytes).asBytes();
            hashed = HashAlgorithm.MURMUR3.name().toLowerCase() + "$" + bytesToString(hashedBytes);
        } catch (IOException e) {
            logger.error("Failed to create murmur3 checksum for {}", file);
        }
        return hashed;
    }

    private static String standardHash(File file, HashAlgorithm hash) {

        checkNotNull(file);
        checkNotNull(hash);

        MessageDigest md;
        FileInputStream fis = null;

        try {
            md = MessageDigest.getInstance(hash.name());

            fis = new FileInputStream(file);
            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();
            return hash.name().toLowerCase() + "$" + bytesToString(mdbytes);
        } catch (Exception e) {
            logger.error("Failed to create {} checksum for {}", hash.name(), file);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
        return null;
    }

    private static String bytesToString(byte[] bytes) {
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
