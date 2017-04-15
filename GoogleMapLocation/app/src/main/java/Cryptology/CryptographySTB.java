package Cryptology;

/**
 * Created by Kirill on 08.04.2017.
 */

import android.util.Base64;

import org.apache.commons.lang3.ArrayUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;

//import javax.xml.bind.DatatypeConverter;

public class CryptographySTB {

    private static byte[] X_bytes = new byte[32];
    private static String H_value = "B194BAC80A08F53B366D008E584A5DE48504FA9D1BB6C7AC252E72C202FDCE0D";
    private static byte[] H_TABLE = new byte[]{
            -79, -108, -70, -56, 10, 8, -11, 59, 54, 109, 0, -114, 88, 74, 93, -28, -123, 4, -6, -99, 27, -74, -57, -84,
            37, 46, 114, -62, 2, -3, -50, 13, 91, -29, -42, 18, 23, -71, 97, -127, -2, 103, -122, -83, 113, 107, -119,
            11, 92, -80, -64, -1, 51, -61, 86, -72, 53, -60, 5, -82, -40, -32, 127, -103, -31, 43, -36, 26, -30, -126,
            87, -20, 112, 63, -52, -16, -107, -18, -115, -15, -63, -85, 118, 56, -97, -26, 120, -54, -9, -58, -8, 96,
            -43, -69, -100, 79, -13, 60, 101, 123, 99, 124, 48, 106, -35, 78, -89, 121, -98, -78, 61, 49, 62, -104, -75,
            110, 39, -45, -68, -49, 89, 30, 24, 31, 76, 90, -73, -109, -23, -34, -25, 44, -113, 12, 15, -90, 45, -37, 73,
            -12, 111, 115, -106, 71, 6, 7, 83, 22, -19, 36, 122, 55, 57, -53, -93, -125, 3, -87, -117, -10, -110, -67,
            -101, 28, -27, -47, 65, 1, 84, 69, -5, -55, 94, 77, 14, -14, 104, 32, -128, -86, 34, 125, 100, 47, 38, -121,
            -7, 52, -112, 64, 85, 17, -66, 50, -105, 19, 67, -4, -102, 72, -96, 42, -120, 95, 25, 75, 9, -95, 126, -51,
            -92, -48, 21, 68, -81, -116, -91, -124, 80, -65, 102, -46, -24, -118, -94, -41, 70, 82, 66, -88, -33, -77,
            105, 116, -59, 81, -21, 35, 41, 33, -44, -17, -39, -76, 58, 98, 40, 117, -111, 20, 16, -22, 119, 108, -38, 29
    };
    private static byte[] s_bytes = new byte[16];
    private static byte[] h_bytes = hexStringToByteArray(H_value);
    private static byte MASK = (byte) Integer.parseInt("11111111", 2);
    private static byte[] a_X1;
    private static byte[] b_X2;
    private static byte[] d_X4;
    private static byte[] c_X3;
    private static byte[] Y;
    private static long Two_In32 = 4294967296L;
    private static long Two_In24 = 16777216L;
    private static long Two_In16 = 65536L;
    private static long Two_In8 = 256L;
    private static int T;
    private static int flag;
    private static long length;
    private static long Start;
    private static long Stop;

