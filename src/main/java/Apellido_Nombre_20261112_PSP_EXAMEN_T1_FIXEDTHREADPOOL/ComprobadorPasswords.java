package Apellido_Nombre_20261112_PSP_EXAMEN_T1_FIXEDTHREADPOOL;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;


/**********************************************************************************************************************
 * APLICACIÓN: "ExamenPSP1" *
 **********************************************************************************************************************
 * PROGRAMACIÓN DE SERVICIOS Y PROCESOS 2DAM - Intellij v2021-09 (4.21.0) *
 **********************************************************************************************************************
 * @version 1.1 - Examen  *
 * 1.0 - Versión inicial del algoritmo. *
 * @since 13NOV2026 *
 * 13NOV2026 *
 *********************************************************************************************************************
 * COMENTARIOS: *
 * • “a_”  Atributos de la clase (p.e. “a_NumeroRuedas”).
 * • “l_”  Variables locales al metodo (p.e. “l_Contador”).
 * • “p_”  Parámetros de subrutinas -métodos y funciones- (p.e. “p_ModoCalculo”).
 * Métodos: verbo(+predicado) en lowerCamelCase
 * Constantes: autoexplicativos en ALL_UPPERCASE
 * Clases: UpperCamelCase
 * Constantes: Al principio de la clase
 * Subrutinas (métodos y funciones): Unico punto de salida situado al final
 * Variables: todas se declararán al principio del metodo.
 *********************************************************************************************************************/


public class ComprobadorPasswords {

    /*CONSTANTES*/
    public static final Pattern PATRON_ESPECIALES = Pattern.compile("[@#$%&*!+-]");
    public static final String[] PASSWORDS = {
            "jhkjhddS·$_", "akasddfoadfj232", "IAJSDQW", "LSADUANDLIAUhkjh(/&", "saBIÑaNIGO26", "jUaN2334++ASDA",
            "jhkDSSjhddS·$_", "akaeee*f56757oadfj232", "IAJSDQW", "LSADUANDLIAUhkjh(/&", "saBIÑaNIGO26", "jUaN2334++ASDA",
            "jhkjhWQEddS·$_", "akasddfoadfj232", "IAJSDQW", "LSADUANDLIAUhkjh(/&", "saBIÑaNIGO26++", "jUaN2334++ASDA",
            "jhkddS·$_", "akasddKKKfj232", "kkkk", "1234", "pasword", "Jaca",
    };//

    private static final int NUMERO_HILOS = 3;
    private static final int TIEMPO_MAXIMO_SEGUNDOS = 10;
    /*VARIABLES*/


    public static void main(String[] p_Args) {

        Buzon l_buzon = new Buzon();
        //List<Callable> l_Passwords = new ArrayList<>();

        // Declaración de variables : Una lista de tareas por cada característica a verificar
        List<Callable> l_PasswordComplejidad = new ArrayList<>();
        List<Callable> l_PasswordLongitud = new ArrayList<>();
        List<Callable> l_PasswordFiloPrimo = new ArrayList<>();

        // Añadimos las tareas a las listas
        for (int i = 0; i < PASSWORDS.length; i++) {

            l_PasswordComplejidad.add(new Tarea(Tarea.Caracteristica.COMPLEJIDAD, i, PASSWORDS[i], l_buzon));
            l_PasswordLongitud.add(new Tarea(Tarea.Caracteristica.LONGITUD, i, PASSWORDS[i], l_buzon));
            l_PasswordFiloPrimo.add(new Tarea(Tarea.Caracteristica.FILOPRIMO, i, PASSWORDS[i], l_buzon));

        }

        // Un único ejecutor para todas las tareas
        ExecutorService l_ejecutor = Executors.newFixedThreadPool(NUMERO_HILOS);
        l_PasswordComplejidad.forEach(l_ejecutor::execute);
        l_PasswordLongitud.forEach(l_ejecutor::execute);
        l_PasswordFiloPrimo.forEach(l_ejecutor::execute);


// Orden de apagado con tiempo máximo de espera
        l_ejecutor.shutdown();
        if (!l_ejecutor.awaitTermination(TIEMPO_MAXIMO_SEGUNDOS, TimeUnit.SECONDS)) {
            l_ejecutor.shutdownNow();
            System.err.println("Se agotó el tiempo máximo (" + TIEMPO_MAXIMO_SEGUNDOS
                    + " s) antes de terminar todas las tareas.");
            return; //(?)
        }

        // El main muestra los resultados de cada comentario

        for (int i = 0; i < PASSWORDS.length; i++) {
            List<String> l_marcas = l_buzon.getComplejidad(i);
            System.out.println("Password " + (i + 1) + ": \"" + PASSWORDS[i] + "\"");
            System.out.println("  Complejidad: " + (l_buzon.getLongitud(i) ? "SÍ" : "NO"));
            System.out.println("  Longitud: " + (l_buzon.getLongitud(i) ? "SÍ" : "NO"));
            System.out.println("  FiloPrimo: " + (l_buzon.getFiloPrimo(i) ? "SÍ" : "NO"));

            System.out.println();

        }



    } //main
} //ComprobadorPasswords

