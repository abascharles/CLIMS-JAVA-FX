package com.aircraft.util;

import com.aircraft.model.LauncherMission;
import com.aircraft.model.LauncherStatus;
import com.aircraft.model.Mission;
import com.aircraft.model.WeaponStatus;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for generating PDF reports.
 */
public class PDFGenerator {

    /**
     * Generates a fatigue monitoring report as a PDF.
     *
     * @param file The output file
     * @param launcherStatus The launcher status data
     * @param missionHistory The mission history data
     * @param username The username of the person generating the report
     * @param maintenanceStatus The maintenance status message
     * @throws IOException If an I/O error occurs
     */
    public void generateFatigueReport(File file, LauncherStatus launcherStatus,
                                      List<LauncherMission> missionHistory,
                                      String username, String maintenanceStatus) throws IOException {

        System.out.println("Generating PDF report with " +
                (missionHistory != null ? missionHistory.size() : 0) +
                " missions");

        // Create PDF document
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(36, 36, 36, 36);

        // Add header
        addHeader(document, launcherStatus.getPartNumber(), username);

        // Add launcher info
        addLauncherInfo(document, launcherStatus);

        // Add maintenance status
        addMaintenanceStatus(document, maintenanceStatus);

        // Create a simple graph visualization for launcher degradation
        try {
            addDegradationGraph(document, missionHistory, launcherStatus.getRemainingLifePercentage());
        } catch (Exception e) {
            System.err.println("Error adding degradation graph to PDF: " + e.getMessage());
            e.printStackTrace();
            document.add(new Paragraph("Error generating degradation graph")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.RED)
                    .setMarginTop(10));
        }

