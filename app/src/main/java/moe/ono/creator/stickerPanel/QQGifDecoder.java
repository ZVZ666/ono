package moe.ono.creator.stickerPanel;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;

public class QQGifDecoder {
    private static final byte[] GIFMask = {0, 1, 0, 1};

    private static int conVertToInt(byte[] b) {
        int i = 0;
        int pos = 0;
        while (pos < b.length && pos < 4) {
            i += b[pos] << (pos * 8);
            pos++;
        }
        return i;
    }

    public static String decodeGifForLocalPath(int dwTabID, byte[] sbufID) {
        try {
            String path = Environment.getExternalStorageDirectory() + "/Android/data/com.tencent.mobileqq/Tencent/MobileQQ/.emotionsm/" + dwTabID + "/"
                    + bytesToHex(sbufID);
            String cachePath = Environment.getExternalStorageDirectory() + "/Android/data/com.tencent.mobileqq/cache/" + bytesToHex(sbufID);
            decodeGif(path, cachePath);
            return cachePath;
        } catch (Exception e) {
            return "";
        }
    }

    public static synchronized void decodeGif(String source, String dest) {
        try {
            new File(dest).delete();
            FileOutputStream out = new FileOutputStream(dest);
            FileInputStream fInp = new FileInputStream(source);
            byte[] headerMask = new byte[6];
            int read = fInp.read(headerMask);
            if (read != 6) {
                out.close();
                fInp.close();
                return;
            }
            decodeByteMask(headerMask, 0);
            out.write(headerMask);

            /**************************/
            headerMask = new byte[2];
            fInp.read(headerMask);
            decodeByteMask(headerMask, 6);
            out.write(headerMask);
            /**************************/
            headerMask = new byte[3];
            fInp.read(headerMask);
            decodeByteMask(headerMask, 6 + 2);
            out.write(headerMask);
            int decodeLength = getNeedDecodeLength(1 << ((conVertToInt(headerMask) & 7) + 1));
            /**************************/
            byte[] needDecode = new byte[decodeLength];
            fInp.read(needDecode);
            decodeByteMask(needDecode, 6 + 2 + 3);
            out.write(needDecode);
            /**************************/
            byte[] rest = new byte[1024];
            while ((read = fInp.read(rest)) != -1) {
                out.write(rest, 0, read);
            }

            out.close();
            fInp.close();


        } catch (Exception e) {

        }
    }

    //必须取偶数
    private static void decodeByteMask(byte[] data, int pos) {
        for (int i = 0; i < data.length; i++) {
            data[i] ^= GIFMask[(i + pos) % 4];
        }
    }

    private static int getNeedDecodeLength(int checkInt) {
        int mask = getMaskL(checkInt);
        if (checkInt != 1 << mask) return 0;
        return mask * 3;
    }

    private static int getMaskL(int checkInt) {
        if (checkInt <= 2) return 1;
        if (checkInt <= 4) return 2;
        if (checkInt <= 8) return 3;
        if (checkInt <= 16) return 4;
        if (checkInt <= 32) return 5;
        if (checkInt <= 64) return 6;
        if (checkInt <= 128) return 7;
        if (checkInt <= 256) return 8;
        return 9;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString().toLowerCase(Locale.ROOT);
    }
}
