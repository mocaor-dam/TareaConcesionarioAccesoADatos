import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class GestorDeFichero {

    private static final int MAXBYTEMATRICULA = 7;
    private static final int MAXBYTEMARCA = 32;
    private static final int MAXBYTEMODELO = 32;
    private static final int TAM_REGISTRO = MAXBYTEMARCA + MAXBYTEMODELO + MAXBYTEMATRICULA;
    private String rutaFichero;

    public GestorDeFichero(String rutaFichero) {
        this.rutaFichero = rutaFichero;
    }


    private byte[] formatearCadena(String texto, int longitud){
        byte[] resultado = new byte[longitud];

        Arrays.fill(resultado, (byte) ' ');

        if (texto != null){
            byte[] textoBytes = texto.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(textoBytes, 0, resultado, 0, Math.min(textoBytes.length, longitud));
        }
        return resultado;
    }


    public void insertarCoche(int posicion, String matricula, String marca, String modelo){
        try (RandomAccessFile raf = new RandomAccessFile(this.rutaFichero, "rw")){

            long totalRegistros = raf.length() / TAM_REGISTRO;

            if (posicion < 0 || posicion > totalRegistros){
                System.out.println("Error: Posicion fuera de limites.");
                return;
            }

            for (long i = totalRegistros - 1;i >= posicion; i--){
                // Leer el registro de la posición 'i'
                raf.seek(i * TAM_REGISTRO);
                byte[] registroTemp = new byte[TAM_REGISTRO];
                raf.read(registroTemp);

                // Escribirlo en la posición 'i + 1'
                raf.seek((i + 1) * TAM_REGISTRO);
                raf.write(registroTemp);
            }

            byte[] bMatricula = formatearCadena(matricula, MAXBYTEMATRICULA);
            byte[] bMarca = formatearCadena(marca, MAXBYTEMARCA);
            byte[] bModelo = formatearCadena(modelo, MAXBYTEMODELO);

            raf.seek(posicion * TAM_REGISTRO); // Nos colocamos en la posición exacta
            raf.write(bMatricula);
            raf.write(bMarca);
            raf.write(bModelo);

            System.out.println("Coche insertado con éxito en la posición " + posicion);

        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

}



