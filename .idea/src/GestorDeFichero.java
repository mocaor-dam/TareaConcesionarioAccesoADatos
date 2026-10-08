import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.IllegalFormatCodePointException;

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

    /**
     * Carga los datos de un fichero CSV en el fichero binario.
     * El contenido anterior del fichero binario se elimina.
     *
     * @param rutaCsv ruta del fichero CSV que contiene los coches
     */
    public void cargarCSV(String rutaCsv) {
        try (
                BufferedReader lector = new BufferedReader(
                        new FileReader(rutaCsv, StandardCharsets.UTF_8));

                RandomAccessFile raf = new RandomAccessFile(
                        rutaFichero, "rw")
        ) {
            /*
             * Se elimina el contenido anterior del fichero.
             * Así, al cargar el CSV no se acumulan registros repetidos.
             */
            raf.setLength(0);

            String linea;
            boolean primeraLinea = true;

            while ((linea = lector.readLine()) != null) {

                /*
                 * La primera línea contiene los nombres de los campos:
                 * Matrícula,Marca,Modelo
                 */
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }

                /*
                 * Si hay líneas en blanco, no se intentan procesar.
                 */
                if (linea.isBlank()) {
                    continue;
                }

                String[] datos = linea.split(",", -1);

                /*
                 * Cada línea válida debe tener exactamente:
                 * matrícula, marca y modelo.
                 */
                if (datos.length != 3) {
                    System.out.println("Línea ignorada: " + linea);
                    continue;
                }

                String matricula = datos[0].trim();
                String marca = datos[1].trim();
                String modelo = datos[2].trim();

                /*
                 * Se comprueba que los datos no estén vacíos y que
                 * quepan en los campos de tamaño fijo.
                 */
                if (!validarDatosCoche(matricula, marca, modelo)) {
                    System.out.println("Línea ignorada: " + linea);
                    continue;
                }

                /*
                 * Cada registro se escribe siempre en este orden:
                 * 7 bytes de matrícula, 32 bytes de marca y 32 de modelo.
                 */
                raf.write(formatearCadena(matricula, MAXBYTEMATRICULA));
                raf.write(formatearCadena(marca, MAXBYTEMARCA));
                raf.write(formatearCadena(modelo, MAXBYTEMODELO));
            }

            System.out.println("CSV cargado correctamente.");

        } catch (IOException e) {
            System.out.println("Error al cargar el CSV: " + e.getMessage());
        }
    }

    public void insertarCoche(int posicion, String matricula, String marca, String modelo) {

        if (!validarDatosCoche(matricula, marca, modelo)) {
            return;
        }

        if (existeMatricula(matricula)) {
            System.out.println("Error: ya existe un coche con esa matrícula.");
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(
                this.rutaFichero, "rw")) {

            long totalRegistros = raf.length() / TAM_REGISTRO;

            if (posicion < 0 || posicion > totalRegistros) {
                System.out.println("Error: posición fuera de límites.");
                return;
            }

            /*
             * Se desplaza desde el final hacia la posición indicada
             * para no sobrescribir registros que todavía no se han copiado.
             */
            for (long i = totalRegistros - 1; i >= posicion; i--) {
                raf.seek(i * TAM_REGISTRO);

                byte[] registroTemp = new byte[TAM_REGISTRO];
                raf.readFully(registroTemp);

                raf.seek((i + 1) * TAM_REGISTRO);
                raf.write(registroTemp);
            }

            byte[] bMatricula = formatearCadena(
                    matricula.trim(), MAXBYTEMATRICULA);

            byte[] bMarca = formatearCadena(
                    marca.trim(), MAXBYTEMARCA);

            byte[] bModelo = formatearCadena(
                    modelo.trim(), MAXBYTEMODELO);

            raf.seek((long) posicion * TAM_REGISTRO);

            raf.write(bMatricula);
            raf.write(bMarca);
            raf.write(bModelo);

            System.out.println(
                    "Coche insertado correctamente en la posición "
                            + posicion + ".");

        } catch (IOException e) {
            System.out.println(
                    "Error al insertar el coche: " + e.getMessage());
        }
    }

    private boolean existeMatricula(String matricula) {
        try (RandomAccessFile raf = new RandomAccessFile(rutaFichero, "r")){

            long totalRegistros = raf.length() / TAM_REGISTRO;

            for (long i = 0; i < totalRegistros; i++){
                byte[] bytesMatricula = new byte[MAXBYTEMATRICULA];

                raf.readFully(bytesMatricula);

                String matriculaLeida = new String(bytesMatricula, StandardCharsets.UTF_8).trim();

                raf.skipBytes(MAXBYTEMARCA + MAXBYTEMODELO);

                if (matriculaLeida.equalsIgnoreCase(matricula.trim())){
                    return true;
                }
            }

        } catch (IOException e){
            System.out.println("Error al buscar matricula: " + e.getMessage());
        }
        return false;
    }


    private boolean validarDatosCoche(
            String matricula,
            String marca,
            String modelo) {

        if (matricula == null || matricula.isBlank()) {
            System.out.println("Error: la matrícula no puede estar vacía.");
            return false;
        }

        if (marca == null || marca.isBlank()) {
            System.out.println("Error: la marca no puede estar vacía.");
            return false;
        }

        if (modelo == null || modelo.isBlank()) {
            System.out.println("Error: el modelo no puede estar vacío.");
            return false;
        }

        if (matricula.getBytes(StandardCharsets.UTF_8).length
                > MAXBYTEMATRICULA) {
            System.out.println("Error: la matrícula supera "
                    + MAXBYTEMATRICULA + " bytes.");
            return false;
        }

        if (marca.getBytes(StandardCharsets.UTF_8).length
                > MAXBYTEMARCA) {
            System.out.println("Error: la marca supera "
                    + MAXBYTEMARCA + " bytes.");
            return false;
        }

        if (modelo.getBytes(StandardCharsets.UTF_8).length
                > MAXBYTEMODELO) {
            System.out.println("Error: el modelo supera "
                    + MAXBYTEMODELO + " bytes.");
            return false;
        }

        return true;
    }

}



