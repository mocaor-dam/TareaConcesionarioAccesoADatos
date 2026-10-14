import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Clase encargada de gestionar las operaciones CRUD y de ordenación sobre
 * el archivo binario de acceso aleatorio que actúa como BBDD de coches.
 *
 * Formato fijo por registro:
 * - Matrícula: 7 bytes
 * - Marca: 32 bytes
 * - Modelo: 32 bytes
 * Total: 71 bytes por registro.
 */
public class GestorDeFichero {

    private static final int MAXBYTEMATRICULA = 7;
    private static final int MAXBYTEMARCA = 32;
    private static final int MAXBYTEMODELO = 32;
    private static final int TAM_REGISTRO = MAXBYTEMATRICULA + MAXBYTEMARCA + MAXBYTEMODELO;

    private final String rutaFichero;

    /**
     * Constructor del gestor de ficheros.
     *
     * @param rutaFichero Ruta del archivo binario (.dat) que almacena la BBDD.
     */
    public GestorDeFichero(String rutaFichero) {
        this.rutaFichero = rutaFichero;
    }

    /**
     * Formatea una cadena a un arreglo de bytes de longitud fija rellenando con espacios.
     */
    private byte[] formatearCadena(String texto, int longitud) {
        byte[] resultado = new byte[longitud];
        Arrays.fill(resultado, (byte) ' ');

        if (texto != null) {
            byte[] textoBytes = texto.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(textoBytes, 0, resultado, 0, Math.min(textoBytes.length, longitud));
        }
        return resultado;
    }

    /**
     * Escribe o reescribe los bytes de un registro en una posición física del archivo (Base 0).
     */
    private void escribirRegistro(RandomAccessFile raf, long posicionFisica, String matricula, String marca, String modelo) throws IOException {
        raf.seek(posicionFisica * TAM_REGISTRO);
        raf.write(formatearCadena(matricula.trim(), MAXBYTEMATRICULA));
        raf.write(formatearCadena(marca.trim(), MAXBYTEMARCA));
        raf.write(formatearCadena(modelo.trim(), MAXBYTEMODELO));
    }

    /**
     * Carga los datos de un fichero CSV en el fichero binario eliminando el contenido anterior.
     */
    public void cargarCSV(String rutaCsv) {
        try (BufferedReader lector = new BufferedReader(new FileReader(rutaCsv, StandardCharsets.UTF_8));
             RandomAccessFile raf = new RandomAccessFile(rutaFichero, "rw")) {

            raf.setLength(0); // Vaciar el archivo binario anterior

            String linea;
            boolean primeraLinea = true;
            int contador = 0;

            while ((linea = lector.readLine()) != null) {
                if (primeraLinea) {
                    if (linea.startsWith("\uFEFF")) {
                        linea = linea.substring(1);
                    }
                    primeraLinea = false;
                    if (linea.toLowerCase().contains("matrícula") || linea.toLowerCase().contains("matricula")) {
                        continue;
                    }
                }

                if (linea.isBlank()) {
                    continue;
                }

                String[] datos = linea.split(",", -1);
                if (datos.length < 3) {
                    System.out.println("Línea ignorada por formato incorrecto: " + linea);
                    continue;
                }

                String matricula = datos[0].trim();
                String marca = datos[1].trim();
                String modelo = datos[2].trim();

                if (!validarDatosCoche(matricula, marca, modelo)) {
                    System.out.println("Línea ignorada por validación de datos: " + linea);
                    continue;
                }

                escribirRegistro(raf, contador, matricula, marca, modelo);
                contador++;
            }

            System.out.println("CSV cargado correctamente. Registros importados: " + contador);

        } catch (IOException e) {
            System.out.println("Error al cargar el CSV: " + e.getMessage());
        }
    }