    public static String getHash(String path) {//Стартовая функция ( path - путь к файлу, который требуется захэшировать)
        RandomAccessFile file = null;
        long point = 0;
        long current;
        try {
            file = new RandomAccessFile(path, "rw");
            length = file.length();
            point = ((int) (length / 32) * 32);
            while (file.getFilePointer() != length) {
                //System.out.println(file.getFilePointer());
                if (file.getFilePointer() == point) {
                    flag = file.read(X_bytes = new byte[(int) (length - file.getFilePointer())]);
                    madeMultiple();
                } else {
                    flag = file.read(X_bytes);
                }
                s_bytes = XOR(s_bytes, getDisplay1(ArrayUtils.addAll(X_bytes, h_bytes)));
                h_bytes = getDisplay2(ArrayUtils.addAll(X_bytes, h_bytes));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long fileLength = length * 8;
        Y = getDisplay2(ArrayUtils.addAll(getWordForFileLength(fileLength), ArrayUtils.addAll(s_bytes, h_bytes)));
        Formatter formatter = new Formatter();
        for (byte b : Y) {
            formatter.format("%02x", b);
        }
        String hex = formatter.toString();
        return hex.toUpperCase();

    }

    public static void madeMultiple() {//добавление к байтовой последовательности 0-ей в конец, если длина последовательности < 32
        T = (X_bytes.length % 32);
        for (int i = 0; i < (32 - T); i++) {
            X_bytes = Arrays.copyOf(X_bytes, 32);
        }
    }

    public static byte[] getDisplay1(byte[] XandH) {// отображение1 принимает массив байт размером 64
        byte[] u1_d1 = Arrays.copyOfRange(XandH, 0, 16);
        byte[] u2_d1 = Arrays.copyOfRange(XandH, 16, 32);
        byte[] u3_d1 = Arrays.copyOfRange(XandH, 32, 48);
        byte[] u4_d1 = Arrays.copyOfRange(XandH, 48, 64);
        byte[] encrypt_result = encrypt(ArrayUtils.addAll(u1_d1, u2_d1), XOR(u3_d1, u4_d1));
        //byte [] ds = XOR((XOR(encrypt_result,u3_d1)),u4_d1);
        //System.out.println(ds);
        return XOR((XOR(encrypt_result, u3_d1)), u4_d1);
    }

    public static byte[] getDisplay2(byte[] XandH) {// отображение 2 принимает массив байт размером 64
        byte[] u1_d2 = Arrays.copyOfRange(XandH, 0, 32);
        byte[] u2_d2 = Arrays.copyOfRange(XandH, 32, 64);
        byte[] u1 = Arrays.copyOfRange(XandH, 0, 16);
        byte[] u2 = Arrays.copyOfRange(XandH, 16, 32);
        // byte [] mask = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        byte[] key_1 = ArrayUtils.addAll(getDisplay1(XandH), Arrays.copyOfRange(XandH, 48, 64));//u4
        byte[] key_2 = ArrayUtils.addAll(XORWithOnes(getDisplay1(XandH)), Arrays.copyOfRange(XandH, 32, 48));//u3
        byte[] first_encrypt_path_result = XOR(encrypt(key_1, u1), u1);//u1)d2
        byte[] second_encrypt_path_result = XOR(encrypt(key_2, u2), u2);//u2_d2

        return ArrayUtils.addAll(first_encrypt_path_result, second_encrypt_path_result);
    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    public static byte[] XOR(byte[] u_bytes1, byte[] u_bytes2) {// операция Xor принимает параметры размером 16
        byte[] result = new byte[u_bytes1.length];
        for (int i = 0; i < u_bytes1.length; i++) {
            //u_bytes2[i] ^= u_bytes1[i];
            result[i] = (byte) (u_bytes1[i] ^ u_bytes2[i]);
        }
        return result;
    }

    private static byte[] XORWithOnes(byte[] array) {// операция XOR с маской
        byte[] result = new byte[array.length];
        int currentIndex;
        for (currentIndex = 0; currentIndex < array.length; currentIndex++) {
            result[currentIndex] = (byte) (array[currentIndex] ^ MASK);
        }
        return result;
    }

    public static byte[] encrypt(byte key[], byte[] value) {//Функция шифрования, принимает ключ и значение для шифрования
        ArrayList<byte[]> K;
        a_X1 = Arrays.copyOfRange(value, 0, 4);
        b_X2 = Arrays.copyOfRange(value, 4, 8);
        c_X3 = Arrays.copyOfRange(value, 8, 12);
        d_X4 = Arrays.copyOfRange(value, 12, 16);
        byte[] e = new byte[4];
        byte[] buff;
        K = getK(key);
        for (int i = 1; i <= 8; i++) {
            b_X2 = XOR(b_X2, G(5, square_plus(a_X1, K.get((7 * i - 6 - 1)))));
            printTestResult(b_X2);
            c_X3 = XOR(c_X3, G(21, square_plus(d_X4, K.get((7 * i - 5 - 1)))));
            printTestResult(c_X3);
            a_X1 = square_minus(a_X1, G(13, square_plus(b_X2, K.get(7 * i - 4 - 1))));
            printTestResult(a_X1);
            e = XOR(G(21, square_plus(K.get(7 * i - 3 - 1), square_plus(b_X2, c_X3))), getWord(i));
            printTestResult(e);
            b_X2 = square_plus(b_X2, e);
            printTestResult(b_X2);
            c_X3 = square_minus(c_X3, e);
            printTestResult(c_X3);
            d_X4 = square_plus(d_X4, G(13, square_plus(c_X3, K.get(7 * i - 2 - 1))));
            printTestResult(d_X4);
            b_X2 = XOR(b_X2, G(21, square_plus(a_X1, K.get((7 * i - 1 - 1)))));
            printTestResult(b_X2);
            c_X3 = XOR(c_X3, G(5, square_plus(d_X4, K.get((7 * i - 1)))));
            printTestResult(c_X3);

            buff = a_X1;
            a_X1 = b_X2;
            b_X2 = buff;

            buff = c_X3;
            c_X3 = d_X4;
            d_X4 = buff;

            buff = b_X2;
            b_X2 = c_X3;
            c_X3 = buff;

        }
//        System.out.println("-------------------------------------------------------------------");
        //printTestResult(ArrayUtils.addAll(ArrayUtils.addAll(b_X2,d_X4),ArrayUtils.addAll(a_X1,c_X3)));
        return ArrayUtils.addAll(ArrayUtils.addAll(b_X2, d_X4), ArrayUtils.addAll(a_X1, c_X3));
    }

    public static ArrayList<byte[]> getK(byte key[]) {// получает массив ключей K, принимает массив размером 56
        ArrayList<byte[]> K = new ArrayList<>();
        int pos = 0;
        for (int i = 0; i < 56; i++) {
            if (pos >= 8) {
                pos = 0;
            }
            K.add(Arrays.copyOfRange(key, pos * 4, (pos + 1) * 4));
            pos++;
        }
        return K;
    }

    public static byte[] G(int r, byte[] value) {
        return RotHi(r, value);
    }

    private static int signedByteToInteger(byte b) {
        return b & 0xFF;
    }

    public static long getAccordance(byte[] value) {
        long acc = signedByteToInteger(value[0]);
        acc += signedByteToInteger(value[1]) * Two_In8;
        acc += signedByteToInteger(value[2]) * Two_In16;
        acc += signedByteToInteger(value[3]) * Two_In24;
//        long acc = Byte.toUnsignedInt(value[0]);
//        acc += Byte.toUnsignedInt(value[1]) * Two_In8;
//        acc += Byte.toUnsignedInt(value[2]) * Two_In16;
//        acc += Byte.toUnsignedInt(value[3]) * Two_In24;
//        if(value.length ==4) {
//            for(int i = 0;i < value.length; i++) {
//                acc += signedByteToInteger(value[i]) * Math.pow(2,8*i);
//            }
//        }
        return acc;
    }

    public static byte[] getWordForFileLength(long lg) {// получает байтовое слово из длины файла, lg - длина файла
        byte[] buff = ByteBuffer.allocate(8).putLong(lg).array();
        byte[] result = new byte[16];
        for (int i = 0; i < buff.length; i++) {
            result[i] = buff[buff.length - i - 1];
        }
        return result;
    }

    public static byte[] getWord(long acc) {// получает слово из long, acc - long
        acc = acc % Two_In32; // добавил лонг --------------------------------------------------------!!!!!!!!!
        byte[] buffer = ByteBuffer.allocate(8).putLong(acc).array();

        return new byte[]{buffer[7], buffer[6], buffer[5], buffer[4]};
    }

    public static byte[] square_plus(byte[] u, byte[] v) {//операция сложения двух последовательностей
        return getWord(getAccordance(u) + getAccordance(v));
    }

    public static byte[] square_minus(byte[] u, byte[] v) {//операция разности двух последовательносей
        return getWord(getAccordance(u) - getAccordance(v));
    }

    public static byte[] RotHi(int cycle, byte[] val) {
        byte[] b = new byte[]{H_TABLE[signedByteToInteger(val[0])],
                H_TABLE[signedByteToInteger(val[1])],
                H_TABLE[signedByteToInteger(val[2])],
                H_TABLE[signedByteToInteger(val[3])]};
//                H_TABLE[Byte.toUnsignedInt(val[1])],
//                H_TABLE[Byte.toUnsignedInt(val[2])],
//                H_TABLE[Byte.toUnsignedInt(val[3])]};
        ByteBuffer byteBuffer = ByteBuffer.wrap(b);
        int buff = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getInt();
        //int buff = ByteBuffer.wrap(b).getInt();
//        String string = H_TABLE.get(signedByteToInteger(val[3])) +H_TABLE.get(signedByteToInteger(val[2])) +
//                H_TABLE.get(signedByteToInteger(val[1])) +
//                H_TABLE.get(signedByteToInteger(val[0])) ;
//        String swap = string.substring(cycle) + string.substring(0,cycle);
//        Long buff = Long.parseLong(swap,2);
        buff = ((buff << cycle) | (buff) >>> (32 - cycle));
        byteBuffer = ByteBuffer.allocate(4).putInt(buff).order(ByteOrder.BIG_ENDIAN);

        //buff  = buff << cycle;
        //b =  Arrays.copyOfRange(ByteBuffer.allocate(4).putInt(buff).array(),0,4);
        return new byte[]{byteBuffer.get(3), byteBuffer.get(2), byteBuffer.get(1), byteBuffer.get(0)};
    }

    public static void printTestResult(byte[] b) {
//        System.out.println(DatatypeConverter.printHexBinary(b));
    }

    public static void Start() {
        Start = System.currentTimeMillis();
    }

    public static void Stop() {
        Stop = System.currentTimeMillis();
        System.out.println(Stop - Start);
    }
    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }
}
