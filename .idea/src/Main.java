import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static final String FICHERO_DATOS = "coches.dat";
    private static final String FICHERO_CSV = "BBDD-Coches-1.csv";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        GestorDeFichero gestor = new GestorDeFichero("coches.dat");



        int opcion = 0;

        while(opcion != 6){

            interfaz();

            opcion = sc.nextInt();


                switch (opcion){
                    case 1 -> {

                        gestor.cargarCSV(FICHERO_CSV);

                        break;
                    }
                    case 2 -> {

                        break;
                    }
                    case 3 -> {

                        break;
                    }
                    case 4 -> {

                        break;
                    }
                    case 5 -> {

                        break;
                    }



                }


        }

    }

    public static void interfaz(){
        System.out.println("\n=== MENÚ GESTIÓN DE COCHES ===");
        System.out.println("1. Cargar información desde fichero CSV");
        System.out.println("2. Insertar un coche en una posición concreta");
        System.out.println("3. Ordenar el fichero por matrícula");
        System.out.println("4. Borrar un registro");
        System.out.println("5. Modificar el registro de una posición dada");
        System.out.println("6. Salir");
        System.out.print("Elige una opción: ");
    }
}
