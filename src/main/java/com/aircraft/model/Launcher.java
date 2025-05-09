package com.aircraft.model;

import java.math.BigDecimal;

/**
 * Model class representing a launcher in the system.
 * Corresponds to the 'anagrafica_lanciatore' table in the database.
 */
public class Launcher {
    private String partNumber;
    private String nomenclatura;
    private String codiceDitta;
    private BigDecimal oreVitaOperativa;

    /**
     * Default constructor.
     */
    public Launcher() {
    }

    /**
     * Constructor with parameters.
     *
     * @param partNumber Part number (primary key)
     * @param nomenclatura Nomenclature/Name
     * @param codiceDitta Company code
     * @param oreVitaOperativa Operational life hours
     */
    public Launcher(String partNumber, String nomenclatura, String codiceDitta, BigDecimal oreVitaOperativa) {
        this.partNumber = partNumber;
        this.nomenclatura = nomenclatura;
        this.codiceDitta = codiceDitta;
        this.oreVitaOperativa = oreVitaOperativa;
    }

    /**
     * Gets the part number.
     *
     * @return The part number
     */
    public String getPartNumber() {
        return partNumber;
    }

    /**
     * Sets the part number.
     *
     * @param partNumber The part number to set
     */
    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    /**
     * Gets the nomenclature (name).
     *
     * @return The nomenclature
     */
    public String getNomenclatura() {
        return nomenclatura;
    }

    /**
     * Sets the nomenclature (name).
     *
     * @param nomenclatura The nomenclature to set
     */
    public void setNomenclatura(String nomenclatura) {
        this.nomenclatura = nomenclatura;
    }

    /**
     * Gets the company code.
     *
     * @return The company code
     */
    public String getCodiceDitta() {
        return codiceDitta;
    }

    /**
     * Sets the company code.
     *
     * @param codiceDitta The company code to set
     */
    public void setCodiceDitta(String codiceDitta) {
        this.codiceDitta = codiceDitta;
    }

    /**
     * Gets the operational life hours.
     *
     * @return The operational life hours
     */
    public BigDecimal getOreVitaOperativa() {
        return oreVitaOperativa;
    }

    /**
     * Sets the operational life hours.
     *
     * @param oreVitaOperativa The operational life hours to set
     */
    public void setOreVitaOperativa(BigDecimal oreVitaOperativa) {
        this.oreVitaOperativa = oreVitaOperativa;
    }

    /**
     * Returns a string representation of the Launcher object.
     *
     * @return A string representation of the Launcher
     */
    @Override
    public String toString() {
        return "Launcher{" +
                "partNumber='" + partNumber + '\'' +
                ", nomenclatura='" + nomenclatura + '\'' +
                ", codiceDitta='" + codiceDitta + '\'' +
                ", oreVitaOperativa=" + oreVitaOperativa +
                '}';
    }
}