/**
        * Buzón de intercambio entre las tareas (que escriben) y el main (que lee).
        * Hay un mapa por característica, indexado por la posición del password.
        * Se usan ConcurrentHashMap porque varios hilos escriben a la vez.
        */
class Buzon {

    private final Map<Integer, Boolean> a_complejidades = new ConcurrentHashMap<>();
    private final Map<Integer, Boolean> a_longitudes = new ConcurrentHashMap<>();
    private final Map<Integer, Boolean> a_filoPrimos = new ConcurrentHashMap<>();


    // --- Escritura (tareas) ---

    public void guardarComplejidad(int p_indice, boolean p_complejidad) {
        a_complejidades.put(p_indice, p_complejidad);
    }
    public void guardarLongitud(int p_indice, boolean p_longitud) {
        a_longitudes.put(p_indice, p_longitud);
    }
    public void guardarFiloPrimo(int p_indice, boolean p_filoPrimo) {
        a_filoPrimos.put(p_indice, p_filoPrimo);
    }


    // --- Lectura (main) ---

    public boolean getComplejidad(int p_indice) {
        return a_complejidades.get(p_indice);
    }
    public boolean getLongitud(int p_indice) {
        return a_longitudes.get(p_indice);
    }
    public boolean getFiloPrimo(int p_indice) {
        return a_filoPrimos.get(p_indice);
    }

}
/**

 * Tarea que verifica UNA característica de UN comentario y deja el

 * resultado en el Buzon.

 */

class Tarea implements Callable<Boolean> {

    public enum Caracteristica { COMPLEJIDAD, LONGITUD, FILOPRIMO }
    private final Caracteristica a_caracteristica;
    private final int a_indice;
    private final String a_password;
    private final Buzon a_buzon;

/**
 * Tarea callable que detecta el cumplimiento de las normas
* */
    public Tarea(Caracteristica p_caracteristica, int p_indice, String p_password, Buzon p_buzon) {
        a_caracteristica = p_caracteristica;
        a_indice = p_indice;
        a_password = p_password;
        a_buzon = p_buzon;
    }

    @Override
    public void call() {

        switch (a_caracteristica) {
            case COMPLEJIDAD -> a_buzon.guardarComplejidad(a_indice, Complejidad(a_password));
            case LONGITUD -> a_buzon.guardarLongitud(a_indice, Longitud(a_password));
            case FILOPRIMO -> a_buzon.guardarFiloPrimo(a_indice, FiloPrimo(a_password));
        }
    }

    /**
     * Tarea para
     * DETECTAR LA PRESENCIA DE CARACTERES ESPECIALES
     * DETECTAR LA PRESENCIA DE MAYÚSCULAS, MINÚSCULAS y NÚMEROS
     */
    private boolean Complejidad(String p_Candidato) {

       // En la zona de constantes de la clase principal que contiene el main().
        final Pattern PATRON_ESPECIALES = Pattern.compile("[@#$%&*!+-]");
       // En Complejidad.call():

        String l_Candidato = p_Candidato;
        boolean l_cumple = false;

        boolean l_TieneEspecial = PATRON_ESPECIALES.matcher(l_Candidato).find();
        boolean l_TieneMayusculasMinusculas = false;
         enum Caracteristica { COMPLEJIDAD, LOMGITUD, FILOPRIMO }

         // En Complejidad.call():
        char Character;
        for (int i=0; i == l_Candidato.length() ; i++){
            switch (l_Candidato[i]) {

            }

    }

    /**
     * Tarea para DETECTAR SI CUMPLE LONGITUD
     */
    private boolean Longitud(String p_Candidato){
        /*todo Longitud()*/
    }
    /**
     * Tarea para ver si es FILOPRIMO
     */
    private boolean FiloPrimo(String p_Candidato){
        /*todo Longitud()*/
    }

}