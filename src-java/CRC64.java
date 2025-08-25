package zlib_tiny;

public class CRC64 {

    private static final long poly = 0xC96C5795D7870F42L;
    private static final long[] crcTable = new long[256];

    private long crc = -1;

    static {
        for (int b = 0; b < crcTable.length; ++b) {
            long r = b;
            for (int i = 0; i < 8; ++i) {
                if ((r & 1) == 1)
                    r = (r >>> 1) ^ poly;
                else
                    r >>>= 1;
            }

            crcTable[b] = r;
        }
    }

    public CRC64() {
    }

    public void update(byte b) {
        crc = crcTable[(b ^ (int) crc) & 0xFF] ^ (crc >>> 8);
    }

    public void update(byte[] buf) {
        update(buf, 0, buf.length);
    }

    public void update(byte[] buf, int off, int len) {
        int end = off + len;
        
        // Process 8 bytes at a time for better performance
        int fastEnd = end - 7;
        while (off < fastEnd) {
            // Process a block of 8 bytes using inner loop
            for (int j = 0; j < 8; j++) {
                crc = crcTable[(buf[off + j] ^ (int) crc) & 0xFF] ^ (crc >>> 8);
            }
            off += 8;
        }
        
        // Process remaining bytes
        while (off < end) {
            crc = crcTable[(buf[off++] ^ (int) crc) & 0xFF] ^ (crc >>> 8);
        }
    }

    public long getValue() {
        return ~crc;
    }
}
