import java.util.Scanner;

/**
 * Clase principal que despliega la interfaz de usuario en consola
 * y gestiona las opciones del menú numeradas del 1 al 7.
 *
 * Garantiza que el usuario solo introduzca posiciones válidas a partir de 1.
 */
public class Main {

    private static final String FICHERO_DATOS = "coches.dat";
    private static final String FICHERO_CSV_DEFECTO = "BBDD-Coches-1.csv";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        GestorDeFichero gestor = new GestorDeFichero(FICHERO_DATOS);

        int opcion = 0;

        while (opcion != 7) {
            interfaz();

            try {
                opcion = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: Debe introducir un número entero válido.");
                continue;
            }

            switch (opcion) {
                case 1 -> {
                    System.out.print("Introduce el nombre/ruta del CSV [Presiona ENTER para '" + FICHERO_CSV_DEFECTO + "']: ");
                    String ruta = sc.nextLine().trim();
                    if (ruta.isEmpty()) {
                        ruta = FICHERO_CSV_DEFECTO;
                    }
                    gestor.cargarCSV(ruta);
                }

                case 2 -> {
                    try {
                        System.out.print("Introduce la posición donde insertar (A partir de 1): ");
                        int posUsuario = Integer.parseInt(sc.nextLine().trim());

                        if (posUsuario < 1) {
                            System.out.println("Error: La posición debe ser un número mayor o igual a 1.");
                            break;
                        }

                        System.out.print("Introduce la matrícula (7 caracteres exactos): ");
                        String mat = sc.nextLine().trim();

                        System.out.print("Introduce la marca: ");
                        String marca = sc.nextLine().trim();

                        System.out.print("Introduce el modelo: ");
                        String modelo = sc.nextLine().trim();

                        // Se envía la posición tal como la entiende el usuario (Base 1)
                        gestor.insertarCoche(posUsuario, mat, marca, modelo);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: La posición debe ser un número entero.");
                    }
                }

                case 3 -> gestor.ordenarPorMatricula();

                case 4 -> {
                    System.out.println("Elija el método de borrado:");
                    System.out.println("1. Por posición");
                    System.out.println("2. Por matrícula (Campo Clave)");
                    System.out.print("Opción: ");
                    String subOpcion = sc.nextLine().trim();

                    if ("1".equals(subOpcion)) {
                        try {
                            System.out.print("Introduce la posición a borrar (A partir de 1): ");
                            int posUsuario = Integer.parseInt(sc.nextLine().trim());

                            if (posUsuario < 1) {
                                System.out.println("Error: La posición debe ser un número mayor o igual a 1.");
                                break;
                            }

                            gestor.borrarPorPosicion(posUsuario);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Posición inválida.");
                        }
                    } else if ("2".equals(subOpcion)) {
                        System.out.print("Introduce la matrícula a borrar: ");
                        String mat = sc.nextLine().trim();
                        gestor.borrarPorMatricula(mat);
                    } else {
                        System.out.println("Opción no válida.");
                    }
                }

                case 5 -> {
                    try {
                        System.out.print("Introduce la posición a modificar (A partir de 1): ");
                        int posUsuario = Integer.parseInt(sc.nextLine().trim());

                        if (posUsuario < 1) {
                            System.out.println("Error: La posición debe ser un número mayor o igual a 1.");
                            break;
                        }

                        System.out.print("Introduce la nueva marca: ");
                        String marca = sc.nextLine().trim();

                        System.out.print("Introduce el nuevo modelo: ");
                        String modelo = sc.nextLine().trim();

                        gestor.modificarRegistro(posUsuario, marca, modelo);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: La posición debe ser numérica.");
                    }
                }

                case 6 -> gestor.listarRegistros();

                case 7 -> System.out.println("Saliendo de la aplicación...");

                default -> System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
        sc.close();
    }

    /**
     * Despliega las opciones del menú numeradas del 1 al 7.
     */
    public static void interfaz() {
        System.out.println("\n=== MENÚ GESTIÓN DE COCHES ===");
        System.out.println("1. Cargar información desde fichero CSV");
        System.out.println("2. Insertar un coche en una posición concreta");
        System.out.println("3. Ordenar el fichero por matrícula");
        System.out.println("4. Borrar un registro (por Matrícula o Posición)");
        System.out.println("5. Modificar el registro de una posición dada");
        System.out.println("6. Mostrar todos los registros de la BBDD");
        System.out.println("7. Salir");
        System.out.print("Elige una opción (1-7): ");
    }
}