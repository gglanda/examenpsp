package Apellido_Nombre_20261112_PSP_EXAMEN_T1_FIXEDTHREADPOOL;

import java.util.ArrayList;
import java.util.List;
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
            "jhkDSSjhddS·$_", "akaeee*f56757oadfj232", "IAJSDQW", "LSADUAND77LIAUhkjh(/&", "saBIÑaNIGO26", "jUaN2334++ASDA",
            "jhkjhWQEddS·$_", "akasddfoadfj232", "IAJSDQW", "LSADUANDLIAUhkjh(/&", "saBIÑaNIGO26++", "jUaN2334++ASDA",
            "jhkddS·$_", "akasddKKKfj232", "kkkk", "1234", "pasword", "Jaca",
    };// PASSWORDS
    private static final int NUMERO_HILOS = 3;
    private static final int TIEMPO_MAXIMO_SEGUNDOS = 10;
    // Constante nueva para la regla de longitud.
    public static final int LONGITUD_MINIMA = 8;


    public static void main(String[] p_Args) {

        Buzon l_buzon = new Buzon();
        List<Callable<Boolean>> l_PasswordComplejidad = new ArrayList<>();
        List<Callable<Boolean>> l_PasswordLongitud = new ArrayList<>();
        List<Callable<Boolean>> l_PasswordFiloPrimo = new ArrayList<>();
        ExecutorService l_ejecutor;
        boolean l_terminado;

        // Añadimos las tareas a las listas
        for (int i = 0; i < PASSWORDS.length; i++) {

            l_PasswordComplejidad.add(new Tarea(Tarea.Caracteristica.COMPLEJIDAD, i, PASSWORDS[i], l_buzon));
            l_PasswordLongitud.add(new Tarea(Tarea.Caracteristica.LONGITUD, i, PASSWORDS[i], l_buzon));
            l_PasswordFiloPrimo.add(new Tarea(Tarea.Caracteristica.FILOPRIMO, i, PASSWORDS[i], l_buzon));

        }

        // Un único ejecutor para todas las tareas
        l_ejecutor = Executors.newFixedThreadPool(NUMERO_HILOS);


        //Las tareas se envían al ejecutor
        l_PasswordComplejidad.forEach(l_ejecutor::submit);
        l_PasswordLongitud.forEach(l_ejecutor::submit);
        l_PasswordFiloPrimo.forEach(l_ejecutor::submit);

        // Orden de apagado con tiempo máximo de espera
        l_ejecutor.shutdown();


        // Si se interrumpe, se restaura la interrupción.
        try {
            l_terminado = l_ejecutor.awaitTermination(TIEMPO_MAXIMO_SEGUNDOS, TimeUnit.SECONDS);
        } catch (InterruptedException l_excepcion) {
            Thread.currentThread().interrupt();
            l_terminado = false;
        }

        //  se usa if/else para proporcionar un único punto de salida al final
        if (!l_terminado) {

            l_ejecutor.shutdownNow();
            System.err.println("Se agotó el tiempo máximo (" + TIEMPO_MAXIMO_SEGUNDOS
                    + " s) antes de terminar todas las tareas.");

        } else {

            // El main muestra los resultados de cada password
            for (int i = 0; i < PASSWORDS.length; i++) {

                System.out.println("Password " + (i + 1) + ": \"" + PASSWORDS[i] + "\"");
                System.out.println("  Complejidad: " + (l_buzon.getComplejidad(i) ? "SÍ" : "NO"));
                System.out.println("  Longitud: " + (l_buzon.getLongitud(i) ? "SÍ" : "NO"));
                System.out.println("  FiloPrimo: " + (l_buzon.getFiloPrimo(i) ? "SÍ" : "NO"));

                System.out.println();

            }
        }  //if

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


    // Escritura (tareas)

    public void guardarComplejidad(int p_indice, boolean p_complejidad) {
        a_complejidades.put(p_indice, p_complejidad);
    }

    public void guardarLongitud(int p_indice, boolean p_longitud) {
        a_longitudes.put(p_indice, p_longitud);
    }

    public void guardarFiloPrimo(int p_indice, boolean p_filoPrimo) {
        a_filoPrimos.put(p_indice, p_filoPrimo);
    }


    // Lectura (main)

    public boolean getComplejidad(int p_indice) {
        return a_complejidades.getOrDefault(p_indice, false);
    }

    public boolean getLongitud(int p_indice) {
        return a_longitudes.getOrDefault(p_indice, false);
    }

    public boolean getFiloPrimo(int p_indice) {
        return a_filoPrimos.getOrDefault(p_indice, false);
    }

}

