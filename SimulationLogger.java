import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimulationLogger {

    private final Path logFilePath;

    public SimulationLogger(String fileName, String input, boolean graphicMode){
        Path outputDir = Paths.get("computing");
        try{
            Files.createDirectories(outputDir);
        }catch(IOException e){
            System.err.println("No se pudo crear el directorio de logs: " + outputDir.toAbsolutePath());
        }

        this.logFilePath = outputDir.resolve(fileName);

        try(PrintWriter writer = new PrintWriter(new FileWriter(logFilePath.toFile()))){
            writer.println(" Calculos Maquina de Turing ");
            writer.println("Lenguaje: {0^n1^n | n>=1}");
            writer.println("Cadena de entrada: " + input);
            writer.println("Longitud: " + input.length() + " caracteres");
            if(graphicMode){
                writer.println("Modo: Grafico");
            }else{
                writer.println("Modo: No Grafico");
            }
        }catch(IOException e){
            System.err.println("Error al crear archivo de log: " + e.getMessage());
        }
    }

    public void logStep(int step, String state, char symbol, Transition transition, boolean enable){
        if(!enable){
            return;
        }
        try(PrintWriter writer = new PrintWriter(new FileWriter(logFilePath.toFile(), true))){
            writer.println("Paso " + step + ":");
            writer.println("Estado: " + state + ", Simbolo: '" + symbol + "'");
            writer.println("delta(" + state + ", " + symbol + ") = (" +
                    transition.getNextState() + ", " + transition.getWriteSymbol() + ", " +
                    transition.getDirection() + ")\n");
        }catch(IOException e){
            System.err.println("Error al escribir en archivo de log: " + e.getMessage());
        }
    }

    public void logNoTransition(int step, String state, char symbol, boolean enable){
        if(!enable){
            return;
        }
        try(PrintWriter writer = new PrintWriter(new FileWriter(logFilePath.toFile(), true))){
            writer.println("Paso " + step + ":");
            writer.println("No hay transicion para estado " + state + " con simbolo '" + symbol + "'");
            writer.println("Cadena rechazada\n");
        }catch(IOException e){
            System.err.println("Error al escribir en archivo de log: " + e.getMessage());
        }
    }

    public void logAcceptance(int step){
        try(PrintWriter writer = new PrintWriter(new FileWriter(logFilePath.toFile(), true))){
            writer.println("Estado de aceptacion alcanzado");
            writer.println("Cadena aceptada");
            writer.println("Total de pasos: " + step);
            writer.println("La cadena pertenece al lenguaje {0^n1^n | n>=1}\n");
        }catch(IOException e){
            System.err.println("Error al escribir en archivo de log: " + e.getMessage());
        }
    }

    public void logLoopLimit(int stepLimit){
        try(PrintWriter writer = new PrintWriter(new FileWriter(logFilePath.toFile(), true))){
            writer.println("Limite de pasos alcanzado (" + stepLimit + ")");
        }catch(IOException e){
            System.err.println("Error al escribir en archivo de log: " + e.getMessage());
        }
    }
}