        // Add mission history table with proper error handling
        if (missionHistory != null && !missionHistory.isEmpty()) {
            System.out.println("Adding mission history table with " + missionHistory.size() + " missions");
            addMissionHistoryTable(document, missionHistory);
        } else {
            System.out.println("No mission history to add to report");
            document.add(new Paragraph("No mission history available.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                    .setMarginTop(10));
        }

        // Close document
        document.close();
        System.out.println("PDF report generation completed");
    }

    /**
     * Generates a mission report as a PDF.
     *
     * @param file The output file
     * @param mission The mission data
     * @param weapons The weapons (launchers and missiles) data
     * @throws IOException If an I/O error occurs
     */
    public void generateMissionReport(File file, Mission mission, WeaponStatus[] weapons) throws IOException {
        // Create PDF document
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(36, 36, 36, 36);

        // Add title
        document.add(new Paragraph("Mission Report #" + mission.getId())
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Add generation info
        document.add(new Paragraph("Generated: " + getCurrentDateTime())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20));

        // Add mission details section
        document.add(new Paragraph("Mission Information")
                .setBold()
                .setFontSize(16)
                .setMarginBottom(10));

        // Create mission info table
        Table missionTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}));
        missionTable.setWidth(UnitValue.createPercentValue(100));

        addInfoRow(missionTable, "Aircraft:", mission.getMatricolaVelivolo());
        addInfoRow(missionTable, "Flight Number:", String.valueOf(mission.getNumeroVolo()));
        addInfoRow(missionTable, "Date:", mission.getDataMissione().toString());

        if (mission.getOraPartenza() != null) {
            addInfoRow(missionTable, "Departure Time:", mission.getOraPartenza().toString());
        }

        if (mission.getOraArrivo() != null) {
            addInfoRow(missionTable, "Arrival Time:", mission.getOraArrivo().toString());
        }

        if (mission.getOraPartenza() != null && mission.getOraArrivo() != null) {
            // Calculate duration
            LocalTime depart = mission.getOraPartenza().toLocalTime();
            LocalTime arrive = mission.getOraArrivo().toLocalTime();
            long durationMinutes = java.time.Duration.between(depart, arrive).toMinutes();
            String durationStr = String.format("%02d:%02d", durationMinutes / 60, durationMinutes % 60);

            addInfoRow(missionTable, "Duration:", durationStr);
        }

        document.add(missionTable);
        document.add(new Paragraph(" ").setMarginBottom(10));

        // Add weapons section
        document.add(new Paragraph("Launchers and Missiles")
                .setBold()
                .setFontSize(16)
                .setMarginBottom(10));

        if (weapons != null && weapons.length > 0) {
            // Create weapons table
            Table weaponsTable = new Table(UnitValue.createPercentArray(
                    new float[]{15, 20, 20, 20, 20, 15}));
            weaponsTable.setWidth(UnitValue.createPercentValue(100));

            // Add headers
            weaponsTable.addHeaderCell(createHeaderCell("Position"));
            weaponsTable.addHeaderCell(createHeaderCell("Launcher P/N"));
            weaponsTable.addHeaderCell(createHeaderCell("Launcher S/N"));
            weaponsTable.addHeaderCell(createHeaderCell("Missile P/N"));
            weaponsTable.addHeaderCell(createHeaderCell("Missile Name"));
            weaponsTable.addHeaderCell(createHeaderCell("Status"));

            // Add data rows
            for (WeaponStatus weapon : weapons) {
                weaponsTable.addCell(weapon.getPosition());
                weaponsTable.addCell(weapon.getLauncherPartNumber());
                weaponsTable.addCell(weapon.getLauncherSerialNumber());
                weaponsTable.addCell(weapon.getMissilePartNumber());
                weaponsTable.addCell(weapon.getMissileName());
                weaponsTable.addCell(weapon.getStatus());
            }

            document.add(weaponsTable);
        } else {
            document.add(new Paragraph("No weapons data available for this mission")
                    .setItalic()
                    .setMarginBottom(10));
        }

        // Close document
        document.close();
    }

    /**
     * Adds the header section to the PDF document.
     */
    private void addHeader(Document document, String partNumber, String username) {
        // Create header table
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{30, 40, 30}));
        headerTable.setWidth(UnitValue.createPercentValue(100));

        // Date and username
        Cell dateTimeCell = new Cell();
        dateTimeCell.setBorder(null);
        dateTimeCell.add(new Paragraph("Date: " + getCurrentDateTime()));
        dateTimeCell.add(new Paragraph("User: " + username));
        headerTable.addCell(dateTimeCell);

        // Title
        Cell titleCell = new Cell();
        titleCell.setBorder(null);
        titleCell.add(new Paragraph("Launcher Fatigue Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(16));
        headerTable.addCell(titleCell);

        // Logo cell
        Cell logoCell = new Cell();
        logoCell.setBorder(null);
        logoCell.setTextAlignment(TextAlignment.RIGHT);

        try {
            // Try to load logo
            URL logoUrl = getClass().getResource("/images/clims_logo.png");
            if (logoUrl != null) {
                Image logo = new Image(ImageDataFactory.create(logoUrl));
                logo.setWidth(80);
                logoCell.add(logo);
            } else {
                logoCell.add(new Paragraph("CLIMS").setBold());
            }
        } catch (Exception e) {
            // If logo can't be loaded, use text instead
            logoCell.add(new Paragraph("CLIMS").setBold());
        }

        headerTable.addCell(logoCell);
        document.add(headerTable);

        // Report title
        document.add(new Paragraph("Fatigue Report: " + partNumber)
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setMarginBottom(10));

        // Add a separator
        document.add(new Paragraph(" ")
                .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                .setMarginBottom(10));
    }

    /**
     * Adds the launcher information section to the PDF document.
     */
    private void addLauncherInfo(Document document, LauncherStatus status) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}));
        infoTable.setWidth(UnitValue.createPercentValue(100));

        // Add launcher details
        addInfoRow(infoTable, "Launcher Name:", status.getLauncherName());
        addInfoRow(infoTable, "Part Number:", status.getPartNumber());

        // Serial Number is now "N/A" as per specification
        addInfoRow(infoTable, "Serial Number:", status.getSerialNumber());
        addInfoRow(infoTable, "Number of Missions:", String.valueOf(status.getMissionCount()));
        addInfoRow(infoTable, "Number of Firings:", String.valueOf(status.getFiringCount()));

        DecimalFormat df = new DecimalFormat("#,##0.00");
        addInfoRow(infoTable, "Flight Time (hours):", df.format(status.getFlightTime()));
        addInfoRow(infoTable, "Remaining Life (%):", df.format(status.getRemainingLifePercentage()));

        document.add(infoTable);
    }

    /**
     * Adds a row to the information table.
     */
    private void addInfoRow(Table table, String label, String value) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setBold());
        labelCell.setBorder(null);

        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value != null ? value : ""));
        valueCell.setBorder(null);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Adds the maintenance status section to the PDF document.
     */
    private void addMaintenanceStatus(Document document, String status) {
        Paragraph statusPara = new Paragraph("Maintenance Status: " + status);
        statusPara.setBold();

        // Set color based on status
        if (status.toUpperCase().contains("OK")) {
            statusPara.setFontColor(ColorConstants.DARK_GRAY);
            statusPara.setBackgroundColor(new DeviceRgb(230, 255, 230));
        } else if (status.toUpperCase().contains("ATTENZIONE")) {
            statusPara.setFontColor(ColorConstants.DARK_GRAY);
            statusPara.setBackgroundColor(new DeviceRgb(255, 245, 200));
        } else if (status.toUpperCase().contains("URGENTE")) {
            statusPara.setFontColor(ColorConstants.WHITE);
            statusPara.setBackgroundColor(new DeviceRgb(220, 50, 50));
        }

        statusPara.setPadding(10);
        statusPara.setMarginTop(15);
        statusPara.setMarginBottom(15);
        document.add(statusPara);
    }

    /**
     * Adds a simple degradation graph visualization to the PDF.
     * This is a new method to create a simplified graph for the PDF.
     */
    private void addDegradationGraph(Document document, List<LauncherMission> missionHistory, double remainingLife) {
        // Add title
        document.add(new Paragraph("Launcher Degradation Over Time")
                .setBold()
                .setFontSize(14)
                .setMarginTop(10)
                .setMarginBottom(5));

        // Create a simple visual representation of remaining life
        float width = 500f;
        float height = 40f;

        // Background rectangle (empty)
        Table graphTable = new Table(1);
        graphTable.setWidth(UnitValue.createPercentValue(100));

        Cell graphCell = new Cell();
        graphCell.setBorder(new SolidBorder(ColorConstants.GRAY, 1));
        graphCell.setPadding(5);

        // Create the progress bar
        Table progressBar = new Table(2);
        progressBar.setWidth(UnitValue.createPercentValue(100));

        // Remaining life bar (green)
        Cell remainingCell = new Cell();
        remainingCell.setWidth(UnitValue.createPercentValue((float)remainingLife));
        remainingCell.setHeight(height);
        remainingCell.setBorder(null);
        remainingCell.setBackgroundColor(new DeviceRgb(50, 180, 50));

        // Used life bar (red)
        Cell usedCell = new Cell();
        usedCell.setWidth(UnitValue.createPercentValue((float)(100 - remainingLife)));
        usedCell.setHeight(height);
        usedCell.setBorder(null);
        usedCell.setBackgroundColor(new DeviceRgb(220, 50, 50));

        progressBar.addCell(remainingCell);
        progressBar.addCell(usedCell);

        graphCell.add(progressBar);
        graphTable.addCell(graphCell);

        document.add(graphTable);

        // Add legend
        Paragraph legend = new Paragraph()
                .add(new Text("Remaining Life: ").setBold())
                .add(new Text(new DecimalFormat("#,##0.00").format(remainingLife) + "%"));
        legend.setMarginTop(5);
        document.add(legend);

        document.add(new Paragraph(" ").setMarginBottom(20));
    }

    /**
     * Adds the mission history table to the PDF document.
     */
    private void addMissionHistoryTable(Document document, List<LauncherMission> missionHistory) {
        try {
            // Add title
            document.add(new Paragraph("Mission History")
                    .setBold()
                    .setFontSize(14)
                    .setMarginTop(10)
                    .setMarginBottom(5));

            // Create table
            Table table = new Table(UnitValue.createPercentArray(new float[]{15, 20, 20, 20, 25}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Add headers
            table.addHeaderCell(createHeaderCell("Mission ID"));
            table.addHeaderCell(createHeaderCell("Date"));
            table.addHeaderCell(createHeaderCell("Aircraft"));
            table.addHeaderCell(createHeaderCell("Flight Time"));
            table.addHeaderCell(createHeaderCell("Damage Factor"));

            // Add data
            DecimalFormat df = new DecimalFormat("#,##0.00");

            // Safety check - if list is null, use empty list
            List<LauncherMission> safeList = missionHistory != null ?
                    missionHistory :
                    new ArrayList<>();

            if (safeList.isEmpty()) {
                // Add a row indicating no data
                Cell emptyCell = new Cell(1, 5)
                        .add(new Paragraph("No mission history data available"))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setItalic();
                table.addCell(emptyCell);
            } else {
                // Add actual data rows with error handling for each cell
                for (LauncherMission mission : safeList) {
                    try {
                        // Mission ID - with null check
                        table.addCell(String.valueOf(mission.getMissionId()));

                        // Date - handle null values
                        String dateStr = mission.getMissionDate() != null ?
                                mission.getMissionDate().toString() : "N/A";
                        table.addCell(dateStr);

                        // Aircraft - handle null values
                        table.addCell(mission.getAircraft() != null ? mission.getAircraft() : "N/A");

                        // Flight Time
                        table.addCell(df.format(mission.getFlightTime()) + " hrs");

                        // Damage Factor - shown as percentage
                        table.addCell(df.format(mission.getDamageFactor() * 100) + "%");
                    } catch (Exception e) {
                        System.err.println("Error adding mission row to PDF table: " + e.getMessage());
                        // Add placeholder cells if there's an error with this mission
                        for (int i = 0; i < 5; i++) {
                            table.addCell("Error");
                        }
                    }
                }
            }

            document.add(table);
            System.out.println("Mission history table added to PDF");
        } catch (Exception e) {
            System.err.println("Error creating mission history table in PDF: " + e.getMessage());
            e.printStackTrace();
            // Add error message to document instead of failing
            try {
                document.add(new Paragraph("Error generating mission history table")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.RED)
                        .setMarginTop(10));
            } catch (Exception ex) {
                // Last resort if even adding error message fails
                System.err.println("Failed to add error message to PDF: " + ex.getMessage());
            }
        }
    }

    /**
     * Creates a header cell for tables.
     */
    private Cell createHeaderCell(String text) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text).setBold());
        cell.setBackgroundColor(new DeviceRgb(240, 240, 240));
        return cell;
    }

    /**
     * Gets the current date and time formatted as a string.
     */
    private String getCurrentDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}