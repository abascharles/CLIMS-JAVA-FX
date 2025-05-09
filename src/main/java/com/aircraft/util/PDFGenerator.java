package com.aircraft.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for generating PDF reports.
 * This is a simplified implementation for demonstration purposes.
 * In a real application, you would use a PDF library like iText, Apache PDFBox, etc.
 */
public class PDFGenerator {

    /**
     * Generates a fatigue monitoring report as a PDF file.
     *
     * @param file The file to write the PDF to
     * @param pylonId The pylon ID
     * @param aircraftType The aircraft type
     * @param launcherSerial The launcher serial number
     * @param launcherName The launcher name/nomenclatura
     * @param flightHours The flight hours
     * @param fatigueIndex The fatigue index
     * @throws FileNotFoundException If the file cannot be created
     */
    public void generateFatigueReport(
            File file,
            String pylonId,
            String aircraftType,
            String launcherSerial,
            String launcherName,
            double flightHours,
            double fatigueIndex) throws FileNotFoundException {

        // In a real application, this method would use a PDF library to generate the PDF
        // For this demo, we'll just create a text file with a .pdf extension

        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Create a simple report content
            String report = createReportContent(
                    pylonId,
                    aircraftType,
                    launcherSerial,
                    launcherName,
                    flightHours,
                    fatigueIndex
            );

            // Write the report content to the file
            fos.write(report.getBytes());
        } catch (Exception e) {
            throw new FileNotFoundException("Error creating PDF: " + e.getMessage());
        }
    }

    /**
     * Creates the content for the report.
     *
     * @param pylonId The pylon ID
     * @param aircraftType The aircraft type
     * @param launcherSerial The launcher serial number
     * @param launcherName The launcher name/nomenclatura
     * @param flightHours The flight hours
     * @param fatigueIndex The fatigue index
     * @return The report content as a string
     */
    private String createReportContent(
            String pylonId,
            String aircraftType,
            String launcherSerial,
            String launcherName,
            double flightHours,
            double fatigueIndex) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = dateFormat.format(new Date());

        // Create a formatting string
        String reportFormat =
                "================================================\n" +
                        "           FATIGUE MONITORING REPORT            \n" +
                        "================================================\n" +
                        "\n" +
                        "Report Generated: %s\n" +
                        "\n" +
                        "AIRCRAFT INFORMATION\n" +
                        "-----------------------------------------------\n" +
                        "Pylon ID:           %s\n" +
                        "Aircraft Type:      %s\n" +
                        "\n" +
                        "LAUNCHER INFORMATION\n" +
                        "-----------------------------------------------\n" +
                        "Serial Number:      %s\n" +
                        "Nomenclatura:       %s\n" +
                        "\n" +
                        "FATIGUE DATA\n" +
                        "-----------------------------------------------\n" +
                        "Flight Hours:       %.2f hours\n" +
                        "Fatigue Index:      %.2f\n" +
                        "\n" +
                        "ANALYSIS\n" +
                        "-----------------------------------------------\n%s\n" +
                        "\n" +
                        "RECOMMENDATIONS\n" +
                        "-----------------------------------------------\n%s\n" +
                        "\n" +
                        "================================================\n" +
                        "            END OF REPORT                       \n" +
                        "================================================\n";

        // Generate analysis and recommendations based on fatigue index
        String analysis = generateAnalysis(fatigueIndex);
        String recommendations = generateRecommendations(fatigueIndex);

        // Format the report
        return String.format(
                reportFormat,
                currentDate,
                pylonId,
                aircraftType,
                launcherSerial,
                launcherName,
                flightHours,
                fatigueIndex,
                analysis,
                recommendations
        );
    }

    /**
     * Generates an analysis based on the fatigue index.
     *
     * @param fatigueIndex The fatigue index
     * @return The analysis text
     */
    private String generateAnalysis(double fatigueIndex) {
        if (fatigueIndex < 0.3) {
            return "The launcher shows minimal signs of fatigue. The current\n" +
                    "fatigue index is well within the normal operating parameters\n" +
                    "and indicates good structural health.";
        } else if (fatigueIndex < 0.6) {
            return "The launcher shows moderate signs of fatigue, consistent\n" +
                    "with its operational history. The fatigue index indicates\n" +
                    "that regular inspections should be maintained.";
        } else if (fatigueIndex < 0.8) {
            return "The launcher shows significant signs of fatigue. The fatigue\n" +
                    "index is approaching levels that require increased monitoring\n" +
                    "and potential preventive maintenance.";
        } else {
            return "The launcher shows high levels of fatigue. The fatigue index\n" +
                    "indicates potential structural concerns that should be\n" +
                    "addressed promptly to ensure continued safe operation.";
        }
    }

    /**
     * Generates recommendations based on the fatigue index.
     *
     * @param fatigueIndex The fatigue index
     * @return The recommendations text
     */
    private String generateRecommendations(double fatigueIndex) {
        if (fatigueIndex < 0.3) {
            return "- Continue regular maintenance schedule\n" +
                    "- Perform standard pre-flight inspections\n" +
                    "- No additional actions required";
        } else if (fatigueIndex < 0.6) {
            return "- Increase inspection frequency to every 50 flight hours\n" +
                    "- Perform detailed structural analysis at next scheduled maintenance\n" +
                    "- Monitor for any changes in operational performance";
        } else if (fatigueIndex < 0.8) {
            return "- Schedule comprehensive structural inspection within next 20 flight hours\n" +
                    "- Implement enhanced pre-flight inspection protocol\n" +
                    "- Consider reducing maximum load capacity by 15%\n" +
                    "- Plan for potential component replacement at next major service";
        } else {
            return "- Immediate inspection required before next flight\n" +
                    "- Reduce maximum load capacity by 30%\n" +
                    "- Schedule major service and component replacement\n" +
                    "- Consider retirement/replacement if fatigue index exceeds 0.9";
        }
    }
}