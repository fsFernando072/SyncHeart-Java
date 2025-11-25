package school.sptech;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvWriter {

    public ByteArrayOutputStream writeCsv(List<Log> logs) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("TIMESTAMP", "UUID", "ARRITMIA",
                "CPU", "RAM", "DISCO", "BATERIA", "TOTAL_TAREFAS", "LISTA_TAREFAS"));

        for (Log log : logs) {
            csvPrinter.printRecord(
                    log.getTimestamp(),
                    log.getUuid(),
                    log.getArritmia(),
                    log.getValorCpu(),
                    log.getValorRam(),
                    log.getValorDisco(),
                    log.getValorBateria(),
                    log.getTotalTarefas(),
                    log.getListaTarefas()
            );
        }

        csvPrinter.flush();
        writer.close();

        return outputStream;
    }
}
