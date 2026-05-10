package dk.nap.atlas.service.ai;

public class GeneratedImage {
    private final byte[] bytes;
    private final String mimeType;
    private final String source;

    public GeneratedImage(byte[] bytes, String mimeType, String source) {
        this.bytes = bytes;
        this.mimeType = mimeType;
        this.source = source;
    }

    public byte[] getBytes() { return bytes; }
    public String getMimeType() { return mimeType; }
    public String getSource() { return source; }
}
