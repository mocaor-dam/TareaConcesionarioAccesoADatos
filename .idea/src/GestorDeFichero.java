import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class GestorDeFichero {
    private static final byte BYTE_ESPACIO = (byte) ' ';


    private static final String MATRICULA = "matricula";
    private static final String MARCA = "MARCA";
    private static final String MODELO = "MODELO";

    private static final int LONGITUD_MAT = 7;
    private static final int LONGITUD_MARCA_MODELO = 32;

    private final int MAXBYTEMATRICULA = 7;
    private final int MAXBYTEMARCA = 32;
    private final int MAXBYTEMODELO = 32;
    private String rutaFichero;

    public GestorDeFichero(String rutaFichero) {
        this.rutaFichero = rutaFichero;
    }

    public String ajustarTexto(String texto, int longitudMaxima) {
        if (texto.length() > longitudMaxima) {
            texto = texto.substring(0, longitudMaxima);
        }

        while (texto.length() < longitudMaxima) {
            texto = texto + " ";
        }

        return texto;
    }





    private byte[] formatearABytesFijos(String nombreCampo, String texto, int longitudFija) {
        byte[] bytesOriginales = texto.getBytes(StandardCharsets.UTF_8);

        if (bytesOriginales.length > longitudFija) {
            throw new IllegalArgumentException(
                    String.format("El campo '%s' ocupa %d bytes (máximo permitido: %d bytes).",
                            nombreCampo, bytesOriginales.length, longitudFija)
            );
        }

        byte[] bytesResultantes = new byte[longitudFija];
        Arrays.fill(bytesResultantes, BYTE_ESPACIO);
        System.arraycopy(bytesOriginales, 0, bytesResultantes, 0, bytesOriginales.length);

        return bytesResultantes;
    }

    private String extraerStringDeBytes(byte[] buffer, int offset, int longitud) {
        String texto = new String(buffer, offset, longitud, StandardCharsets.UTF_8);
        return texto.trim();
    }

}