/**
 * Tarea que verifica UNA característica de UN password y deja el resultado en el Buzon.
 */
class Tarea implements Callable<Boolean> {

    public enum Caracteristica {COMPLEJIDAD, LONGITUD, FILOPRIMO}

    private final Caracteristica a_caracteristica;
    private final int a_indice;
    private final String a_password;
    private final Buzon a_buzon;

    /**
     * Tarea callable que detecta el cumplimiento de las normas
     */
    public Tarea(Caracteristica p_caracteristica, int p_indice, String p_password, Buzon p_buzon) {
        a_caracteristica = p_caracteristica;
        a_indice = p_indice;
        a_password = p_password;
        a_buzon = p_buzon;
    }

    /**
     * Calcula el resultado, lo guarda en el buzón y lo devuelve.
     */
    @Override
    public Boolean call() {
        boolean l_resultado = false;

        switch (a_caracteristica) {
            case COMPLEJIDAD -> {
                l_resultado = comprobarComplejidad(a_password);
                a_buzon.guardarComplejidad(a_indice, l_resultado);
            }
            case LONGITUD -> {
                l_resultado = comprobarLongitud(a_password);
                a_buzon.guardarLongitud(a_indice, l_resultado);
            }
            case FILOPRIMO -> {
                l_resultado = comprobarFiloPrimo(a_password);
                a_buzon.guardarFiloPrimo(a_indice, l_resultado);
            }
        }

        return l_resultado;
    }


    /**
     * DETECTA LA PRESENCIA DE CARACTERES ESPECIALES y LA PRESENCIA DE MAYÚSCULAS, MINÚSCULAS y NÚMEROS.
     * Cumple solo si tiene las cuatro cosas.
     */
    private boolean comprobarComplejidad(String p_Candidato) {
        boolean l_cumple;
        boolean l_TieneEspecial;
        boolean l_TieneMayuscula = false;
        boolean l_TieneMinuscula = false;
        boolean l_TieneNumero = false;
        char l_Caracter;
        //     ComprobadorPasswords.PATRON_ESPECIALES.
        l_TieneEspecial = ComprobadorPasswords.PATRON_ESPECIALES.matcher(p_Candidato).find();

        for (int i = 0; i < p_Candidato.length(); i++) {
            l_Caracter = p_Candidato.charAt(i);

            if (Character.isUpperCase(l_Caracter)) {
                l_TieneMayuscula = true;
            } else if (Character.isLowerCase(l_Caracter)) {
                l_TieneMinuscula = true;
            } else if (Character.isDigit(l_Caracter)) {
                l_TieneNumero = true;
            }
        }

        l_cumple = l_TieneEspecial && l_TieneMayuscula && l_TieneMinuscula && l_TieneNumero;

        return l_cumple;
    }

    /**
     * DETECTA SI CUMPLE LA LONGITUD MÍNIMA
     */
    private boolean comprobarLongitud(String p_Candidato) {

        boolean l_cumple;

        l_cumple = p_Candidato.length() >= ComprobadorPasswords.LONGITUD_MINIMA;

        return l_cumple;
    }

    /**
     * DETECTA SI ES FILOPRIMO
     */
    private boolean comprobarFiloPrimo(String p_Candidato) {
        boolean l_cumple;
        l_cumple = esPrimo(p_Candidato.length());
        return l_cumple;
    }

    /**
     * Devuelve true si el número es primo.
     */
    private boolean esPrimo(int p_Numero) {
        boolean l_esPrimo = p_Numero >= 2;
        int l_Divisor = 2;

        while (l_esPrimo && l_Divisor * l_Divisor <= p_Numero) {
            if (p_Numero % l_Divisor == 0) {
                l_esPrimo = false;
            }
            l_Divisor++;
        }

        return l_esPrimo;
    }

}