package io.picthor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
@Slf4j
public class ProcessRunner {

    public String execute(String... command) throws Exception {
        StringBuilder output = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                log.debug("Failed to execute system command, output: {}", output.toString());
                throw new Exception(output.toString());
            }
        } catch (InterruptedException e) {
            log.error("Failed to execute system command, output: " + output.toString(), e);
            throw new Exception(output.toString(), e);
        }

        return output.toString().trim();
    }

}