    /**
     * Inserta un coche en una posición en base 1 introducida por el usuario.
     * Rechaza la posición 0 o posiciones superiores al número total de registros + 1.
     */
    public void insertarCoche(int posUsuario, String matricula, String marca, String modelo) {
        if (posUsuario < 1) {
            System.out.println("Error: La posición debe ser igual o mayor que 1.");
            return;
        }

        if (!validarDatosCoche(matricula, marca, modelo)) {
            return;
        }

        if (existeMatricula(matricula)) {
            System.out.println("Error: ya existe un coche registrado con esa matrícula.");
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(this.rutaFichero, "rw")) {
            long totalRegistros = raf.length() / TAM_REGISTRO;

            // En base 1, el rango permitido para insertar va desde 1 hasta (totalRegistros + 1)
            if (posUsuario > totalRegistros + 1) {
                System.out.println("Error: posición fuera de límites (Rango permitido actualmente: 1 a " + (totalRegistros + 1) + ").");
                return;
            }

            int posFisica = posUsuario - 1; // Conversión interna a base 0

            byte[] registroTemp = new byte[TAM_REGISTRO];
            for (long i = totalRegistros - 1; i >= posFisica; i--) {
                raf.seek(i * TAM_REGISTRO);
                raf.readFully(registroTemp);

                raf.seek((i + 1) * TAM_REGISTRO);
                raf.write(registroTemp);
            }

            escribirRegistro(raf, posFisica, matricula, marca, modelo);
            System.out.println("Coche insertado correctamente en la posición " + posUsuario + ".");

        } catch (IOException e) {
            System.out.println("Error al insertar el coche: " + e.getMessage());
        }
    }

    /**
     * Ordena el archivo binario por el campo Matrícula utilizando Bubble Sort.
     */
    public void ordenarPorMatricula() {
        try (RandomAccessFile raf = new RandomAccessFile(rutaFichero, "rw")) {
            long totalRegistros = raf.length() / TAM_REGISTRO;

            if (totalRegistros <= 1) {
                System.out.println("No hay suficientes registros para ordenar.");
                return;
            }

            byte[] reg1 = new byte[TAM_REGISTRO];
            byte[] reg2 = new byte[TAM_REGISTRO];

            for (int i = 0; i < totalRegistros - 1; i++) {
                for (int j = 0; j < totalRegistros - i - 1; j++) {
                    raf.seek(j * TAM_REGISTRO);
                    raf.readFully(reg1);
                    raf.readFully(reg2);

                    String mat1 = new String(reg1, 0, MAXBYTEMATRICULA, StandardCharsets.UTF_8).trim();
                    String mat2 = new String(reg2, 0, MAXBYTEMATRICULA, StandardCharsets.UTF_8).trim();

                    if (mat1.compareToIgnoreCase(mat2) > 0) {
                        raf.seek(j * TAM_REGISTRO);
                        raf.write(reg2);
                        raf.write(reg1);
                    }
                }
            }

            System.out.println("El fichero ha sido ordenado por matrícula correctamente.");

        } catch (IOException e) {
            System.out.println("Error al ordenar el fichero: " + e.getMessage());
        }
    }

    /**
     * Borra un registro buscando por su número de posición en Base 1.
     */
    public void borrarPorPosicion(int posUsuario) {
        if (posUsuario < 1) {
            System.out.println("Error: La posición debe ser igual o mayor que 1.");
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(rutaFichero, "rw")) {
            long totalRegistros = raf.length() / TAM_REGISTRO;

            if (posUsuario > totalRegistros) {
                System.out.println("Error: posición fuera de límites (Existen " + totalRegistros + " registros).");
                return;
            }

            int posFisica = posUsuario - 1;

            byte[] buffer = new byte[TAM_REGISTRO];
            for (long i = posFisica; i < totalRegistros - 1; i++) {
                raf.seek((i + 1) * TAM_REGISTRO);
                raf.readFully(buffer);

                raf.seek(i * TAM_REGISTRO);
                raf.write(buffer);
            }

            raf.setLength((totalRegistros - 1) * TAM_REGISTRO);
            System.out.println("Registro en la posición " + posUsuario + " borrado con éxito.");

        } catch (IOException e) {
            System.out.println("Error al borrar por posición: " + e.getMessage());
        }
    }

    /**
     * Borra un registro buscando por su campo clave (Matrícula).
     */
    public void borrarPorMatricula(String matricula) {
        int posFisica = obtenerPosicionPorMatricula(matricula);
        if (posFisica == -1) {
            System.out.println("Error: no se encontró ningún registro con la matrícula " + matricula);
            return;
        }
        borrarPorPosicion(posFisica + 1); // Se pasa la posición convertida a Base 1
    }

