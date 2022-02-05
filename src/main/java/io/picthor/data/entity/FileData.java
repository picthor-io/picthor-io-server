package io.picthor.data.entity;

import com.realcnbs.horizon.framework.data.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileData extends AbstractEntity {

    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("JPG", "JPEG", "PNG", "GIF",
            "WBMP", "HEIC", "3FR", "ARW", "CR2", "CRW", "DCR", "DNG", "ERF", "JPG", "KDC", "MDC", "MEF", "MOS",
            "MRW", "NEF", "NRW", "ORF", "PDF", "PEF", "PPM", "RAF", "RAW", "RW2", "SR2", "SRF", "SRW", "TIF", "TIFF", "X3F");

    public static final List<String> VIDEO_EXTENSIONS = Arrays.asList("MOV", "AVI", "3GP", "3GPP", "MP4", "FLV", "HEIF", "M4V", "MKV", "MPG", "OGV", "VOB");

    public static final List<String> RAW_EXTENSIONS = Arrays.asList("3FR", "ARW", "CR2", "CRW", "DCR", "DNG", "ERF", "KDC", "MDC", "MEF", "MOS",
            "MRW", "NEF", "NRW", "ORF", "PEF", "PPM", "RAF", "RAW", "RW2", "SR2", "SRF", "SRW", "TIF", "TIFF", "X3F");

    public enum Type {IMAGE, VIDEO}

    public enum SyncState {SCANNED, MISSING, UNSUPPORTED, ERROR}

    private LocalDateTime takenAt;
    private String fullPath;
    private String dirPath;
    private String hash;
    private String fileName;
    private String baseName;
    private String extension;
    private Long sizeBytes;
    private Type type;
    private SyncState syncState;
    private Long indexNanos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long rootDirectoryId;
    private Long directoryId;
    private String meta;
    private String error;

    public String getThumbPath(String root, Integer width) {
        return getThumbDir(root, width) + "/" + hash + ".jpg";
    }

    public String getThumbDir(String root, Integer width) {
        return root + "/thumbs/" + width;
    }

}