    /**
     * Modifica Marca y Modelo de un registro en una posición específica (Base 1).
     */
    public void modificarRegistro(int posUsuario, String nuevaMarca, String nuevoModelo) {
        if (posUsuario < 1) {
            System.out.println("Error: La posición debe ser igual o mayor que 1.");
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(rutaFichero, "rw")) {
            long totalRegistros = raf.length() / TAM_REGISTRO;

            if (posUsuario > totalRegistros) {
                System.out.println("Error: posición fuera de límites (Existen " + totalRegistros + " registros).");
                return;
            }

            if (nuevaMarca == null || nuevaMarca.isBlank() || nuevoModelo == null || nuevoModelo.isBlank()) {
                System.out.println("Error: La marca y el modelo no pueden estar vacíos.");
                return;
            }

            if (nuevaMarca.getBytes(StandardCharsets.UTF_8).length > MAXBYTEMARCA ||
                    nuevoModelo.getBytes(StandardCharsets.UTF_8).length > MAXBYTEMODELO) {
                System.out.println("Error: La marca o el modelo superan los límites de bytes permitidos.");
                return;
            }

            int posFisica = posUsuario - 1;

            raf.seek(posFisica * TAM_REGISTRO);
            byte[] bytesMatricula = new byte[MAXBYTEMATRICULA];
            raf.readFully(bytesMatricula);
            String matriculaExistente = new String(bytesMatricula, StandardCharsets.UTF_8).trim();

            escribirRegistro(raf, posFisica, matriculaExistente, nuevaMarca, nuevoModelo);
            System.out.println("Registro en posición " + posUsuario + " modificado correctamente.");

        } catch (IOException e) {
            System.out.println("Error al modificar el registro: " + e.getMessage());
        }
    }

    /**
     * Muestra el contenido del archivo de la BBDD enumerando las posiciones desde el 1.
     */
    public void listarRegistros() {
        try (RandomAccessFile raf = new RandomAccessFile(rutaFichero, "r")) {
            long totalRegistros = raf.length() / TAM_REGISTRO;

            if (totalRegistros == 0) {
                System.out.println("La base de datos está vacía.");
                return;
            }

            System.out.println("\n------------------------------------------------------------------");
            System.out.printf("%-8s | %-10s | %-20s | %-20s%n", "POS", "MATRÍCULA", "MARCA", "MODELO");
            System.out.println("------------------------------------------------------------------");

            byte[] bufferMat = new byte[MAXBYTEMATRICULA];
            byte[] bufferMarca = new byte[MAXBYTEMARCA];
            byte[] bufferMod = new byte[MAXBYTEMODELO];

            for (long i = 0; i < totalRegistros; i++) {
                raf.seek(i * TAM_REGISTRO);
                raf.readFully(bufferMat);
                raf.readFully(bufferMarca);
                raf.readFully(bufferMod);

                String mat = new String(bufferMat, StandardCharsets.UTF_8).trim();
                String mar = new String(bufferMarca, StandardCharsets.UTF_8).trim();
                String mod = new String(bufferMod, StandardCharsets.UTF_8).trim();

                // POS muestra posiciones empezando en 1
                System.out.printf("%-8d | %-10s | %-20s | %-20s%n", (i + 1), mat, mar, mod);
            }
            System.out.println("------------------------------------------------------------------\n");

        } catch (IOException e) {
            System.out.println("Error al listar los registros: " + e.getMessage());
        }
    }

    private boolean existeMatricula(String matricula) {
        return obtenerPosicionPorMatricula(matricula) != -1;
    }

    private int obtenerPosicionPorMatricula(String matricula) {
        try (RandomAccessFile raf = new RandomAccessFile(rutaFichero, "r")) {
            long totalRegistros = raf.length() / TAM_REGISTRO;

            for (int i = 0; i < totalRegistros; i++) {
                raf.seek((long) i * TAM_REGISTRO);
                byte[] bytesMatricula = new byte[MAXBYTEMATRICULA];
                raf.readFully(bytesMatricula);

                String matriculaLeida = new String(bytesMatricula, StandardCharsets.UTF_8).trim();
                if (matriculaLeida.equalsIgnoreCase(matricula.trim())) {
                    return i;
                }
            }
        } catch (IOException e) {
            // Fichero aún no existe
        }
        return -1;
    }

    private boolean validarDatosCoche(String matricula, String marca, String modelo) {
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

        if (matricula.trim().getBytes(StandardCharsets.UTF_8).length > MAXBYTEMATRICULA) {
            System.out.println("Error: la matrícula supera " + MAXBYTEMATRICULA + " bytes.");
            return false;
        }

        if (marca.trim().getBytes(StandardCharsets.UTF_8).length > MAXBYTEMARCA) {
            System.out.println("Error: la marca supera " + MAXBYTEMARCA + " bytes.");
            return false;
        }

        if (modelo.trim().getBytes(StandardCharsets.UTF_8).length > MAXBYTEMODELO) {
            System.out.println("Error: el modelo supera " + MAXBYTEMODELO + " bytes.");
            return false;
        }

        return true;
    }
}